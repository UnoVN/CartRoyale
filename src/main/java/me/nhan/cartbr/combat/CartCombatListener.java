package me.nhan.cartbr.combat;

import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.*;
import org.bukkit.entity.*;
import org.bukkit.entity.minecart.ExplosiveMinecart;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.*;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.event.vehicle.VehicleDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;

public class CartCombatListener implements Listener {

    private final CartBRPlugin plugin;
    private final NamespacedKey placerKey;
    private final NamespacedKey igniterKey;

    private final Map<UUID, List<Long>> cartPlaceTimestamps = new HashMap<>();
    private final Map<UUID, CartDamageRecord> lastCartDamageMap = new HashMap<>();
    private final Map<UUID, Location> pendingSpectatorRespawn = new HashMap<>();

    public CartCombatListener(CartBRPlugin plugin) {
        this.plugin = plugin;
        this.placerKey = new NamespacedKey(plugin, "placer_uuid");
        this.igniterKey = new NamespacedKey(plugin, "igniter_uuid");
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityPlace(EntityPlaceEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart cart)) return;
        Player player = event.getPlayer();
        if (player == null) return;

        long now = System.currentTimeMillis();
        long windowMs = plugin.getConfigManager().getCartSpamWindowMs();
        int maxCarts = plugin.getConfigManager().getMaxCartsPerWindow();

        List<Long> timestamps = cartPlaceTimestamps.computeIfAbsent(player.getUniqueId(), k -> new ArrayList<>());
        timestamps.removeIf(t -> now - t > windowMs);

        if (timestamps.size() >= maxCarts) {
            event.setCancelled(true);
            double waitSec = Math.max(0.1, (windowMs - (now - timestamps.getFirst())) / 1000.0);
            plugin.getLocaleManager().sendActionBar(player, "combat.cart-cooldown",
                    "<time>", String.format(Locale.US, "%.1f", waitSec));
            return;
        }

        timestamps.add(now);
        cart.getPersistentDataContainer().set(placerKey, PersistentDataType.STRING, player.getUniqueId().toString());
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onProjectileHitCart(ProjectileHitEvent event) {
        if (!(event.getHitEntity() instanceof ExplosiveMinecart cart)) return;
        if (event.getEntity().getShooter() instanceof Player shooter) {
            cart.getPersistentDataContainer().set(igniterKey, PersistentDataType.STRING, shooter.getUniqueId().toString());
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onVehicleDamage(VehicleDamageEvent event) {
        if (!(event.getVehicle() instanceof ExplosiveMinecart cart)) return;
        Player attacker = resolvePlayerAttacker(event.getAttacker());
        if (attacker != null) {
            cart.getPersistentDataContainer().set(igniterKey, PersistentDataType.STRING, attacker.getUniqueId().toString());
        }
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onCartChainExplode(EntityExplodeEvent event) {
        if (!(event.getEntity() instanceof ExplosiveMinecart sourceCart)) return;
        UUID ownerUuid = resolveCartKiller(sourceCart);
        if (ownerUuid == null) return;

        for (Entity nearby : sourceCart.getNearbyEntities(6.5, 6.5, 6.5)) {
            if (nearby instanceof ExplosiveMinecart otherCart) {
                PersistentDataContainer pdc = otherCart.getPersistentDataContainer();
                if (!pdc.has(igniterKey, PersistentDataType.STRING)) {
                    pdc.set(igniterKey, PersistentDataType.STRING, ownerUuid.toString());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onDamageByCart(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;
        if (event.getDamager() instanceof ExplosiveMinecart cart) {
            UUID killerUuid = resolveCartKiller(cart);
            if (killerUuid != null) {
                lastCartDamageMap.put(victim.getUniqueId(), new CartDamageRecord(killerUuid, System.currentTimeMillis()));
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player victim = event.getEntity();
        if (!plugin.getMatchManager().getAlivePlayers().contains(victim.getUniqueId())) return;

        event.deathMessage(null);
        plugin.getParachuteManager().removeParachute(victim);
        event.getDrops().removeIf(item -> plugin.getParachuteManager().isParachuteItem(item));

        Player killer = victim.getKiller();
        boolean byCart = false;

        CartDamageRecord cartRecord = lastCartDamageMap.remove(victim.getUniqueId());
        if (cartRecord != null && System.currentTimeMillis() - cartRecord.timestamp <= 5000L) {
            Player cartKiller = Bukkit.getPlayer(cartRecord.killerUuid);
            if (cartKiller != null && !cartKiller.equals(victim)) {
                killer = cartKiller;
                byCart = true;
            }
        }

        if (killer != null && !killer.equals(victim)) {
            grantKillRefill(killer);
        }

        Location deathLoc = victim.getLocation().clone().add(0, 2.0, 0);
        pendingSpectatorRespawn.put(victim.getUniqueId(), deathLoc);

        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (victim.isDead()) {
                victim.spigot().respawn();
            }
            victim.setGameMode(GameMode.SPECTATOR);
            victim.teleport(deathLoc);
        }, 1L);

        plugin.getMatchManager().eliminatePlayer(victim, killer, byCart);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerRespawn(PlayerRespawnEvent event) {
        Player player = event.getPlayer();
        Location specLoc = pendingSpectatorRespawn.remove(player.getUniqueId());
        if (specLoc != null) {
            event.setRespawnLocation(specLoc);
            Bukkit.getScheduler().runTaskLater(plugin, () -> {
                player.setGameMode(GameMode.SPECTATOR);
                player.teleport(specLoc);
            }, 1L);
        }
    }

    private void grantKillRefill(Player killer) {
        giveOrDrop(killer, plugin.getLootConfig().createTntMinecartItem(5));
        giveOrDrop(killer, new ItemStack(Material.RAIL, 16));
        giveOrDrop(killer, new ItemStack(Material.GOLDEN_APPLE, 2));

        killer.playSound(killer.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.2f);
        plugin.getLocaleManager().sendMessage(killer, "combat.kill-refill-reward");
    }

    private void giveOrDrop(Player player, ItemStack item) {
        Map<Integer, ItemStack> leftover = player.getInventory().addItem(item);
        for (ItemStack drop : leftover.values()) {
            player.getWorld().dropItemNaturally(player.getLocation(), drop);
        }
    }

    private UUID resolveCartKiller(ExplosiveMinecart cart) {
        PersistentDataContainer pdc = cart.getPersistentDataContainer();
        String igniter = pdc.get(igniterKey, PersistentDataType.STRING);
        if (igniter != null) {
            try { return UUID.fromString(igniter); } catch (IllegalArgumentException ignored) {}
        }
        String placer = pdc.get(placerKey, PersistentDataType.STRING);
        if (placer != null) {
            try { return UUID.fromString(placer); } catch (IllegalArgumentException ignored) {}
        }
        return null;
    }

    private Player resolvePlayerAttacker(Entity damager) {
        if (damager instanceof Player p) return p;
        if (damager instanceof Projectile proj && proj.getShooter() instanceof Player p) return p;
        return null;
    }

    private record CartDamageRecord(UUID killerUuid, long timestamp) {}
}
