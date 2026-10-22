package me.nhan.cartbr;

import me.nhan.cartbr.arena.MatchManager;
import me.nhan.cartbr.arena.SlimeArenaManager;
import me.nhan.cartbr.commands.CartBRCommand;
import me.nhan.cartbr.combat.CartCombatListener;
import me.nhan.cartbr.combat.DeathmatchManager;
import me.nhan.cartbr.combat.PhaseScheduler;
import me.nhan.cartbr.config.ConfigManager;
import me.nhan.cartbr.config.LootConfig;
import me.nhan.cartbr.drop.ParachuteManager;
import me.nhan.cartbr.gui.HotbarManager;
import me.nhan.cartbr.gui.SettingsGUI;
import me.nhan.cartbr.locale.LocaleManager;
import me.nhan.cartbr.loot.ChestHologramManager;
import me.nhan.cartbr.loot.LootManager;
import org.bukkit.Bukkit;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class CartBRPlugin extends JavaPlugin {

    private static CartBRPlugin instance;

    private ConfigManager configManager;
    private LootConfig lootConfig;
    private LocaleManager localeManager;
    private HotbarManager hotbarManager;
    private SlimeArenaManager slimeArenaManager;
    private ChestHologramManager hologramManager;
    private LootManager lootManager;
    private ParachuteManager parachuteManager;
    private PhaseScheduler phaseScheduler;
    private DeathmatchManager deathmatchManager;
    private MatchManager matchManager;
    private CartCombatListener combatListener;

    @Override
    public void onEnable() {
        instance = this;

        this.configManager = new ConfigManager(this);
        this.lootConfig = new LootConfig(this);
        this.localeManager = new LocaleManager(this);

        this.hotbarManager = new HotbarManager(this);
        this.slimeArenaManager = new SlimeArenaManager(this);
        this.hologramManager = new ChestHologramManager(this);
        this.lootManager = new LootManager(this);
        this.parachuteManager = new ParachuteManager(this);
        this.deathmatchManager = new DeathmatchManager(this);
        this.phaseScheduler = new PhaseScheduler(this);
        this.matchManager = new MatchManager(this);
        this.combatListener = new CartCombatListener(this);

        var pm = Bukkit.getPluginManager();
        pm.registerEvents(hotbarManager, this);
        pm.registerEvents(new SettingsGUI.Listener(this), this);
        pm.registerEvents(parachuteManager, this);
        pm.registerEvents(hologramManager, this);
        pm.registerEvents(lootManager, this);
        pm.registerEvents(combatListener, this);
        pm.registerEvents(deathmatchManager, this);
        pm.registerEvents(matchManager, this);

        PluginCommand cmd = getCommand("cartbr");
        if (cmd != null) {
            CartBRCommand executor = new CartBRCommand(this);
            cmd.setExecutor(executor);
            cmd.setTabCompleter(executor);
        }

        Bukkit.getScheduler().runTaskLater(this, () -> {
            matchManager.initializeFirstArena();
        }, 20L);

        getLogger().info("CartBR enabled successfully (Single-Map SWM Load/Unload Mode).");
    }

    @Override
    public void onDisable() {
        if (phaseScheduler != null) phaseScheduler.stop();
        if (deathmatchManager != null) deathmatchManager.reset();
        if (parachuteManager != null) parachuteManager.cleanupAll();
        if (hologramManager != null) hologramManager.removeAllHolograms();
        if (slimeArenaManager != null) slimeArenaManager.releaseForceLoadedChunks();
        getLogger().info("CartBR disabled cleanly.");
    }

    public static CartBRPlugin getInstance() { return instance; }
    public ConfigManager getConfigManager() { return configManager; }
    public LootConfig getLootConfig() { return lootConfig; }
    public LocaleManager getLocaleManager() { return localeManager; }
    public HotbarManager getHotbarManager() { return hotbarManager; }
    public SlimeArenaManager getSlimeArenaManager() { return slimeArenaManager; }
    public ChestHologramManager getHologramManager() { return hologramManager; }
    public LootManager getLootManager() { return lootManager; }
    public ParachuteManager getParachuteManager() { return parachuteManager; }
    public PhaseScheduler getPhaseScheduler() { return phaseScheduler; }
    public DeathmatchManager getDeathmatchManager() { return deathmatchManager; }
    public MatchManager getMatchManager() { return matchManager; }
}
