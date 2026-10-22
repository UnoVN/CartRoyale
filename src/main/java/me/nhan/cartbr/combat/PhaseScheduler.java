package me.nhan.cartbr.combat;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.arena.GameState;
import me.nhan.cartbr.arena.PhaseType;
import me.nhan.cartbr.locale.Language;
import net.kyori.adventure.bossbar.BossBar;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.WorldBorder;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;
import java.util.UUID;

public class PhaseScheduler {

    private final CartBRPlugin plugin;
    private BossBar bossBarEN;
    private BossBar bossBarVI;
    private BukkitTask task;

    private PhaseType currentPhase = PhaseType.DROP;
    private int totalDuration = 35;
    private int timeLeft = 35;

    public PhaseScheduler(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    public void start(List<Player> players) {
        stop();
        this.bossBarEN = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);
        this.bossBarVI = BossBar.bossBar(Component.empty(), 1.0f, BossBar.Color.PURPLE, BossBar.Overlay.PROGRESS);

        for (Player p : players) {
            assignBossBar(p);
        }

        setPhaseState(PhaseType.DROP, plugin.getConfigManager().getPhaseDuration("drop-max-seconds", 35));

        this.task = new BukkitRunnable() {
            @Override
            public void run() {
                tickSecond();
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    private void tickSecond() {
        updateBossBars();

        if (timeLeft <= 0) {
            skipToNextPhase();
            return;
        }

        if (currentPhase == PhaseType.PHASE_3 && timeLeft <= 5) {
            plugin.getMatchManager().broadcastToMatch("deathmatch.teleport-warning", "<seconds>", String.valueOf(timeLeft));
        }

        timeLeft--;
    }

    public void skipToNextPhase() {
        switch (currentPhase) {
            case DROP -> advanceToPhase(PhaseType.PHASE_1);
            case PHASE_1 -> advanceToPhase(PhaseType.PHASE_2);
            case PHASE_2 -> advanceToPhase(PhaseType.PHASE_3);
            case PHASE_3 -> advanceToPhase(PhaseType.DEATHMATCH);
            case DEATHMATCH -> {
                Player winner = plugin.getMatchManager().getAlivePlayers().stream()
                        .map(Bukkit::getPlayer).findFirst().orElse(null);
                plugin.getMatchManager().endMatch(winner);
            }
        }
    }

    public void advanceToPhase(PhaseType nextPhase) {
        World world = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        if (world == null) return;
        WorldBorder border = world.getWorldBorder();

        switch (nextPhase) {
            case PHASE_1 -> {
                int dur = plugin.getConfigManager().getPhaseDuration("phase-1", 60);
                setPhaseState(PhaseType.PHASE_1, dur);
                border.setSize(plugin.getConfigManager().getPhaseBorderSize("phase-1", 450.0));
                notifyPhaseChange(dur);
            }
            case PHASE_2 -> {
                int dur = plugin.getConfigManager().getPhaseDuration("phase-2", 90);
                setPhaseState(PhaseType.PHASE_2, dur);
                border.setSize(plugin.getConfigManager().getPhaseBorderSize("phase-2", 250.0), dur);
                notifyPhaseChange(dur);
                checkTwoPlayersAcceleration();
            }
            case PHASE_3 -> {
                int dur = plugin.getConfigManager().getPhaseDuration("phase-3", 90);
                setPhaseState(PhaseType.PHASE_3, dur);
                border.setSize(plugin.getConfigManager().getPhaseBorderSize("phase-3", 100.0), dur);
                notifyPhaseChange(dur);
                checkTwoPlayersAcceleration();
            }
            case DEATHMATCH -> {
                int dur = plugin.getConfigManager().getPhaseDuration("deathmatch", 180);
                setPhaseState(PhaseType.DEATHMATCH, dur);
                plugin.getMatchManager().setState(GameState.DEATHMATCH);
                plugin.getDeathmatchManager().startDeathmatch(world);
            }
            default -> {}
        }
    }

    public void checkTwoPlayersAcceleration() {
        if ((currentPhase == PhaseType.PHASE_2 || currentPhase == PhaseType.PHASE_3)
                && plugin.getMatchManager().getAlivePlayers().size() == 2) {
            this.currentPhase = PhaseType.PHASE_3;
            if (this.timeLeft > 15) {
                this.timeLeft = 15;
                this.totalDuration = 15;
            }
        }
    }

    private void setPhaseState(PhaseType phase, int duration) {
        this.currentPhase = phase;
        this.totalDuration = Math.max(1, duration);
        this.timeLeft = duration;
        updateBossBars();
    }

    private void notifyPhaseChange(int duration) {
        for (UUID uuid : plugin.getMatchManager().getMatchPlayers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                Language lang = plugin.getLocaleManager().getLanguage(p);
                String phaseName = plugin.getLocaleManager().getRaw(lang, "phases.names." + currentPhase.name());
                plugin.getLocaleManager().sendMessage(p, "phases.phase-change-chat",
                        "<phase_name>", phaseName,
                        "<duration>", String.valueOf(duration));
                plugin.getLocaleManager().sendTitle(p, "phases.phase-change-title", 200, 2000, 400,
                        "<phase_name>", phaseName);
                p.playSound(p.getLocation(), Sound.ENTITY_ENDER_DRAGON_GROWL, 0.6f, 1.2f);
            }
        }
    }

    private void updateBossBars() {
        if (bossBarEN == null || bossBarVI == null) return;
        float progress = Math.clamp((float) timeLeft / totalDuration, 0.0f, 1.0f);
        String timeFormatted = String.format("%02d:%02d", timeLeft / 60, timeLeft % 60);
        String aliveStr = String.valueOf(plugin.getMatchManager().getAlivePlayers().size());

        String phaseEN = plugin.getLocaleManager().getRaw(Language.EN, "phases.names." + currentPhase.name());
        String phaseVI = plugin.getLocaleManager().getRaw(Language.VI, "phases.names." + currentPhase.name());

        bossBarEN.name(plugin.getLocaleManager().getComponent(Language.EN, "phases.bossbar-format",
                "<phase_name>", phaseEN, "<time>", timeFormatted, "<alive>", aliveStr));
        bossBarEN.progress(progress);

        bossBarVI.name(plugin.getLocaleManager().getComponent(Language.VI, "phases.bossbar-format",
                "<phase_name>", phaseVI, "<time>", timeFormatted, "<alive>", aliveStr));
        bossBarVI.progress(progress);
    }

    public void assignBossBar(Player player) {
        if (bossBarEN == null || bossBarVI == null) return;
        player.hideBossBar(bossBarEN);
        player.hideBossBar(bossBarVI);
        if (plugin.getLocaleManager().getLanguage(player) == Language.VI) {
            player.showBossBar(bossBarVI);
        } else {
            player.showBossBar(bossBarEN);
        }
    }

    public void switchPlayerBossBarLanguage(Player player) {
        if (plugin.getMatchManager().isInActiveMatch(player)) {
            assignBossBar(player);
        }
    }

    public void removePlayerBossBar(Player player) {
        if (bossBarEN != null) player.hideBossBar(bossBarEN);
        if (bossBarVI != null) player.hideBossBar(bossBarVI);
    }

    public void stop() {
        if (task != null) {
            task.cancel();
            task = null;
        }
        for (Player p : Bukkit.getOnlinePlayers()) {
            removePlayerBossBar(p);
        }
        bossBarEN = null;
        bossBarVI = null;
    }

    public PhaseType getCurrentPhase() {
        return currentPhase;
    }
}
