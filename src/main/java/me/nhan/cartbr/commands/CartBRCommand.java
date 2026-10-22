package me.nhan.cartbr.commands;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.arena.GameState;
import me.nhan.cartbr.gui.SettingsGUI;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.configuration.file.FileConfiguration;
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
            sendHelp(player);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "join" -> plugin.getMatchManager().joinQueue(player);
            case "leave" -> plugin.getMatchManager().leaveQueue(player);
            case "settings", "lang" -> new SettingsGUI(plugin, player).open();

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
                handleSetupCommand(player, args);
            }

            default -> sendHelp(player);
        }
        return true;
    }

    private void handleSetupCommand(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(ChatColor.YELLOW + "Usage: /cartbr setup <lobby|waiting|p1|p2|islandcenter|dmcenter|dmfloor|adddmspawn|cleardmspawns>");
            return;
        }

        String target = args[1].toLowerCase(Locale.ROOT);
        Location loc = player.getLocation();
        FileConfiguration cfg = plugin.getConfig();

        switch (target) {
            case "lobby" -> {
                cfg.set("lobby-spawn.world", loc.getWorld().getName());
                cfg.set("lobby-spawn.x", round(loc.getX()));
                cfg.set("lobby-spawn.y", round(loc.getY()));
                cfg.set("lobby-spawn.z", round(loc.getZ()));
                cfg.set("lobby-spawn.yaw", round(loc.getYaw()));
                cfg.set("lobby-spawn.pitch", round(loc.getPitch()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Lobby Spawn at your current location!");
            }
            case "waiting" -> {
                cfg.set("arena.waiting-cage.x", round(loc.getX()));
                cfg.set("arena.waiting-cage.y", round(loc.getY()));
                cfg.set("arena.waiting-cage.z", round(loc.getZ()));
                cfg.set("arena.waiting-cage.yaw", round(loc.getYaw()));
                cfg.set("arena.waiting-cage.pitch", round(loc.getPitch()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Waiting Cage location!");
            }
            case "p1" -> {
                cfg.set("arena.dragon-path.p1.x", round(loc.getX()));
                cfg.set("arena.dragon-path.p1.y", round(loc.getY()));
                cfg.set("arena.dragon-path.p1.z", round(loc.getZ()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Dragon Flight Start (P1)!");
            }
            case "p2" -> {
                cfg.set("arena.dragon-path.p2.x", round(loc.getX()));
                cfg.set("arena.dragon-path.p2.y", round(loc.getY()));
                cfg.set("arena.dragon-path.p2.z", round(loc.getZ()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Dragon Flight End (P2)!");
            }
            case "islandcenter" -> {
                cfg.set("arena.island-center.x", round(loc.getX()));
                cfg.set("arena.island-center.z", round(loc.getZ()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Main Island Center!");
            }
            case "dmcenter" -> {
                cfg.set("arena.deathmatch.center-x", round(loc.getX()));
                cfg.set("arena.deathmatch.center-z", round(loc.getZ()));
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Deathmatch Arena Center!");
            }
            case "dmfloor" -> {
                int floorY = loc.getBlockY() - 1;
                cfg.set("arena.deathmatch.floor-y", floorY);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Saved Deathmatch Floor Y = " + floorY + " (Protected from TNT explosions)!");
            }
            case "adddmspawn" -> {
                List<String> spawns = new ArrayList<>(cfg.getStringList("arena.deathmatch.spawns"));
                String serialized = String.format(Locale.US, "%.1f,%.1f,%.1f,%.1f,%.1f",
                        loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
                spawns.add(serialized);
                cfg.set("arena.deathmatch.spawns", spawns);
                plugin.saveConfig();
                player.sendMessage(ChatColor.GREEN + "✔ Added Deathmatch Spawn #" + spawns.size() + ": " + serialized);
            }
            case "cleardmspawns" -> {
                cfg.set("arena.deathmatch.spawns", new ArrayList<String>());
                plugin.saveConfig();
                player.sendMessage(ChatColor.YELLOW + "✔ Cleared all Deathmatch Spawns!");
            }
            default -> player.sendMessage(ChatColor.RED + "Unknown setup option: " + target);
        }
    }

    private double round(double val) {
        return Math.round(val * 10.0) / 10.0;
    }

    private void sendHelp(Player player) {
        player.sendMessage(ChatColor.LIGHT_PURPLE + "=== CartBR Commands ===");
        player.sendMessage(ChatColor.GRAY + "/cartbr join " + ChatColor.WHITE + "- Join the Battle Royale queue");
        player.sendMessage(ChatColor.GRAY + "/cartbr leave " + ChatColor.WHITE + "- Leave the current queue");
        player.sendMessage(ChatColor.GRAY + "/cartbr settings " + ChatColor.WHITE + "- Open Language & Settings GUI");
        if (player.hasPermission("cartbr.admin")) {
            player.sendMessage(ChatColor.GOLD + "/cartbr forcestart " + ChatColor.WHITE + "- Force start current match");
            player.sendMessage(ChatColor.GOLD + "/cartbr setup <...> " + ChatColor.WHITE + "- Configure arena coordinates");
            player.sendMessage(ChatColor.GOLD + "/cartbr reload " + ChatColor.WHITE + "- Reload all YAML configs");
        }
    }

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 1) {
            List<String> list = new ArrayList<>(Arrays.asList("join", "leave", "settings"));
            if (sender.hasPermission("cartbr.admin")) {
                list.addAll(Arrays.asList("forcestart", "setup", "reload"));
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
