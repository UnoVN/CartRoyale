package me.nhan.cartbr.combat;

import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.*;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.SpectralArrow;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityPlaceEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.scheduler.BukkitTask;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class DeathmatchManager implements Listener {

    private final CartBRPlugin plugin;
    private boolean isDeathmatchFrozen = false;
    private BukkitTask freezeTask;

    public DeathmatchManager(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    public void startDeathmatch(World world) {
        reset();

        for (Entity entity : world.getEntities()) {
            if (entity instanceof ExplosiveMinecart || entity instanceof Arrow || entity instanceof SpectralArrow) {
                entity.remove();
            }
        }
        plugin.getParachuteManager().cleanupAll();

        WorldBorder border = world.getWorldBorder();
        border.setCenter(plugin.getConfigManager().getDeathmatchCenterX(), plugin.getConfigManager().getDeathmatchCenterZ());
        border.setSize(plugin.getConfigManager().getDeathmatchInitialBorder());

        List<Location> spawns = plugin.getConfigManager().getDeathmatchSpawns(world);
        List<UUID> survivors = new ArrayList<>(plugin.getMatchManager().getAlivePlayers());

        for (int i = 0; i < survivors.size(); i++) {
            Player p = Bukkit.getPlayer(survivors.get(i));
            if (p == null) continue;

            Location targetSpawn = spawns.get(i % spawns.size());
            p.teleport(targetSpawn);
            p.setFallDistance(0f);

            p.getInventory().addItem(
                    plugin.getLootConfig().createTntMinecartItem(8),
                    new ItemStack(Material.RAIL, 32),
                    new ItemStack(Material.GOLDEN_APPLE, 2)
            );
            plugin.getLocaleManager().sendMessage(p, "deathmatch.arena-intro-chat");
        }

        Location specLoc = new Location(world, plugin.getConfigManager().getDeathmatchCenterX(),
                plugin.getConfigManager().getDeathmatchFloorY() + 8, plugin.getConfigManager().getDeathmatchCenterZ());
        for (UUID uuid : plugin.getMatchManager().getMatchPlayers()) {
            if (!survivors.contains(uuid)) {
                Player spec = Bukkit.getPlayer(uuid);
                if (spec != null) spec.teleport(specLoc);
            }
        }

        this.isDeathmatchFrozen = true;
        this.freezeTask = new BukkitRunnable() {
            int count = plugin.getConfigManager().getDeathmatchFreezeSeconds();

            @Override
            public void run() {
                if (count <= 0) {
                    cancel();
                    isDeathmatchFrozen = false;
                    border.setSize(
                            plugin.getConfigManager().getDeathmatchFinalBorder(),
                            plugin.getConfigManager().getDeathmatchShrinkDuration()
                    );
                    for (UUID uuid : plugin.getMatchManager().getMatchPlayers()) {
                        Player p = Bukkit.getPlayer(uuid);
                        if (p != null) {
                            plugin.getLocaleManager().sendTitle(p, "deathmatch.fight-start-title", 100, 1200, 300);
                            p.playSound(p.getLocation(), Sound.ENTITY_WITHER_SPAWN, 0.8f, 1.2f);
                        }
                    }
                    return;
                }

                String secStr = String.valueOf(count);
                for (UUID uuid : plugin.getMatchManager().getMatchPlayers()) {
                    Player p = Bukkit.getPlayer(uuid);
                    if (p != null) {
                        plugin.getLocaleManager().sendTitle(p, "deathmatch.freeze-countdown-title", 50, 800, 150, "<seconds>", secStr);
                        p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_PLING, 1f, 1f);
                    }
                }
                count--;
            }
        }.runTaskTimer(plugin, 0L, 20L);
    }

    public void reset() {
        this.isDeathmatchFrozen = false;
        if (freezeTask != null) {
            freezeTask.cancel();
            freezeTask = null;
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFreezeMove(PlayerMoveEvent event) {
        if (!isDeathmatchFrozen) return;
        if (!plugin.getMatchManager().getAlivePlayers().contains(event.getPlayer().getUniqueId())) return;

        Location from = event.getFrom();
        Location to = event.getTo();
        if (from.getX() != to.getX() || from.getZ() != to.getZ() || to.getY() > from.getY()) {
            Location locked = from.clone();
            locked.setYaw(to.getYaw());
            locked.setPitch(to.getPitch());
            event.setTo(locked);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFreezeBlockPlace(BlockPlaceEvent event) {
        if (isDeathmatchFrozen && plugin.getMatchManager().getAlivePlayers().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFreezeEntityPlace(EntityPlaceEvent event) {
        if (isDeathmatchFrozen && event.getPlayer() != null
                && plugin.getMatchManager().getAlivePlayers().contains(event.getPlayer().getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFreezeBowShoot(EntityShootBowEvent event) {
        if (isDeathmatchFrozen && event.getEntity() instanceof Player p
                && plugin.getMatchManager().getAlivePlayers().contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onFreezeDamage(EntityDamageEvent event) {
        if (isDeathmatchFrozen && event.getEntity() instanceof Player p
                && plugin.getMatchManager().getAlivePlayers().contains(p.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
