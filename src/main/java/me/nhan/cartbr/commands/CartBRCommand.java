package me.nhan.cartbr.commands;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.arena.GameState;
import me.nhan.cartbr.config.ConfigManager;
import me.nhan.cartbr.gui.AdminGUI;
import me.nhan.cartbr.gui.SettingsGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public class CartBRCommand implements CommandExecutor, TabCompleter {

    private final CartBRPlugin plugin;

    public CartBRCommand(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can execute this command.");
            return true;
        }

        if (args.length == 0) {
            if (player.hasPermission("cartbr.admin")) {
                new AdminGUI(plugin, player).open();
            } else {
                sendHelp(player);
            }
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "join" -> plugin.getMatchManager().joinQueue(player);
            case "leave" -> plugin.getMatchManager().leaveQueue(player);
            case "settings", "lang" -> new SettingsGUI(plugin, player).open();

            case "admin", "gui" -> {
                if (!player.hasPermission("cartbr.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                new AdminGUI(plugin, player).open();
            }

            case "forcestart" -> {
                if (!player.hasPermission("cartbr.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                GameState state = plugin.getMatchManager().getState();
                if (state == GameState.WAITING || state == GameState.STARTING) {
                    if (plugin.getMatchManager().getMatchPlayers().isEmpty()) {
                        player.sendMessage(ChatColor.RED + "Cannot force start with 0 players in queue!");
                        return true;
                    }
                    plugin.getMatchManager().startDragonDropPhase();
                    player.sendMessage(ChatColor.GREEN + "✔ Force-started the CartBR match!");
                } else {
                    player.sendMessage(ChatColor.RED + "Match cannot be force-started in state: " + state);
                }
            }

            case "skip", "skipphase" -> {
                if (!player.hasPermission("cartbr.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (plugin.getMatchManager().forceSkipPhase()) {
                    player.sendMessage(ChatColor.GREEN + "✔ Skipped to phase: " + plugin.getPhaseScheduler().getCurrentPhase().name());
                } else {
                    player.sendMessage(ChatColor.RED + "Can only skip phases while a match is running!");
                }
            }

            case "reload" -> {
                if (!player.hasPermission("cartbr.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                plugin.getConfigManager().reload();
                plugin.getLootConfig().load();
                plugin.getLocaleManager().reload();
                player.sendMessage(ChatColor.GREEN + "✔ CartBR configuration, loot tables, and language files reloaded!");
            }

            case "setup" -> {
                if (!player.hasPermission("cartbr.admin")) {
                    player.sendMessage(ChatColor.RED + "You do not have permission!");
                    return true;
                }
                if (args.length < 2) {
                    new AdminGUI(plugin, player).open();
                    return true;
                }
                handleSetupCommand(player, args);
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void handleSetupCommand(Player player, String[] args) {
        String target = args[1].toLowerCase(Locale.ROOT);
        Location loc = player.getLocation();
        ConfigManager cfg = plugin.getConfigManager();

        switch (target) {
            case "lobby" -> {
                cfg.setLobbySpawn(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Lobby Spawn at your current location!");
            }
            case "waiting" -> {
                cfg.setWaitingCage(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Waiting Cage location!");
            }
            case "p1" -> {
                cfg.setDragonP1(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Dragon Flight Start (P1)!");
            }
            case "p2" -> {
                cfg.setDragonP2(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Dragon Flight End (P2)!");
            }
            case "islandcenter" -> {
                cfg.setIslandCenter(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Main Island Center!");
            }
            case "dmcenter" -> {
                cfg.setDeathmatchCenter(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Deathmatch Arena Center!");
            }
            case "dmfloor" -> {
                int floorY = loc.getBlockY() - 1;
                cfg.setDeathmatchFloorY(floorY);
                player.sendMessage(ChatColor.GREEN + "✔ Saved Deathmatch Floor Y = " + floorY + " (Protected from TNT explosions)!");
            }
            case "adddmspawn" -> {
                int count = cfg.addDeathmatchSpawn(loc);
                player.sendMessage(ChatColor.GREEN + "✔ Added Deathmatch Spawn #" + count + "!");
            }
            case "cleardmspawns" -> {
                cfg.clearDeathmatchSpawns();
                player.sendMessage(ChatColor.YELLOW + "✔ Cleared all Deathmatch Spawns!");
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown setup option: " + target);
        }
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "=== CartBR Commands ===");
        player.sendMessage(ChatColor.GRAY + "/cartbr join " + ChatColor.WHITE + "- Join the Battle Royale queue");
        player.sendMessage(ChatColor.GRAY + "/cartbr leave " + ChatColor.WHITE + "- Leave the current queue");
        player.sendMessage(ChatColor.GRAY + "/cartbr settings " + ChatColor.WHITE + "- Open Language & Settings GUI");
        if (player.hasPermission("cartbr.admin")) {
            player.sendMessage(ChatColor.GOLD + "/cartbr admin " + ChatColor.WHITE + "- Open Admin Setup & Control GUI");
            player.sendMessage(ChatColor.GOLD + "/cartbr forcestart " + ChatColor.WHITE + "- Force start current match");
            player.sendMessage(ChatColor.GOLD + "/cartbr skip " + ChatColor.WHITE + "- Skip to next phase");
            player.sendMessage(ChatColor.GOLD + "/cartbr reload " + ChatColor.WHITE + "- Reload all YAML configs");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("join", "leave", "settings"));
            if (sender.hasPermission("cartbr.admin")) {
                list.addAll(Arrays.asList("admin", "forcestart", "skip", "setup", "reload"));
            }
            return filter(list, args[0]);
        }
        if (args.length == 2 && args[0].equalsIgnoreCase("setup") && sender.hasPermission("cartbr.admin")) {
            return filter(Arrays.asList(
                    "lobby", "waiting", "p1", "p2", "islandcenter",
                    "dmcenter", "dmfloor", "adddmspawn", "cleardmspawns"
            ), args[1]);
        }
        return Collections.emptyList();
    }

    private List<String> filter(List<String> options, String prefix) {
        String lower = prefix.toLowerCase(Locale.ROOT);
        return options.stream().filter(s -> s.startsWith(lower)).toList();
    }
}
