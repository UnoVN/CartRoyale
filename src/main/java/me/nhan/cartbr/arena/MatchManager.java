package me.nhan.cartbr.arena;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.drop.CustomDragonTask;
import org.bukkit.*;
import org.bukkit.entity.Firework;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class MatchManager implements Listener {

    private final CartBRPlugin plugin;
    private GameState state = GameState.RESETTING;

    private final Set<UUID> matchPlayers = new LinkedHashSet<>();
    private final Set<UUID> alivePlayers = new LinkedHashSet<>();
    private final Map<UUID, Integer> killCounts = new HashMap<>();

    private BukkitTask countdownTask;
    private CustomDragonTask dragonTask;

    public MatchManager(CartBRPlugin plugin) {
        this.plugin = plugin;
        startWaitingActionBarLoop();
    }

    public void initializeFirstArena() {
        this.state = GameState.RESETTING;
        plugin.getLootManager().resetOpenedChests();
        plugin.getHologramManager().removeAllHolograms();

        plugin.getSlimeArenaManager().attachOrLoadSingleWorld().whenComplete((world, ex) -> {
            if (ex != null || world == null) {
                plugin.getLogger().severe("Failed to load arena world: " + (ex != null ? ex.getMessage() : "unknown"));
                return;
            }
            this.state = GameState.WAITING;
            plugin.getLogger().info("Arena world '" + world.getName() + "' attached and ready!");
        });
    }

    public void resetSingleArena() {
        this.state = GameState.RESETTING;
        this.matchPlayers.clear();
        this.alivePlayers.clear();
        this.killCounts.clear();

        plugin.getDeathmatchManager().reset();
        plugin.getLootManager().resetOpenedChests();
        plugin.getHologramManager().removeAllHolograms();

        plugin.getSlimeArenaManager().reloadSingleWorld().whenComplete((world, ex) -> {
            if (ex != null || world == null) {
                plugin.getLogger().severe("Failed to reload Slime arena world: " + (ex != null ? ex.getMessage() : "unknown"));
                return;
            }
            this.state = GameState.WAITING;
            plugin.getLogger().info("Arena world '" + world.getName() + "' has been unloaded & reloaded fresh on RAM!");
        });
    }

    private void startWaitingActionBarLoop() {
        new BukkitRunnable() {
            @Override
            public void run() {
                if (state == GameState.WAITING && !matchPlayers.isEmpty()) {
                    String countStr = String.valueOf(matchPlayers.size());
                    for (UUID uuid : matchPlayers) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            plugin.getLocaleManager().sendActionBar(p, "queue.waiting-actionbar", "<current>", countStr);
                        }
                    }
                }
            }
        }.runTaskTimer(plugin, 20L, 20L);
    }

    public void joinQueue(Player player) {
        if (state != GameState.WAITING && state != GameState.STARTING) {
            player.sendMessage(ChatColor.RED + "Match is currently in progress or resetting. Please wait!");
            return;
        }
        if (matchPlayers.contains(player.getUniqueId())) return;
        if (matchPlayers.size() >= plugin.getConfigManager().getMaxPlayers()) {
            player.sendMessage(ChatColor.RED + "Queue is full!");
            return;
        }

        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        if (arenaWorld == null) {
            player.sendMessage(ChatColor.RED + "Arena world is still loading!");
            return;
        }

        matchPlayers.add(player.getUniqueId());
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFallDistance(0f);
        player.teleport(plugin.getConfigManager().getWaitingCageLocation(arenaWorld));
        plugin.getHotbarManager().giveWaitingItems(player);

        String currentStr = String.valueOf(matchPlayers.size());
        String maxStr = String.valueOf(plugin.getConfigManager().getMaxPlayers());
        for (UUID uuid : matchPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getLocaleManager().sendMessage(p, "queue.joined", "<current>", currentStr, "<max>", maxStr);
            }
        }

        if (state == GameState.WAITING && matchPlayers.size() >= plugin.getConfigManager().getMinPlayers()) {
            startCountdown();
        }
    }

    public void leaveQueue(Player player) {
        UUID uuid = player.getUniqueId();
        if (!matchPlayers.remove(uuid)) return;

        if (dragonTask != null) {
            dragonTask.revealPlayer(player);
        }

        boolean wasAlive = alivePlayers.remove(uuid);
        if (wasAlive && (state == GameState.DRAGON_DROP || state == GameState.INGAME || state == GameState.DEATHMATCH)) {
            plugin.getParachuteManager().removeParachute(player);
            Location dropLoc = player.getLocation();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR && !plugin.getParachuteManager().isParachuteItem(item)) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, item);
                }
            }
            player.getInventory().clear();
        }

        resetPlayerToLobby(player);
        plugin.getLocaleManager().sendMessage(player, "queue.left");

        if (state == GameState.STARTING && matchPlayers.size() < plugin.getConfigManager().getMinPlayers()) {
            cancelCountdown();
        } else if (wasAlive) {
            checkWinOrAccelerateCondition();
        }
    }

    public void startCountdown() {
        this.state = GameState.STARTING;
        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        if (arenaWorld != null) {
            plugin.getSlimeArenaManager().preloadFlightPathChunks(
                    plugin.getConfigManager().getDragonP1(arenaWorld),
                    plugin.getConfigManager().getDragonP2(arenaWorld)
            );
        }

        if (countdownTask != null) countdownTask.cancel();
        countdownTask = new BukkitRunnable() {
            int timeLeft = plugin.getConfigManager().getCountdownSeconds();

            @Override
            public void run() {
                if (timeLeft <= 0) {
                    cancel();
                    startDragonDropPhase();
                    return;
                }

                if (timeLeft == 15 || timeLeft == 10 || timeLeft <= 5) {
                    String secStr = String.valueOf(timeLeft);
                    for (UUID uuid : matchPlayers) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            plugin.getLocaleManager().sendMessage(p, "queue.countdown-chat", "<seconds>", secStr);
                            plugin.getLocaleManager().sendTitle(p, "queue.countdown-title", 100, 700, 200, "<seconds>", secStr);
                            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_HAT, 1f, 1.2f);
                        }
                    }
                }
                timeLeft--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void cancelCountdown() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        this.state = GameState.WAITING;
        for (UUID uuid : matchPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getLocaleManager().sendMessage(p, "queue.countdown-cancelled");
                p.playSound(p.getLocation(), Sound.ENTITY_VILLAGER_NO, 1f, 1f);
            }
        }
    }

    public void startDragonDropPhase() {
        if (countdownTask != null) {
            countdownTask.cancel();
            countdownTask = null;
        }
        this.state = GameState.DRAGON_DROP;
        this.alivePlayers.clear();
        this.alivePlayers.addAll(matchPlayers);

        List<Player> onlinePlayers = new ArrayList<>();
        for (UUID uuid : alivePlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                killCounts.put(uuid, 0);
                p.getInventory().clear();
                p.setGameMode(GameMode.SURVIVAL);
                p.setHealth(20.0);
                p.setFoodLevel(20);
                p.setSaturation(20f);
                plugin.getLocaleManager().sendMessage(p, "drop-phase.dragon-start-chat");
                onlinePlayers.add(p);
            }
        }

        plugin.getPhaseScheduler().start(onlinePlayers);

        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        this.dragonTask = new CustomDragonTask(plugin, arenaWorld, onlinePlayers);
        this.dragonTask.start();
    }

    public void onDragonFlightFinished() {
        this.dragonTask = null;
        plugin.getSlimeArenaManager().releaseForceLoadedChunks();
        if (state == GameState.DRAGON_DROP) {
            this.state = GameState.INGAME;
            plugin.getPhaseScheduler().advanceToPhase(PhaseType.PHASE_1);
        }
    }

    public void eliminatePlayer(Player victim, Player killer, boolean byCart) {
        if (!alivePlayers.remove(victim.getUniqueId())) return;

        int aliveCount = alivePlayers.size();
        if (killer != null && !killer.getUniqueId().equals(victim.getUniqueId())) {
            killCounts.merge(killer.getUniqueId(), 1, Integer::sum);
            String msgKey = byCart ? "combat.kill-cart" : "combat.kill-melee";
            broadcastToMatch(msgKey,
                    "<killer>", killer.getName(),
                    "<victim>", victim.getName(),
                    "<alive>", String.valueOf(aliveCount));
        } else {
            broadcastToMatch("combat.kill-melee",
                    "<killer>", "Environment",
                    "<victim>", victim.getName(),
                    "<alive>", String.valueOf(aliveCount));
        }

        checkWinOrAccelerateCondition();
    }

    public void checkWinOrAccelerateCondition() {
        if (state != GameState.DRAGON_DROP && state != GameState.INGAME && state != GameState.DEATHMATCH) return;

        if (alivePlayers.size() <= 1) {
            Player winner = alivePlayers.stream().map(Bukkit::getPlayer).filter(Objects::nonNull).findFirst().orElse(null);
            endMatch(winner);
            return;
        }

        if (alivePlayers.size() == 2 && state == GameState.INGAME) {
            plugin.getPhaseScheduler().checkTwoPlayersAcceleration();
        }
    }

    public void endMatch(Player winner) {
        if (state == GameState.ENDING || state == GameState.RESETTING) return;
        this.state = GameState.ENDING;

        if (dragonTask != null) {
            dragonTask.cleanup();
            dragonTask = null;
        }
        plugin.getPhaseScheduler().stop();
        plugin.getDeathmatchManager().reset();
        plugin.getParachuteManager().cleanupAll();

        String winnerName = winner != null ? winner.getName() : "None";
        int kills = winner != null ? killCounts.getOrDefault(winner.getUniqueId(), 0) : 0;

        for (UUID uuid : matchPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getLocaleManager().sendTitle(p, "ending.victory-title", 200, 4000, 500, "<winner>", winnerName);
                plugin.getLocaleManager().sendMessage(p, "ending.victory-broadcast",
                        "<winner>", winnerName,
                        "<kills>", String.valueOf(kills));
                p.playSound(p.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
            }
        }

        new BukkitRunnable() {
            int seconds = 0;

            @Override
            public void run() {
                if (seconds >= 5) {
                    cancel();
                    for (UUID uuid : new HashSet<>(matchPlayers)) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) resetPlayerToLobby(p);
                    }
                    resetSingleArena();
                    return;
                }
                if (winner != null && winner.isOnline()) {
                    spawnVictoryFirework(winner.getLocation());
                }
                seconds++;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    private void spawnVictoryFirework(Location loc) {
        Firework fw = loc.getWorld().spawn(loc, Firework.class);
        FireworkMeta meta = fw.getFireworkMeta();
        meta.addEffect(FireworkEffect.builder()
                .withColor(Color.fromRGB(0xcba6f7), Color.fromRGB(0xf9e2af))
                .withFade(Color.fromRGB(0xf38ba8))
                .with(FireworkEffect.Type.BALL_LARGE)
                .withTrail()
                .build());
        meta.setPower(1);
        fw.setFireworkMeta(meta);
    }

    public void resetPlayerToLobby(Player player) {
        for (Player other : Bukkit.getOnlinePlayers()) {
            player.showPlayer(plugin, other);
            other.showPlayer(plugin, player);
        }
        plugin.getParachuteManager().removeParachute(player);
        plugin.getPhaseScheduler().removePlayerBossBar(player);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.setGameMode(GameMode.ADVENTURE);
        player.setHealth(20.0);
        player.setFoodLevel(20);
        player.setFallDistance(0f);
        player.teleport(plugin.getConfigManager().getLobbySpawn());
        plugin.getHotbarManager().giveLobbyItems(player);
    }

    public void broadcastToMatch(String path, String... placeholders) {
        for (UUID uuid : matchPlayers) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) {
                plugin.getLocaleManager().sendMessage(p, path, placeholders);
            }
        }
    }

    @EventHandler
    public void onDamageInNonCombatStates(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player p)) return;
        if (state == GameState.WAITING || state == GameState.STARTING || state == GameState.ENDING) {
            if (matchPlayers.contains(p.getUniqueId())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onCombatLog(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();

        if (state == GameState.WAITING || state == GameState.STARTING) {
            leaveQueue(player);
            return;
        }

        if (dragonTask != null) {
            dragonTask.revealPlayer(player);
        }

        if (alivePlayers.contains(uuid)) {
            plugin.getParachuteManager().removeParachute(player);

            Location dropLoc = player.getLocation();
            for (ItemStack item : player.getInventory().getContents()) {
                if (item != null && item.getType() != Material.AIR && !plugin.getParachuteManager().isParachuteItem(item)) {
                    dropLoc.getWorld().dropItemNaturally(dropLoc, item);
                }
            }
            player.getInventory().clear();
            alivePlayers.remove(uuid);
            matchPlayers.remove(uuid);
            checkWinOrAccelerateCondition();
        } else {
            matchPlayers.remove(uuid);
        }
    }

    public GameState getState() { return state; }
    public void setState(GameState state) { this.state = state; }
    public Set<UUID> getMatchPlayers() { return matchPlayers; }
    public Set<UUID> getAlivePlayers() { return alivePlayers; }
    public boolean isInQueue(Player p) { return (state == GameState.WAITING || state == GameState.STARTING) && matchPlayers.contains(p.getUniqueId()); }
    public boolean isInActiveMatch(Player p) { return state != GameState.WAITING && state != GameState.STARTING && matchPlayers.contains(p.getUniqueId()); }
    public CustomDragonTask getDragonTask() { return dragonTask; }
}
