package me.nhan.cartbr.arena;

import com.infernalsuite.asp.api.AdvancedSlimePaperAPI;
import com.infernalsuite.asp.api.loaders.SlimeLoader;
import com.infernalsuite.asp.api.world.SlimeWorld;
import com.infernalsuite.asp.api.world.SlimeWorldInstance;
import com.infernalsuite.asp.api.world.properties.SlimePropertyMap;
import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.*;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

public class SlimeArenaManager {

    private final CartBRPlugin plugin;
    private World currentArenaWorld;
    private SlimeLoader cachedLoader;
    private SlimePropertyMap cachedPropertyMap;
    private final Set<Long> forceLoadedChunkKeys = new HashSet<>();

    public SlimeArenaManager(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    public CompletableFuture<World> attachOrLoadSingleWorld() {
        String worldName = plugin.getConfigManager().getTemplateWorldName();
        World existingBukkit = Bukkit.getWorld(worldName);

        if (existingBukkit != null) {
            cacheSlimeMetadata(worldName);
            configureWorldRules(existingBukkit);
            this.currentArenaWorld = existingBukkit;
            return CompletableFuture.completedFuture(existingBukkit);
        }

        return reloadSingleWorld();
    }

    public CompletableFuture<World> reloadSingleWorld() {
        CompletableFuture<World> future = new CompletableFuture<>();
        String worldName = plugin.getConfigManager().getTemplateWorldName();

        releaseForceLoadedChunks();
        cacheSlimeMetadata(worldName);

        World oldWorld = Bukkit.getWorld(worldName);
        if (oldWorld != null) {
            Location lobby = plugin.getConfigManager().getLobbySpawn();
            oldWorld.getPlayers().forEach(p -> p.teleport(lobby));
            boolean unloaded = Bukkit.unloadWorld(oldWorld, false);
            if (!unloaded) {
                plugin.getLogger().warning("Could not unload world " + worldName + " immediately, retrying...");
            }
            this.currentArenaWorld = null;
        }

        if (cachedLoader != null) {
            SlimePropertyMap props = cachedPropertyMap != null ? cachedPropertyMap : new SlimePropertyMap();
            Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
                try {
                    AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
                    SlimeWorld freshSlimeWorld = asp.readWorld(cachedLoader, worldName, true, props);

                    Bukkit.getScheduler().runTask(plugin, () -> {
                        try {
                            SlimeWorldInstance instance = asp.loadWorld(freshSlimeWorld, true);
                            World bukkitWorld = instance != null ? instance.getBukkitWorld() : Bukkit.getWorld(worldName);
                            if (bukkitWorld != null) {
                                configureWorldRules(bukkitWorld);
                                this.currentArenaWorld = bukkitWorld;
                                future.complete(bukkitWorld);
                            } else {
                                loadViaSwpCommandFallback(worldName, future);
                            }
                        } catch (Exception loadEx) {
                            plugin.getLogger().warning("ASP loadWorld fallback triggered: " + loadEx.getMessage());
                            loadViaSwpCommandFallback(worldName, future);
                        }
                    });
                } catch (Exception readEx) {
                    plugin.getLogger().warning("ASP readWorld fallback triggered: " + readEx.getMessage());
                    Bukkit.getScheduler().runTask(plugin, () -> loadViaSwpCommandFallback(worldName, future));
                }
            });
        } else {
            loadViaSwpCommandFallback(worldName, future);
        }

        return future;
    }

    private void cacheSlimeMetadata(String worldName) {
        try {
            AdvancedSlimePaperAPI asp = AdvancedSlimePaperAPI.instance();
            SlimeWorldInstance loadedInstance = asp.getLoadedWorld(worldName);
            if (loadedInstance != null) {
                SlimeWorld copy = loadedInstance.getSerializableCopy();
                if (copy != null) {
                    if (copy.getLoader() != null) this.cachedLoader = copy.getLoader();
                    if (copy.getPropertyMap() != null) this.cachedPropertyMap = copy.getPropertyMap();
                }
            }
        } catch (Throwable ignored) {}
    }

    private void loadViaSwpCommandFallback(String worldName, CompletableFuture<World> future) {
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "swp load " + worldName);
        new BukkitRunnable() {
            int attempts = 0;

            @Override
            public void run() {
                attempts++;
                World loaded = Bukkit.getWorld(worldName);
                if (loaded != null) {
                    cancel();
                    cacheSlimeMetadata(worldName);
                    configureWorldRules(loaded);
                    currentArenaWorld = loaded;
                    future.complete(loaded);
                } else if (attempts >= 20) {
                    cancel();
                    future.completeExceptionally(new IllegalStateException("Timeout waiting for SlimeWorld: " + worldName));
                }
            }
        }.runTaskTimer(plugin, 5L, 10L);
    }

    private void configureWorldRules(World world) {
        world.setAutoSave(false);
        world.setDifficulty(Difficulty.NORMAL);
        world.setGameRule(GameRule.DO_IMMEDIATE_RESPAWN, true);
        world.setGameRule(GameRule.ANNOUNCE_ADVANCEMENTS, false);
        world.setGameRule(GameRule.DO_MOB_SPAWNING, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.FALL_DAMAGE, true);
        world.setTime(6000L);

        WorldBorder border = world.getWorldBorder();
        border.setCenter(plugin.getConfigManager().getIslandCenterX(), plugin.getConfigManager().getIslandCenterZ());
        border.setSize(plugin.getConfigManager().getPhaseBorderSize("phase-1", 450.0));

        for (Chunk chunk : world.getLoadedChunks()) {
            plugin.getHologramManager().scanChunkForChests(chunk);
        }
    }

    public void preloadFlightPathChunks(Location p1, Location p2) {
        if (currentArenaWorld == null) return;
        int c1x = p1.getBlockX() >> 4;
        int c1z = p1.getBlockZ() >> 4;
        int c2x = p2.getBlockX() >> 4;
        int c2z = p2.getBlockZ() >> 4;

        int steps = Math.max(1, Math.max(Math.abs(c2x - c1x), Math.abs(c2z - c1z)));

        Set<Long> targetChunks = new HashSet<>();
        for (int i = 0; i <= steps; i++) {
            double t = (double) i / steps;
            int cx = (int) Math.round(c1x + (c2x - c1x) * t);
            int cz = (int) Math.round(c1z + (c2z - c1z) * t);

            for (int dx = -2; dx <= 2; dx++) {
                for (int dz = -2; dz <= 2; dz++) {
                    long key = (((long) (cx + dx)) << 32) | ((cz + dz) & 0xffffffffL);
                    targetChunks.add(key);
                }
            }
        }

        World targetWorld = this.currentArenaWorld;
        for (long key : targetChunks) {
            int cx = (int) (key >> 32);
            int cz = (int) key;
            targetWorld.getChunkAtAsync(cx, cz).thenAccept(chunk -> {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    if (currentArenaWorld != null && currentArenaWorld.equals(targetWorld)) {
                        chunk.setForceLoaded(true);
                        forceLoadedChunkKeys.add(key);
                        plugin.getHologramManager().scanChunkForChests(chunk);
                    }
                });
            });
        }
    }

    public void releaseForceLoadedChunks() {
        if (currentArenaWorld != null) {
            for (long key : forceLoadedChunkKeys) {
                int cx = (int) (key >> 32);
                int cz = (int) key;
                currentArenaWorld.setChunkForceLoaded(cx, cz, false);
            }
        }
        forceLoadedChunkKeys.clear();
    }

    public World getCurrentArenaWorld() {
        return currentArenaWorld;
    }
}
