package me.nhan.cartbr.config;

import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfigManager {

    private final CartBRPlugin plugin;

    public ConfigManager(CartBRPlugin plugin) {
        this.plugin = plugin;
        plugin.saveDefaultConfig();
    }

    public void reload() {
        plugin.reloadConfig();
    }

    private FileConfiguration cfg() {
        return plugin.getConfig();
    }

    public String getTemplateWorldName() {
        return cfg().getString("settings.template-world", "cartbr_template");
    }

    public int getMinPlayers() {
        return cfg().getInt("settings.min-players", 3);
    }

    public void setMinPlayers(int minPlayers) {
        cfg().set("settings.min-players", minPlayers);
        plugin.saveConfig();
    }

    public int getMaxPlayers() {
        return cfg().getInt("settings.max-players", 16);
    }

    public int getCountdownSeconds() {
        return cfg().getInt("settings.countdown-seconds", 15);
    }

    public int getTntMinecartMaxStack() {
        return cfg().getInt("settings.tnt-minecart-max-stack", 16);
    }

    public int getMaxCartsPerWindow() {
        return cfg().getInt("settings.anti-cart-spam.max-carts", 4);
    }

    public long getCartSpamWindowMs() {
        return cfg().getLong("settings.anti-cart-spam.time-window-ms", 1500L);
    }

    public String getDragonModelId() {
        return cfg().getString("hooks.dragon-model-id", "cartbr_dragon");
    }

    public String getDragonAnimation() {
        return cfg().getString("hooks.dragon-animation", "fly");
    }

    public String getNexoParachuteId() {
        return cfg().getString("hooks.nexo-items.parachute-helmet", "cartbr_parachute");
    }

    public String getNexoJoinItemId() {
        return cfg().getString("hooks.nexo-items.join-item", "cartbr_play_icon");
    }

    public String getNexoSettingsItemId() {
        return cfg().getString("hooks.nexo-items.settings-item", "cartbr_settings_icon");
    }

    public String getNexoLeaveItemId() {
        return cfg().getString("hooks.nexo-items.leave-item", "cartbr_leave_icon");
    }

    public Location getLobbySpawn() {
        String worldName = cfg().getString("lobby-spawn.world", "world");
        World world = Bukkit.getWorld(worldName);
        if (world == null && !Bukkit.getWorlds().isEmpty()) {
            world = Bukkit.getWorlds().getFirst();
        }
        return new Location(
                world,
                cfg().getDouble("lobby-spawn.x", 0.5),
                cfg().getDouble("lobby-spawn.y", 100.0),
                cfg().getDouble("lobby-spawn.z", 0.5),
                (float) cfg().getDouble("lobby-spawn.yaw", 0.0),
                (float) cfg().getDouble("lobby-spawn.pitch", 0.0)
        );
    }

    public void setLobbySpawn(Location loc) {
        cfg().set("lobby-spawn.world", loc.getWorld().getName());
        cfg().set("lobby-spawn.x", round(loc.getX()));
        cfg().set("lobby-spawn.y", round(loc.getY()));
        cfg().set("lobby-spawn.z", round(loc.getZ()));
        cfg().set("lobby-spawn.yaw", round(loc.getYaw()));
        cfg().set("lobby-spawn.pitch", round(loc.getPitch()));
        plugin.saveConfig();
    }

    public Location getWaitingCageLocation(World arenaWorld) {
        return new Location(
                arenaWorld,
                cfg().getDouble("arena.waiting-cage.x", 0.5),
                cfg().getDouble("arena.waiting-cage.y", 200.0),
                cfg().getDouble("arena.waiting-cage.z", 0.5),
                (float) cfg().getDouble("arena.waiting-cage.yaw", 0.0),
                (float) cfg().getDouble("arena.waiting-cage.pitch", 0.0)
        );
    }

    public void setWaitingCage(Location loc) {
        cfg().set("arena.waiting-cage.x", round(loc.getX()));
        cfg().set("arena.waiting-cage.y", round(loc.getY()));
        cfg().set("arena.waiting-cage.z", round(loc.getZ()));
        cfg().set("arena.waiting-cage.yaw", round(loc.getYaw()));
        cfg().set("arena.waiting-cage.pitch", round(loc.getPitch()));
        plugin.saveConfig();
    }

    public Location getDragonP1(World arenaWorld) {
        return new Location(
                arenaWorld,
                cfg().getDouble("arena.dragon-path.p1.x", -200.0),
                cfg().getDouble("arena.dragon-path.p1.y", 145.0),
                cfg().getDouble("arena.dragon-path.p1.z", -200.0)
        );
    }

    public void setDragonP1(Location loc) {
        cfg().set("arena.dragon-path.p1.x", round(loc.getX()));
        cfg().set("arena.dragon-path.p1.y", round(loc.getY()));
        cfg().set("arena.dragon-path.p1.z", round(loc.getZ()));
        plugin.saveConfig();
    }

    public Location getDragonP2(World arenaWorld) {
        return new Location(
                arenaWorld,
                cfg().getDouble("arena.dragon-path.p2.x", 200.0),
                cfg().getDouble("arena.dragon-path.p2.y", 145.0),
                cfg().getDouble("arena.dragon-path.p2.z", 200.0)
        );
    }

    public void setDragonP2(Location loc) {
        cfg().set("arena.dragon-path.p2.x", round(loc.getX()));
        cfg().set("arena.dragon-path.p2.y", round(loc.getY()));
        cfg().set("arena.dragon-path.p2.z", round(loc.getZ()));
        plugin.saveConfig();
    }

    public double getDragonSpeed() {
        return cfg().getDouble("arena.dragon-path.speed-blocks-per-tick", 0.85);
    }

    public double getIslandCenterX() {
        return cfg().getDouble("arena.island-center.x", 0.5);
    }

    public double getIslandCenterZ() {
        return cfg().getDouble("arena.island-center.z", 0.5);
    }

    public void setIslandCenter(Location loc) {
        cfg().set("arena.island-center.x", round(loc.getX()));
        cfg().set("arena.island-center.z", round(loc.getZ()));
        plugin.saveConfig();
    }

    public int getPhaseDuration(String key, int def) {
        return cfg().getInt("arena.phases." + key + ".duration", def);
    }

    public double getPhaseBorderSize(String key, double def) {
        return cfg().getDouble("arena.phases." + key + ".border-size", def);
    }

    public double getDeathmatchCenterX() {
        return cfg().getDouble("arena.deathmatch.center-x", 2000.5);
    }

    public double getDeathmatchCenterZ() {
        return cfg().getDouble("arena.deathmatch.center-z", 2000.5);
    }

    public void setDeathmatchCenter(Location loc) {
        cfg().set("arena.deathmatch.center-x", round(loc.getX()));
        cfg().set("arena.deathmatch.center-z", round(loc.getZ()));
        plugin.saveConfig();
    }

    public int getDeathmatchFloorY() {
        return cfg().getInt("arena.deathmatch.floor-y", 99);
    }

    public void setDeathmatchFloorY(int floorY) {
        cfg().set("arena.deathmatch.floor-y", floorY);
        plugin.saveConfig();
    }

    public int getDeathmatchFreezeSeconds() {
        return cfg().getInt("arena.deathmatch.freeze-seconds", 5);
    }

    public double getDeathmatchInitialBorder() {
        return cfg().getDouble("arena.phases.deathmatch.initial-border", 40.0);
    }

    public double getDeathmatchFinalBorder() {
        return cfg().getDouble("arena.phases.deathmatch.final-border", 10.0);
    }

    public long getDeathmatchShrinkDuration() {
        return cfg().getLong("arena.phases.deathmatch.shrink-duration", 120L);
    }

    public List<String> getRawDeathmatchSpawns() {
        return cfg().getStringList("arena.deathmatch.spawns");
    }

    public int addDeathmatchSpawn(Location loc) {
        List<String> spawns = new ArrayList<>(getRawDeathmatchSpawns());
        String serialized = String.format(Locale.US, "%.1f,%.1f,%.1f,%.1f,%.1f",
                loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        spawns.add(serialized);
        cfg().set("arena.deathmatch.spawns", spawns);
        plugin.saveConfig();
        return spawns.size();
    }

    public boolean removeLastDeathmatchSpawn() {
        List<String> spawns = new ArrayList<>(getRawDeathmatchSpawns());
        if (spawns.isEmpty()) return false;
        spawns.removeLast();
        cfg().set("arena.deathmatch.spawns", spawns);
        plugin.saveConfig();
        return true;
    }

    public void clearDeathmatchSpawns() {
        cfg().set("arena.deathmatch.spawns", new ArrayList<String>());
        plugin.saveConfig();
    }

    public List<Location> getDeathmatchSpawns(World arenaWorld) {
        List<String> rawList = getRawDeathmatchSpawns();
        List<Location> spawns = new ArrayList<>();
        for (String line : rawList) {
            String[] parts = line.split(",");
            if (parts.length >= 3) {
                double x = Double.parseDouble(parts[0].trim());
                double y = Double.parseDouble(parts[1].trim());
                double z = Double.parseDouble(parts[2].trim());
                float yaw = parts.length >= 4 ? Float.parseFloat(parts[3].trim()) : 0f;
                float pitch = parts.length >= 5 ? Float.parseFloat(parts[4].trim()) : 0f;
                spawns.add(new Location(arenaWorld, x, y, z, yaw, pitch));
            }
        }
        if (spawns.isEmpty()) {
            spawns.add(new Location(arenaWorld, getDeathmatchCenterX(), getDeathmatchFloorY() + 1, getDeathmatchCenterZ()));
        }
        return spawns;
    }

    private double round(double val) {
        return Math.round(val * 10.0) / 10.0;
    }
}
