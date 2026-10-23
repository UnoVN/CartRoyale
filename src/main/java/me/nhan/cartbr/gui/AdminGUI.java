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
        Component title = mm.deserialize("<#1e1e2e><bold>ᴄᴀʀᴛʙʀ</bold> <#585b70>┃ <#cba6f7>ᴀᴅᴍɪɴ ᴄᴏɴsᴏʟᴇ");
        this.inventory = Bukkit.createInventory(this, 54, title);
        render();
    }

    public void render() {
        ItemStack darkPane = createHiddenPane(Material.BLACK_STAINED_GLASS_PANE);
        ItemStack accentPane = createHiddenPane(Material.PURPLE_STAINED_GLASS_PANE);

        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, darkPane);
        }
        int[] accents = {0, 1, 7, 8, 9, 17, 36, 44, 45, 46, 52, 53};
        for (int slot : accents) {
            inventory.setItem(slot, accentPane);
        }

        ConfigManager cfg = plugin.getConfigManager();
        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        GameState state = plugin.getMatchManager().getState();
        String arenaName = cfg.getTemplateWorldName();

        // 4: Overview Header
        inventory.setItem(4, createItem(Material.NETHER_STAR,
                "<white><sprite:items:item/nether_star></white> <gradient:#cba6f7:#f5c2e7><bold>ᴄᴀʀᴛʙʀ sᴛᴀᴛᴜs</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <white><sprite:items:item/ender_pearl></white> <#a6adc8>ᴀʀᴇɴᴀ: <#89dceb>" + arenaName + (arenaWorld != null ? " <#a6e3a1>(ʟᴏᴀᴅᴇᴅ)" : " <#f38ba8>(ᴏꜰꜰʟɪɴᴇ)"),
                " <white><sprite:items:item/clock_00></white> <#a6adc8>sᴛᴀᴛᴇ: <#f9e2af>" + state.name(),
                " <white><sprite:items:item/totem_of_undying></white> <#a6adc8>ᴘʟᴀʏᴇʀs: <#a6e3a1>" + plugin.getMatchManager().getMatchPlayers().size() + "<#585b70>/<#a6adc8>" + cfg.getMaxPlayers(),
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 10: Lobby Spawn
        Location lobby = cfg.getLobbySpawn();
        inventory.setItem(10, createItem(Material.BEACON,
                "<white><sprite:blocks:block/beacon></white> <gradient:#89dceb:#cba6f7><bold>ʟᴏʙʙʏ sᴘᴀᴡɴ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴡᴏʀʟᴅ: <#f9e2af>" + (lobby.getWorld() != null ? lobby.getWorld().getName() : "null"),
                " <#a6adc8>ᴘᴏs: <#a6e3a1>" + fmtLoc(lobby),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 11: Waiting Cage
        Location waiting = arenaWorld != null ? cfg.getWaitingCageLocation(arenaWorld) : null;
        inventory.setItem(11, createItem(Material.GLASS,
                "<white><sprite:blocks:block/glass></white> <gradient:#a6e3a1:#89dceb><bold>ᴡᴀɪᴛɪɴɢ ᴄᴀɢᴇ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴘᴏs: <#a6e3a1>" + (waiting != null ? fmtLoc(waiting) : "ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 12: Dragon P1
        Location p1 = arenaWorld != null ? cfg.getDragonP1(arenaWorld) : null;
        inventory.setItem(12, createItem(Material.DRAGON_HEAD,
                "<white><sprite:items:item/elytra></white> <gradient:#cba6f7:#f5c2e7><bold>ᴅʀᴀɢᴏɴ sᴛᴀʀᴛ (ᴘ1)</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴘᴏs: <#a6e3a1>" + (p1 != null ? fmtBlockLoc(p1) : "ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 13: Dragon P2
        Location p2 = arenaWorld != null ? cfg.getDragonP2(arenaWorld) : null;
        inventory.setItem(13, createItem(Material.ELYTRA,
                "<white><sprite:items:item/feather></white> <gradient:#f5c2e7:#fab387><bold>ᴅʀᴀɢᴏɴ ᴇɴᴅ (ᴘ2)</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴘᴏs: <#a6e3a1>" + (p2 != null ? fmtBlockLoc(p2) : "ɴᴏᴛ ʟᴏᴀᴅᴇᴅ"),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 14: Main Island Center
        inventory.setItem(14, createItem(Material.COMPASS,
                "<white><sprite:items:item/compass_16></white> <gradient:#f9e2af:#a6e3a1><bold>ɪsʟᴀɴᴅ ᴄᴇɴᴛᴇʀ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴄᴇɴᴛᴇʀ: <#a6e3a1>" + cfg.getIslandCenterX() + " / " + cfg.getIslandCenterZ(),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 15: Deathmatch Center
        inventory.setItem(15, createItem(Material.NETHERITE_SWORD,
                "<white><sprite:items:item/netherite_sword></white> <gradient:#f38ba8:#cba6f7><bold>ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ᴄᴇɴᴛᴇʀ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴄᴇɴᴛᴇʀ: <#a6e3a1>" + cfg.getDeathmatchCenterX() + " / " + cfg.getDeathmatchCenterZ(),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ʜᴇʀᴇ",
                " <#89dceb>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴇʟᴇᴘᴏʀᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 16: Deathmatch Floor Y Protection
        inventory.setItem(16, createItem(Material.OBSIDIAN,
                "<white><sprite:blocks:block/obsidian></white> <gradient:#fab387:#f38ba8><bold>ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ꜰʟᴏᴏʀ ʏ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴘʀᴏᴛᴇᴄᴛᴇᴅ ʏ ≤ <#f9e2af><bold>" + cfg.getDeathmatchFloorY() + "</bold>",
                " <#6c7086>(ᴘʀᴇᴠᴇɴᴛs ᴛɴᴛ ʙʀᴇᴀᴋɪɴɢ ᴀʀᴇɴᴀ ꜰʟᴏᴏʀ)",
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴇᴛ ᴜɴᴅᴇʀ ꜰᴇᴇᴛ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 20: World Teleporter
        inventory.setItem(20, createItem(Material.ENDER_PEARL,
                "<white><sprite:items:item/ender_pearl></white> <gradient:#89dceb:#a6e3a1><bold>ᴡᴏʀʟᴅ ᴛᴇʟᴇᴘᴏʀᴛᴇʀ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴄᴜʀʀᴇɴᴛ: <#f9e2af>" + player.getWorld().getName(),
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴘ ᴀʀᴇɴᴀ ᴡᴏʀʟᴅ",
                " <#fab387>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴛᴘ ʟᴏʙʙʏ ᴡᴏʀʟᴅ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 22: Deathmatch Spawns Manager
        List<String> rawSpawns = cfg.getRawDeathmatchSpawns();
        List<String> spawnLore = new ArrayList<>();
        spawnLore.add("<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        spawnLore.add(" <#a6adc8>sᴀᴠᴇᴅ sᴘᴀᴡɴs: <#f9e2af><bold>" + rawSpawns.size() + "</bold>");
        int previewLimit = Math.min(rawSpawns.size(), 5);
        for (int i = 0; i < previewLimit; i++) {
            spawnLore.add("  <#585b70>#" + (i + 1) + " <#89dceb>" + rawSpawns.get(i));
        }
        if (rawSpawns.size() > 5) {
            spawnLore.add("  <#6c7086>... (+" + (rawSpawns.size() - 5) + " ᴍᴏʀᴇ)");
        }
        spawnLore.add("");
        spawnLore.add(" <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴀᴅᴅ ᴄᴜʀʀᴇɴᴛ ᴘᴏs");
        spawnLore.add(" <#fab387>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴜɴᴅᴏ ʟᴀsᴛ");
        spawnLore.add(" <#f38ba8>▸ sʜɪꜰᴛ+ʀɪɢʜᴛ <#585b70>│ <#cdd6f4>ᴄʟᴇᴀʀ ᴀʟʟ");
        spawnLore.add("<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯");
        inventory.setItem(22, createItem(Material.ARMOR_STAND,
                "<white><sprite:items:item/armor_stand></white> <gradient:#f9e2af:#fab387><bold>ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ sᴘᴀᴡɴs</bold></gradient>",
                spawnLore.toArray(new String[0])
        ));

        // 24: Scan & Refresh Chest Holograms
        inventory.setItem(24, createItem(Material.CHEST,
                "<white><sprite:blocks:block/barrel_side></white> <gradient:#a6e3a1:#f9e2af><bold>ʀᴇꜰʀᴇsʜ ʜᴏʟᴏɢʀᴀᴍs</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ʀᴇ-sᴄᴀɴs ᴀʟʟ ʟᴏᴀᴅᴇᴅ ᴄʜᴜɴᴋs ᴀɴᴅ",
                " <#a6adc8>sᴘᴀᴡɴs 3-ᴛɪᴇʀ ᴄʜᴇsᴛ ʜᴏʟᴏɢʀᴀᴍs.",
                "",
                " <#a6e3a1>▸ ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴄᴀɴ ᴄʜᴜɴᴋs ɴᴏᴡ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 38: Force Start Match
        inventory.setItem(38, createItem(Material.EMERALD,
                "<white><sprite:items:item/emerald></white> <gradient:#a6e3a1:#89dceb><bold>ꜰᴏʀᴄᴇ sᴛᴀʀᴛ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>sᴛᴀᴛᴇ: <#f9e2af>" + state.name(),
                " <#a6adc8>ǫᴜᴇᴜᴇ: <#89dceb>" + plugin.getMatchManager().getMatchPlayers().size() + " ᴘʟᴀʏᴇʀs",
                "",
                " <#a6e3a1>▸ ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>sᴛᴀʀᴛ ɪᴍᴍᴇᴅɪᴀᴛᴇʟʏ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 39: Skip Phase
        inventory.setItem(39, createItem(Material.CLOCK,
                "<white><sprite:items:item/clock_00></white> <gradient:#fab387:#f38ba8><bold>sᴋɪᴘ ᴘʜᴀsᴇ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴘʜᴀsᴇ: <#f9e2af>" + plugin.getPhaseScheduler().getCurrentPhase().name(),
                "",
                " <#a6e3a1>▸ ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ᴀᴅᴠᴀɴᴄᴇ ɴᴇxᴛ ᴘʜᴀsᴇ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 40: Adjust Min Players
        inventory.setItem(40, createItem(Material.TOTEM_OF_UNDYING,
                "<white><sprite:items:item/totem_of_undying></white> <gradient:#cba6f7:#89b4fa><bold>ᴍɪɴ ᴘʟᴀʏᴇʀs: " + cfg.getMinPlayers() + "</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ʀᴇǫᴜɪʀᴇᴅ ᴛᴏ sᴛᴀʀᴛ: <#f9e2af><bold>" + cfg.getMinPlayers() + "</bold>",
                " <#6c7086>(sᴇᴛ ᴛᴏ 1 ꜰᴏʀ sᴏʟᴏ ᴛᴇsᴛɪɴɢ)",
                "",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>+1 ᴘʟᴀʏᴇʀ",
                " <#f38ba8>▸ ʀɪɢʜᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>-1 ᴘʟᴀʏᴇʀ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 41: Force Reset Arena (SWM Unload & Load)
        inventory.setItem(41, createItem(Material.TNT_MINECART,
                "<white><sprite:items:item/tnt_minecart></white> <gradient:#f38ba8:#fab387><bold>ʀᴇsᴇᴛ ᴀʀᴇɴᴀ (sᴡᴍ)</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6adc8>ᴜɴʟᴏᴀᴅs & ʀᴇʟᴏᴀᴅs <#89dceb>" + arenaName,
                " <#a6adc8>ꜰʀᴇsʜ ꜰʀᴏᴍ sʟɪᴍᴇ ᴍᴇᴍᴏʀʏ.",
                "",
                " <#f38ba8>▸ ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ʀᴇsᴇᴛ ᴍᴀᴘ ɴᴏᴡ",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));

        // 42: Reload Configs & Save SlimeWorld
        inventory.setItem(42, createItem(Material.WRITABLE_BOOK,
                "<white><sprite:items:item/writable_book></white> <gradient:#f9e2af:#a6e3a1><bold>ʀᴇʟᴏᴀᴅ & sᴀᴠᴇ</bold></gradient>",
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯",
                " <#a6e3a1>▸ ʟᴇꜰᴛ-ᴄʟɪᴄᴋ <#585b70>│ <#cdd6f4>ʀᴇʟᴏᴀᴅ ᴀʟʟ ʏᴍʟ",
                " <#f9e2af>▸ sʜɪꜰᴛ+ʟᴇꜰᴛ <#585b70>│ <#cdd6f4>/sᴡᴘ sᴀᴠᴇ " + arenaName,
                "<#45475a>⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯⎯"
        ));
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
        private final MiniMessage mm = MiniMessage.miniMessage();

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
                        feedback(player, "sᴀᴠᴇᴅ <#89dceb>ʟᴏʙʙʏ sᴘᴀᴡɴ<#cdd6f4>.");
                    } else if (click.isRightClick()) {
                        player.teleport(cfg.getLobbySpawn());
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#89dceb>ʟᴏʙʙʏ sᴘᴀᴡɴ<#cdd6f4>.");
                    }
                }
                case 11 -> {
                    if (click.isLeftClick()) {
                        cfg.setWaitingCage(player.getLocation());
                        feedback(player, "sᴀᴠᴇᴅ <#a6e3a1>ᴡᴀɪᴛɪɴɢ ᴄᴀɢᴇ<#cdd6f4>.");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getWaitingCageLocation(arenaWorld));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#a6e3a1>ᴡᴀɪᴛɪɴɢ ᴄᴀɢᴇ<#cdd6f4>.");
                    }
                }
                case 12 -> {
                    if (click.isLeftClick()) {
                        cfg.setDragonP1(player.getLocation());
                        feedback(player, "sᴀᴠᴇᴅ <#cba6f7>ᴅʀᴀɢᴏɴ sᴛᴀʀᴛ (ᴘ1)<#cdd6f4>.");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getDragonP1(arenaWorld));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#cba6f7>ᴅʀᴀɢᴏɴ sᴛᴀʀᴛ (ᴘ1)<#cdd6f4>.");
                    }
                }
                case 13 -> {
                    if (click.isLeftClick()) {
                        cfg.setDragonP2(player.getLocation());
                        feedback(player, "sᴀᴠᴇᴅ <#f5c2e7>ᴅʀᴀɢᴏɴ ᴇɴᴅ (ᴘ2)<#cdd6f4>.");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(cfg.getDragonP2(arenaWorld));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#f5c2e7>ᴅʀᴀɢᴏɴ ᴇɴᴅ (ᴘ2)<#cdd6f4>.");
                    }
                }
                case 14 -> {
                    if (click.isLeftClick()) {
                        cfg.setIslandCenter(player.getLocation());
                        feedback(player, "sᴀᴠᴇᴅ <#f9e2af>ɪsʟᴀɴᴅ ᴄᴇɴᴛᴇʀ<#cdd6f4>.");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(new Location(arenaWorld, cfg.getIslandCenterX(), 120.0, cfg.getIslandCenterZ()));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#f9e2af>ɪsʟᴀɴᴅ ᴄᴇɴᴛᴇʀ<#cdd6f4>.");
                    }
                }
                case 15 -> {
                    if (click.isLeftClick()) {
                        cfg.setDeathmatchCenter(player.getLocation());
                        feedback(player, "sᴀᴠᴇᴅ <#f38ba8>ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ᴄᴇɴᴛᴇʀ<#cdd6f4>.");
                    } else if (click.isRightClick() && arenaWorld != null) {
                        player.teleport(new Location(arenaWorld, cfg.getDeathmatchCenterX(), cfg.getDeathmatchFloorY() + 1.0, cfg.getDeathmatchCenterZ()));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#f38ba8>ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ᴄᴇɴᴛᴇʀ<#cdd6f4>.");
                    }
                }
                case 16 -> {
                    if (click.isLeftClick()) {
                        int floorY = player.getLocation().getBlockY() - 1;
                        cfg.setDeathmatchFloorY(floorY);
                        feedback(player, "sᴀᴠᴇᴅ ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ ꜰʟᴏᴏʀ ʏ ≤ <#f9e2af>" + floorY + "<#cdd6f4>.");
                    }
                }
                case 20 -> {
                    if (click.isLeftClick() && arenaWorld != null) {
                        player.teleport(cfg.getWaitingCageLocation(arenaWorld));
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ ᴀʀᴇɴᴀ <#89dceb>" + arenaWorld.getName() + "<#cdd6f4>.");
                    } else if (click.isRightClick()) {
                        player.teleport(cfg.getLobbySpawn());
                        feedback(player, "ᴛᴇʟᴇᴘᴏʀᴛᴇᴅ ᴛᴏ <#fab387>ʟᴏʙʙʏ ᴡᴏʀʟᴅ<#cdd6f4>.");
                    }
                }
                case 22 -> {
                    if (click == ClickType.LEFT) {
                        int count = cfg.addDeathmatchSpawn(player.getLocation());
                        feedback(player, "ᴀᴅᴅᴇᴅ ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ sᴘᴀᴡɴ <#f9e2af>#" + count + "<#cdd6f4>.");
                    } else if (click == ClickType.RIGHT) {
                        if (cfg.removeLastDeathmatchSpawn()) {
                            feedback(player, "ʀᴇᴍᴏᴠᴇᴅ ʟᴀsᴛ ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ sᴘᴀᴡɴ.");
                        }
                    } else if (click == ClickType.SHIFT_RIGHT) {
                        cfg.clearDeathmatchSpawns();
                        feedback(player, "ᴄʟᴇᴀʀᴇᴅ ᴀʟʟ ᴅᴇᴀᴛʜᴍᴀᴛᴄʜ sᴘᴀᴡɴs.");
                    }
                }
                case 24 -> {
                    if (arenaWorld != null) {
                        plugin.getHologramManager().removeAllHolograms();
                        for (Chunk chunk : arenaWorld.getLoadedChunks()) {
                            plugin.getHologramManager().scanChunkForChests(chunk);
                        }
                        feedback(player, "ʀᴇꜰʀᴇsʜᴇᴅ ᴀʟʟ <#a6e3a1>ᴄʜᴇsᴛ ʜᴏʟᴏɢʀᴀᴍs<#cdd6f4>.");
                    }
                }
                case 38 -> {
                    player.closeInventory();
                    Bukkit.dispatchCommand(player, "cartbr forcestart");
                    return;
                }
                case 39 -> {
                    if (plugin.getMatchManager().forceSkipPhase()) {
                        feedback(player, "sᴋɪᴘᴘᴇᴅ ᴛᴏ ᴘʜᴀsᴇ <#f9e2af>" + plugin.getPhaseScheduler().getCurrentPhase().name() + "<#cdd6f4>.");
                    }
                }
                case 40 -> {
                    int current = cfg.getMinPlayers();
                    if (click.isLeftClick()) {
                        cfg.setMinPlayers(Math.min(16, current + 1));
                    } else if (click.isRightClick()) {
                        cfg.setMinPlayers(Math.max(1, current - 1));
                    }
                    feedback(player, "ᴍɪɴ ᴘʟᴀʏᴇʀs sᴇᴛ ᴛᴏ <#f9e2af>" + cfg.getMinPlayers() + "<#cdd6f4>.");
                }
                case 41 -> {
                    player.closeInventory();
                    feedback(player, "ʀᴇsᴇᴛᴛɪɴɢ sʟɪᴍᴇ ᴀʀᴇɴᴀ ᴏɴ ʀᴀᴍ...");
                    plugin.getMatchManager().resetSingleArena();
                    return;
                }
                case 42 -> {
                    if (click == ClickType.SHIFT_LEFT) {
                        player.closeInventory();
                        Bukkit.dispatchCommand(player, "swp save " + cfg.getTemplateWorldName());
                        feedback(player, "sᴀᴠɪɴɢ sʟɪᴍᴇ ᴡᴏʀʟᴅ <#f9e2af>" + cfg.getTemplateWorldName() + "<#cdd6f4>...");
                        return;
                    } else if (click.isLeftClick()) {
                        cfg.reload();
                        plugin.getLootConfig().load();
                        plugin.getLocaleManager().reload();
                        feedback(player, "ʀᴇʟᴏᴀᴅᴇᴅ ᴀʟʟ ᴄᴏɴꜰɪɢᴜʀᴀᴛɪᴏɴ ꜰɪʟᴇs.");
                    }
                }
                default -> {
                    return;
                }
            }

            gui.render();
        }

        private void feedback(Player player, String miniMsg) {
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            player.sendMessage(mm.deserialize("<white><sprite:items:item/tnt_minecart></white> <gradient:#cba6f7:#f5c2e7><bold>ᴀᴅᴍɪɴ</bold></gradient> <#585b70>› <#a6e3a1>✔ <#cdd6f4>" + miniMsg));
        }
    }
}
