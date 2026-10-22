package me.nhan.cartbr.drop;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class ParachuteManager implements Listener {

    private final CartBRPlugin plugin;
    private final NamespacedKey parachuteKey;
    private final Set<UUID> parachutingPlayers = new HashSet<>();
    private final Map<UUID, Long> fallImmunityMap = new HashMap<>();

    public ParachuteManager(CartBRPlugin plugin) {
        this.plugin = plugin;
        this.parachuteKey = new NamespacedKey(plugin, "cartbr_parachute");
        startGlidingPhysicsTask();
    }

    private void startGlidingPhysicsTask() {
        new BukkitRunnable() {
            int tick = 0;

            @Override
            public void run() {
                tick++;
                if (parachutingPlayers.isEmpty()) return;

                for (UUID uuid : new HashSet<>(parachutingPlayers)) {
                    Player player = Bukkit.getPlayer(uuid);
                    if (player == null || !player.isOnline()) {
                        parachutingPlayers.remove(uuid);
                        continue;
                    }

                    Block below = player.getLocation().subtract(0, 0.2, 0).getBlock();
                    if (player.isOnGround() || (!below.getType().isAir() && below.getType() != Material.CAVE_AIR)) {
                        landPlayer(player);
                        continue;
                    }

                    Vector lookDir = player.getLocation().getDirection().setY(0);
                    if (lookDir.lengthSquared() > 0.001) {
                        lookDir.normalize().multiply(0.65);
                    }
                    player.setVelocity(new Vector(lookDir.getX(), -0.35, lookDir.getZ()));
                    player.setFallDistance(0f);

                    if (tick % 10 == 0) {
                        plugin.getLocaleManager().sendActionBar(player, "drop-phase.parachuting-actionbar");
                    }
                }
            }
        }.runTaskTimer(plugin, 1L, 1L);
    }

    public void startParachuting(Player player) {
        if (parachutingPlayers.contains(player.getUniqueId())) return;
        parachutingPlayers.add(player.getUniqueId());

        ItemStack parachuteHat = buildParachuteItem();
        player.getInventory().setHelmet(parachuteHat);
        player.setFallDistance(0f);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_ELYTRA, 1.0f, 1.0f);
    }

    private void landPlayer(Player player) {
        removeParachute(player);
        fallImmunityMap.put(player.getUniqueId(), System.currentTimeMillis() + 3000L);
        player.playSound(player.getLocation(), Sound.ITEM_ARMOR_EQUIP_LEATHER, 1.0f, 1.0f);
        plugin.getLocaleManager().sendMessage(player, "drop-phase.landed-chat");
    }

    public void removeParachute(Player player) {
        parachutingPlayers.remove(player.getUniqueId());
        ItemStack helmet = player.getInventory().getHelmet();
        if (isParachuteItem(helmet)) {
            player.getInventory().setHelmet(null);
        }
    }

    public void cleanupAll() {
        for (UUID uuid : new HashSet<>(parachutingPlayers)) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null) removeParachute(p);
        }
        parachutingPlayers.clear();
        fallImmunityMap.clear();
    }

    private ItemStack buildParachuteItem() {
        ItemStack item = null;
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            try {
                ItemBuilder builder = NexoItems.itemFromId(plugin.getConfigManager().getNexoParachuteId());
                if (builder != null) item = builder.build();
            } catch (Throwable ignored) {}
        }

        if (item == null) {
            item = new ItemStack(Material.FEATHER);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.getPersistentDataContainer().set(parachuteKey, PersistentDataType.INTEGER, 1);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isParachuteItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(parachuteKey, PersistentDataType.INTEGER);
    }

    @EventHandler
    public void onDismount(EntityDismountEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        CustomDragonTask dragonTask = plugin.getMatchManager().getDragonTask();
        if (dragonTask == null || !dragonTask.isCarrier(event.getDismounted())) return;

        if (dragonTask.getTicksElapsed() < 40) {
            event.setCancelled(true);
            return;
        }

        dragonTask.revealPlayer(player);
        Bukkit.getScheduler().runTaskLater(plugin, () -> startParachuting(player), 1L);
    }

    @EventHandler
    public void onHelmetClick(InventoryClickEvent event) {
        if (event.getSlotType() == InventoryType.SlotType.ARMOR
                || isParachuteItem(event.getCurrentItem())
                || isParachuteItem(event.getCursor())) {
            if (isParachuteItem(event.getCurrentItem()) || isParachuteItem(event.getCursor())) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onFallDamage(EntityDamageEvent event) {
        if (event.getCause() != EntityDamageEvent.DamageCause.FALL) return;
        if (!(event.getEntity() instanceof Player player)) return;

        if (parachutingPlayers.contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        Long expiry = fallImmunityMap.get(player.getUniqueId());
        if (expiry != null) {
            if (System.currentTimeMillis() <= expiry) {
                event.setCancelled(true);
            } else {
                fallImmunityMap.remove(player.getUniqueId());
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        removeParachute(event.getPlayer());
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        removeParachute(event.getPlayer());
    }
}
```

### File 22: `src/main/java/me/nhan/cartbr/loot/ChestTier.java`
```java
package me.nhan.cartbr.loot;

import org.bukkit.Material;
import org.bukkit.Tag;

public enum ChestTier {
    TIER_1,
    TIER_2,
    TIER_3;

    public static ChestTier fromMaterial(Material mat) {
        if (mat == Material.BARREL) return TIER_1;
        if (mat == Material.CHEST || mat == Material.TRAPPED_CHEST) return TIER_2;
        if (mat == Material.ENDER_CHEST || Tag.SHULKER_BOXES.isTagged(mat)) return TIER_3;
        return null;
    }

    public static boolean isLootContainer(Material mat) {
        return fromMaterial(mat) != null;
    }
}
