package me.nhan.cartbr.gui;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.arena.GameState;
import me.nhan.cartbr.config.ConfigManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AdminGUI implements InventoryHolder {

    private final CartBRPlugin plugin;
    private final Player player;
    private final Inventory inventory;
    private final MiniMessage mm = MiniMessage.miniMessage();

    public AdminGUI(CartBRPlugin plugin, Player player) {
        this.plugin = plugin;
        this.player = player;
        Component title = mm.deserialize("<#1e1e2e><bold>🛠 ᴄᴀʀᴛʙʀ ᴀᴅᴍɪɴ ᴘᴀɴᴇʟ</bold>");
        this.inventory = Bukkit.createInventory(this, 54, title);
        render();
    }

    public void render() {
        ItemStack filler = createItem(Material.GRAY_STAINED_GLASS_PANE, "");
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        ConfigManager cfg = plugin.getConfigManager();
        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();

        // 10: Lobby Spawn
        Location lobby = cfg.getLobbySpawn();
        inventory.setItem(10, createItem(Material.BEACON,
                "<gradient:#89dceb:#cba6f7><bold>🏠 ʟᴏʙʙʏ sᴘᴀᴡɴ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴡᴏʀʟᴅ: <#f9e2af>" + (lobby.getWorld() != null ? lobby.getWorld().getName() : "null"),
                "<#cdd6f4>ᴄᴏᴏʀᴅs: <#a6e3a1>" + fmtLoc(lobby),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ ʜᴇʀᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 11: Waiting Cage
        Location waiting = arenaWorld != null ? cfg.getWaitingCageLocation(arenaWorld) : null;
        inventory.setItem(11, createItem(Material.GLASS,
                "<gradient:#a6e3a1:#89dceb><bold>⏳ ᴡᴀɪᴛɪɴɢ ᴄᴀɢᴇ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴏᴏʀᴅs: <#a6e3a1>" + (waiting != null ? fmtLoc(waiting) : "ᴀʀᴇɴᴀ ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ ʜᴇʀᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 12: Dragon P1
        Location p1 = arenaWorld != null ? cfg.getDragonP1(arenaWorld) : null;
        inventory.setItem(12, createItem(Material.DRAGON_HEAD,
                "<gradient:#cba6f7:#f5c2e7><bold>🐉 ᴅʀᴀɢᴏɴ sᴛᴀʀᴛ (ᴘ1)</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴏᴏʀᴅs: <#a6e3a1>" + (p1 != null ? fmtBlockLoc(p1) : "ᴀʀᴇɴᴀ ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ ʜᴇʀᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 13: Dragon P2
        Location p2 = arenaWorld != null ? cfg.getDragonP2(arenaWorld) : null;
        inventory.setItem(13, createItem(Material.ELYTRA,
                "<gradient:#f5c2e7:#fab387><bold>🪂 ᴅʀᴀɢᴏɴ ᴇɴᴅ (ᴘ2)</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴏᴏʀᴅs: <#a6e3a1>" + (p2 != null ? fmtBlockLoc(p2) : "ᴀʀᴇɴᴀ ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ ʜᴇʀᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 14: Main Island Center
        inventory.setItem(14, createItem(Material.COMPASS,
                "<gradient:#f9e2af:#a6e3a1><bold>🏝 ɪsʟᴀɴᴅ ᴄᴇɴᴛᴇʀ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴇɴᴛᴇʀ x/ᴢ: <#a6e3a1>" + cfg.getIslandCenterX() + " / " + cfg.getIslandCenterZ(),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ (ʏ=120)",
                "<#6c7086>────────────────────────"
        ));

        // 15: Deathmatch Center
        inventory.setItem(15, createItem(Material.NETHERITE_SWORD,
                "<gradient:#f38ba8:#cba6f7><bold>⚔ ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ᴄᴇɴᴛᴇʀ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴇɴᴛᴇʀ x/ᴢ: <#a6e3a1>" + cfg.getDeathmatchCenterX() + " / " + cfg.getDeathmatchCenterZ(),
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ᴛᴏ ᴄᴜʀʀᴇɴᴛ ᴘᴏs",
                "<#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ ʜᴇʀᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 16: Deathmatch Floor Y Protection
        inventory.setItem(16, createItem(Material.OBSIDIAN,
                "<gradient:#fab387:#f38ba8><bold>🛡 ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ꜰʟᴏᴏʀ ʏ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴘʀᴏᴛᴇᴄᴛᴇᴅ ʏ ≤ <#f9e2af><bold>" + cfg.getDeathmatchFloorY() + "</bold>",
                "<#a6adc8>(ʙʟᴏᴄᴋs ᴀᴛ ᴏʀ ʙᴇʟᴏᴡ ᴛʜɪs ʏ ᴡᴏɴ'ᴛ ᴇxᴘʟᴏᴅᴇ)",
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>sᴇᴛ ꜰʟᴏᴏʀ ᴛᴏ ʙʟᴏᴄᴋ ᴜɴᴅᴇʀ ꜰᴇᴇᴛ",
                "<#6c7086>────────────────────────"
        ));

        // 20: World Switcher (Teleport between Lobby & Arena World)
        String arenaName = cfg.getTemplateWorldName();
        boolean inArena = arenaWorld != null && player.getWorld().equals(arenaWorld);
        inventory.setItem(20, createItem(Material.ENDER_PEARL,
                "<gradient:#89dceb:#a6e3a1><bold>🌍 ᴡᴏʀʟᴅ ᴛᴇʟᴇᴘᴏʀᴛᴇʀ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴜʀʀᴇɴᴛ ᴡᴏʀʟᴅ: <#f9e2af>" + player.getWorld().getName(),
                "<#cdd6f4>ᴀʀᴇɴᴀ ᴡᴏʀʟᴅ: <#89dceb>" + arenaName,
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴘ ᴛᴏ ᴀʀᴇɴᴀ ᴡᴏʀʟᴅ",
                "<#fab387>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴛᴘ ᴛᴏ ʟᴏʙʙʏ ᴡᴏʀʟᴅ",
                "<#6c7086>────────────────────────"
        ));

        // 22: Deathmatch Spawns Manager
        List<String> rawSpawns = cfg.getRawDeathmatchSpawns();
        List<String> spawnLore = new ArrayList<>();
        spawnLore.add("<#6c7086>────────────────────────");
        spawnLore.add("<#cdd6f4>ᴛᴏᴛᴀʟ sᴘᴀᴡɴs: <#f9e2af><bold>" + rawSpawns.size() + "</bold>");
        int previewLimit = Math.min(rawSpawns.size(), 5);
        for (int i = 0; i < previewLimit; i++) {
            spawnLore.add(" <#a6adc8>#" + (i + 1) + ": <#89dceb>" + rawSpawns.get(i));
        }
        if (rawSpawns.size() > 5) {
            spawnLore.add(" <#6c7086>... (+" + (rawSpawns.size() - 5) + " ᴍᴏʀᴇ)");
        }
        spawnLore.add("");
        spawnLore.add("<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ᴀᴅᴅ ᴄᴜʀʀᴇɴᴛ ᴘᴏs");
        spawnLore.add("<#fab387>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ʀᴇᴍᴏᴠᴇ ʟᴀsᴛ sᴘᴀᴡɴ");
        spawnLore.add("<#f38ba8>▸ sʜɪꜰᴛ + ʀɪɢʜᴛ: <#cdd6f4>ᴄʟᴇᴀʀ ᴀʟʟ sᴘᴀᴡɴs");
        spawnLore.add("<#6c7086>────────────────────────");
        inventory.setItem(22, createItem(Material.ARMOR_STAND,
                "<gradient:#f9e2af:#fab387><bold>📍 ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ sᴘᴀᴡɴs</bold></gradient>",
                spawnLore.toArray(new String[0])
        ));

        // 24: Scan & Refresh Chest Holograms
        inventory.setItem(24, createItem(Material.CHEST,
                "<gradient:#a6e3a1:#f9e2af><bold>📦 ʀᴇꜰʀᴇsʜ ᴄʜᴇsᴛ ʜᴏʟᴏɢʀᴀᴍs</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>sᴄᴀɴs ᴀʟʟ ʟᴏᴀᴅᴇᴅ ᴄʜᴜɴᴋs ɪɴ ᴀʀᴇɴᴀ",
                "<#cdd6f4>ᴀɴᴅ sᴘᴀᴡɴs ᴛᴇxᴛᴅɪsᴘʟᴀʏ ʜᴏʟᴏɢʀᴀᴍs.",
                "",
                "<#a6e3a1>▸ ᴄʟɪᴄᴋ: <#cdd6f4>sᴄᴀɴ & ʀᴇꜰʀᴇsʜ ɴᴏᴡ",
                "<#6c7086>────────────────────────"
        ));

        // 37: Force Start Match
        GameState state = plugin.getMatchManager().getState();
        inventory.setItem(37, createItem(Material.EMERALD_BLOCK,
                "<gradient:#a6e3a1:#89dceb><bold>▶ ꜰᴏʀᴄᴇ sᴛᴀʀᴛ ᴍᴀᴛᴄʜ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>sᴛᴀᴛᴇ: <#f9e2af>" + state.name(),
                "<#cdd6f4>ɪɴ ǫᴜᴇᴜᴇ: <#89dceb>" + plugin.getMatchManager().getMatchPlayers().size(),
                "",
                "<#a6e3a1>▸ ᴄʟɪᴄᴋ: <#cdd6f4>sᴛᴀʀᴛ ᴍᴀᴛᴄʜ ɪᴍᴍᴇᴅɪᴀᴛᴇʟʏ",
                "<#6c7086>────────────────────────"
        ));

        // 39: Skip Phase
        inventory.setItem(39, createItem(Material.CLOCK,
                "<gradient:#fab387:#f38ba8><bold>⏩ sᴋɪᴘ ᴛᴏ ɴᴇxᴛ ᴘʜᴀsᴇ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴄᴜʀʀᴇɴᴛ ᴘʜᴀsᴇ: <#f9e2af>" + plugin.getPhaseScheduler().getCurrentPhase().name(),
                "",
                "<#a6e3a1>▸ ᴄʟɪᴄᴋ: <#cdd6f4>ᴀᴅᴠᴀɴᴄᴇ ᴛᴏ ɴᴇxᴛ ᴘʜᴀsᴇ",
                "<#6c7086>────────────────────────"
        ));

        // 40: Adjust Min Players
        inventory.setItem(40, createItem(Material.TOTEM_OF_UNDYING,
                "<gradient:#cba6f7:#89dceb><bold>👥 ᴍɪɴ ᴘʟᴀʏᴇʀs: " + cfg.getMinPlayers() + "</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴍɪɴ ᴘʟᴀʏᴇʀs ᴛᴏ ᴀᴜᴛᴏ-sᴛᴀʀᴛ: <#f9e2af><bold>" + cfg.getMinPlayers() + "</bold>",
                "<#a6adc8>(sᴇᴛ ᴛᴏ 1 ꜰᴏʀ sᴏʟᴏ ᴛᴇsᴛɪɴɢ!)",
                "",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>+1 ᴘʟᴀʏᴇʀ",
                "<#f38ba8>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>-1 ᴘʟᴀʏᴇʀ",
                "<#6c7086>────────────────────────"
        ));

        // 41: Force Reset Arena (SWM Unload & Load)
        inventory.setItem(41, createItem(Material.TNT_MINECART,
                "<gradient:#f38ba8:#fab387><bold>🔄 ꜰᴏʀᴄᴇ ʀᴇsᴇᴛ ᴀʀᴇɴᴀ (sᴡᴍ)</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#cdd6f4>ᴜɴʟᴏᴀᴅs & ʀᴇʟᴏᴀᴅs <#89dceb>" + arenaName,
                "<#cdd6f4>ꜰʀᴇsʜ ꜰʀᴏᴍ sʟɪᴍᴇ ʟᴏᴀᴅᴇʀ ᴏɴ ʀᴀᴍ.",
                "",
                "<#f38ba8>▸ ᴄʟɪᴄᴋ: <#cdd6f4>ʀᴇsᴇᴛ ᴀʀᴇɴᴀ ɴᴏᴡ",
                "<#6c7086>────────────────────────"
        ));

        // 43: Reload Configs & Save SlimeWorld
        inventory.setItem(43, createItem(Material.WRITABLE_BOOK,
                "<gradient:#f9e2af:#a6e3a1><bold>💾 ʀᴇʟᴏᴀᴅ & sᴀᴠᴇ sʟɪᴍᴇᴡᴏʀʟᴅ</bold></gradient>",
                "<#6c7086>────────────────────────",
                "<#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ: <#cdd6f4>ʀᴇʟᴏᴀᴅ ᴄᴏɴꜰɪɢ / ʟᴏᴏᴛ / ʟᴀɴɢ",
                "<#f9e2af>▸ sʜɪꜰᴛ + ʟᴇꜰᴛ: <#cdd6f4>ʀᴜɴ '/sᴡᴘ sᴀᴠᴇ " + arenaName + "'",
                "<#6c7086>────────────────────────"
        ));
    }

    private ItemStack createItem(Material mat, String name, String... loreLines) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.displayName(name.isEmpty() ? Component.empty()
                    : mm.deserialize(name).decoration(TextDecoration.ITALIC, false));
            if (loreLines.length > 0) {
                List<Component> lore = new ArrayList<>(loreLines.length);
                for (String line : loreLines) {
                    lore.add(mm.deserialize(line).decoration(TextDecoration.ITALIC, false));
                }
                meta.lore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private String fmtLoc(Location loc) {
        return String.format(Locale.US, "%.1f, %.1f, %.1f (%.0f°)", loc.getX(), loc.getY(), loc.getZ(), loc.getYaw());
    }

    private String fmtBlockLoc(Location loc) {
        return String.format(Locale.US, "%.1f, %.1f, %.1f", loc.getX(), loc.getY(), loc.getZ());
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
            if (!(event.getInventory().getHolder() instanceof AdminGUI gui)) return;
            event.setCancelled(true);

            if (!(event.getWhoClicked() instanceof Player player)) return;
            if (!player.hasPermission("cartbr.admin")) {
                player.closeInventory();
                return;
            }

            ConfigManager cfg = plugin.getConfigManager();
            World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
            ClickType click = event.getClick();
            int slot = event.getRawSlot();

            switch (slot) {
                case 10 -> {
                    if (click.isLeftClick()) {
                        cfg.setLobbySpawn(player.getLocation());
                        feedback(player, "✔ Saved Lobby Spawn!");
                    } else if (click.isRightClick()) {
                        player.teleport(cfg.getLobbySpawn());
                        feedback(player, "✔ Teleported to Lobby Spawn!");
                    }
                }
                case 11 -> {
                    if (click.isLeftClick()) {
                        cfg.setWaitingCage(player.getLocation());
                        feedback(player, "✔ Saved Waiting Cage location!");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getWaitingCageLocation(arenaWorld));
                        feedback(player, "✔ Teleported to Waiting Cage!");
                    }
                }
                case 12 -> {
                    if (click.isLeftClick()) {
                        cfg.setDragonP1(player.getLocation());
                        feedback(player, "✔ Saved Dragon Start (P1)!");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getDragonP1(arenaWorld));
                        feedback(player, "✔ Teleported to Dragon Start (P1)!");
                    }
                }
                case 13 -> {
                    if (click.isLeftClick()) {
                        cfg.setDragonP2(player.getLocation());
                        feedback(player, "✔ Saved Dragon End (P2)!");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getDragonP2(arenaWorld));
                        feedback(player, "✔ Teleported to Dragon End (P2)!");
                    }
                }
                case 14 -> {
                    if (click.isLeftClick()) {
                        cfg.setIslandCenter(player.getLocation());
                        feedback(player, "✔ Saved Main Island Center!");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(new Location(arenaWorld, cfg.getIslandCenterX(), 120.0, cfg.getIslandCenterZ()));
                        feedback(player, "✔ Teleported to Main Island Center!");
                    }
                }
                case 15 -> {
                    if (click.isLeftClick()) {
                        cfg.setDeathmatchCenter(player.getLocation());
                        feedback(player, "✔ Saved Deathmatch Center!");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(new Location(arenaWorld, cfg.getDeathmatchCenterX(), cfg.getDeathmatchFloorY() + 1.0, cfg.getDeathmatchCenterZ()));
                        feedback(player, "✔ Teleported to Deathmatch Center!");
                    }
                }
                case 16 -> {
                    if (click.isLeftClick()) {
                        int floorY = player.getLocation().getBlockY() - 1;
                        cfg.setDeathmatchFloorY(floorY);
                        feedback(player, "✔ Saved Deathmatch Floor Y = " + floorY + "!");
                    }
                }
                case 20 -> {
                    if (click.isLeftClick() && arenaWorld != null) {
                        player.teleport(cfg.getWaitingCageLocation(arenaWorld));
                        feedback(player, "✔ Teleported to Arena World (" + arenaWorld.getName() + ")!");
                    } else if (click.isRightClick()) {
                        player.teleport(cfg.getLobbySpawn());
                        feedback(player, "✔ Teleported to Lobby World!");
                    }
                }
                case 22 -> {
                    if (click == ClickType.LEFT) {
                        int count = cfg.addDeathmatchSpawn(player.getLocation());
                        feedback(player, "✔ Added Deathmatch Spawn #" + count + "!");
                    } else if (click == ClickType.RIGHT) {
                        if (cfg.removeLastDeathmatchSpawn()) {
                            feedback(player, "✔ Removed last Deathmatch Spawn!");
                        } else {
                            player.sendMessage(ChatColor.RED + "Spawn list is already empty!");
                        }
                    } else if (click == ClickType.SHIFT_RIGHT) {
                        cfg.clearDeathmatchSpawns();
                        feedback(player, "✔ Cleared all Deathmatch Spawns!");
                    }
                }
                case 24 -> {
                    if (arenaWorld != null) {
                        plugin.getHologramManager().removeAllHolograms();
                        for (Chunk chunk : arenaWorld.getLoadedChunks()) {
                            plugin.getHologramManager().scanChunkForChests(chunk);
                        }
                        feedback(player, "✔ Refreshed all Chest Holograms in loaded chunks!");
                    }
                }
                case 37 -> {
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "cartbr forcestart");
                    return;
                }
                case 39 -> {
                    if (plugin.getMatchManager().forceSkipPhase()) {
                        feedback(player, "✔ Skipped to next Phase: " + plugin.getPhaseScheduler().getCurrentPhase().name());
                    } else {
                        player.sendMessage(ChatColor.RED + "Can only skip phases while a match is running!");
                    }
                }
                case 40 -> {
                    int current = cfg.getMinPlayers();
                    if (click.isLeftClick()) {
                        cfg.setMinPlayers(Math.min(16, current + 1));
                    } else if (click.isRightClick()) {
                        cfg.setMinPlayers(Math.max(1, current - 1));
                    }
                    feedback(player, "✔ Set Min Players to " + cfg.getMinPlayers() + "!");
                }
                case 41 -> {
                    player.closeInventory();
                    feedback(player, "🔄 Unloading & Reloading Slime Arena on RAM...");
                    plugin.getMatchManager().resetSingleArena();
                    return;
                }
                case 43 -> {
                    if (click == ClickType.SHIFT_LEFT) {
                        player.closeInventory();
                        Bukkit.dispatchCommand(player, "swp save " + cfg.getTemplateWorldName());
                        feedback(player, "💾 Dispatched '/swp save " + cfg.getTemplateWorldName() + "'!");
                        return;
                    } else if (click.isLeftClick()) {
                        cfg.reload();
                        plugin.getLootConfig().load();
                        plugin.getLocaleManager().reload();
                        feedback(player, "✔ Reloaded config.yml, loot.yml & language files!");
                    }
                }
                default -> {
                    return;
                }
            }

            gui.render();
        }

        private void feedback(Player player, String msg) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            player.sendMessage(ChatColor.GREEN + msg);
        }
    }
}
