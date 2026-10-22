package me.nhan.cartbr.gui;

import com.nexomc.nexo.api.NexoItems;
import com.nexomc.nexo.items.ItemBuilder;
import me.nhan.cartbr.CartBRPlugin;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

public class HotbarManager implements Listener {

    private final CartBRPlugin plugin;
    private final NamespacedKey hotbarKey;

    public HotbarManager(CartBRPlugin plugin) {
        this.plugin = plugin;
        this.hotbarKey = new NamespacedKey(plugin, "cartbr_hotbar_item");
    }

    public void giveLobbyItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(0, createHotbarItem(
                plugin.getConfigManager().getNexoJoinItemId(),
                Material.MINECART,
                plugin.getLocaleManager().getComponent(player, "items.join-item"),
                "join"
        ));
        player.getInventory().setItem(4, createHotbarItem(
                plugin.getConfigManager().getNexoSettingsItemId(),
                Material.COMPARATOR,
                plugin.getLocaleManager().getComponent(player, "items.settings-item"),
                "settings"
        ));
    }

    public void giveWaitingItems(Player player) {
        player.getInventory().clear();
        player.getInventory().setItem(4, createHotbarItem(
                plugin.getConfigManager().getNexoSettingsItemId(),
                Material.COMPARATOR,
                plugin.getLocaleManager().getComponent(player, "items.settings-item"),
                "settings"
        ));
        player.getInventory().setItem(8, createHotbarItem(
                plugin.getConfigManager().getNexoLeaveItemId(),
                Material.RED_BED,
                plugin.getLocaleManager().getComponent(player, "items.leave-item"),
                "leave"
        ));
    }

    public void refreshHotbarNames(Player player) {
        if (plugin.getMatchManager().isInQueue(player)) {
            giveWaitingItems(player);
        } else if (!plugin.getMatchManager().isInActiveMatch(player)) {
            giveLobbyItems(player);
        }
    }

    private ItemStack createHotbarItem(String nexoId, Material fallbackMat, Component displayName, String actionType) {
        ItemStack item = null;
        if (Bukkit.getPluginManager().isPluginEnabled("Nexo")) {
            try {
                ItemBuilder builder = NexoItems.itemFromId(nexoId);
                if (builder != null) item = builder.build();
            } catch (Throwable ignored) {}
        }

        if (item == null) {
            item = new ItemStack(fallbackMat);
        }

        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(displayName);
            meta.getPersistentDataContainer().set(hotbarKey, PersistentDataType.STRING, actionType);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isHotbarItem(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;
        return item.getItemMeta().getPersistentDataContainer().has(hotbarKey, PersistentDataType.STRING);
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        giveLobbyItems(event.getPlayer());
    }

    @EventHandler
    public void onInteract(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_AIR && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        ItemStack item = event.getItem();
        if (!isHotbarItem(item)) return;

        event.setCancelled(true);
        Player player = event.getPlayer();
        String action = item.getItemMeta().getPersistentDataContainer().get(hotbarKey, PersistentDataType.STRING);
        if (action == null) return;

        switch (action) {
            case "join" -> plugin.getMatchManager().joinQueue(player);
            case "leave" -> plugin.getMatchManager().leaveQueue(player);
            case "settings" -> new SettingsGUI(plugin, player).open();
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (isHotbarItem(event.getItemDrop().getItemStack())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onSwapHand(PlayerSwapHandItemsEvent event) {
        if (isHotbarItem(event.getMainHandItem()) || isHotbarItem(event.getOffHandItem())) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (isHotbarItem(event.getCurrentItem()) || isHotbarItem(event.getCursor())) {
            event.setCancelled(true);
            return;
        }
        if (event.getHotbarButton() != -1 && event.getWhoClicked() instanceof Player p) {
            ItemStack hotbarItem = p.getInventory().getItem(event.getHotbarButton());
            if (isHotbarItem(hotbarItem)) {
                event.setCancelled(true);
            }
        }
    }
}
