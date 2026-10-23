package me.nhan.cartbr.gui;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.locale.Language;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

public class SettingsGUI implements InventoryHolder {

    private final CartBRPlugin plugin;
    private final Player player;
    private final Inventory inventory;

    public SettingsGUI(CartBRPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Component title = plugin.getLocaleManager().getComponent(player, "settings-gui.title");
        this.inventory = Bukkit.createInventory(this, 27, title);
        render();
    }

    public void render() {
        ItemStack darkGlass = createHiddenPane(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accentGlass = createHiddenPane(Material.PURPLE_STAINED_GLASS_PANE);

        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, darkGlass);
        }
        int[] accents = {0, 8, 9, 17, 18, 26};
        for (int slot : accents) {
            inventory.setItem(slot, accentGlass);
        }

        ItemStack langBtn = new ItemStack(Material.KNOWLEDGE_BOOK);
        ItemMeta meta = langBtn.getItemMeta();
        if (meta != null) {
            meta.displayName(plugin.getLocaleManager().getComponent(player, "settings-gui.lang-button-name"));
            meta.lore(plugin.getLocaleManager().getComponentList(player, "settings-gui.lang-button-lore"));
            langBtn.setItemMeta(meta);
        }
        inventory.setItem(13, langBtn);
    }

    private ItemStack createHiddenPane(Material mat) {
        ItemStack pane = new ItemStack(mat);
        ItemMeta meta = pane.getItemMeta();
        if (meta != null) {
            meta.displayName(Component.empty());
            meta.setHideTooltip(true);
            pane.setItemMeta(meta);
        }
        return pane;
    }

    public void open() {
        player.openInventory(inventory);
    }

    @Override
    public @NotNull Inventory getInventory() {
        return inventory;
    }

    public static class Listener implements org.bukkit.event.Listener {
        private final CartBRPlugin plugin;

        public Listener(CartBRPlugin plugin) {
            this.plugin = plugin;
        }

        @EventHandler
        public void onClick(InventoryClickEvent event) {
            if (!(event.getInventory().getHolder() instanceof SettingsGUI)) return;
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (event.getRawSlot() != 13) return;

            Language current = plugin.getLocaleManager().getLanguage(player);
            plugin.getLocaleManager().setLanguage(player, current.next());

            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.2f);
            plugin.getLocaleManager().sendMessage(player, "settings-gui.language-changed");

            plugin.getHotbarManager().refreshHotbarNames(player);
            plugin.getPhaseScheduler().switchPlayerBossBarLanguage(player);
            new SettingsGUI(plugin, player).open();
        }
    }
}
