package me.nhan.cartbr.loot;

import me.nhan.cartbr.CartBRPlugin;
import org.bukkit.Chunk;
import org.bukkit.Color;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.entity.Display;
import org.bukkit.entity.TextDisplay;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.HashMap;
import java.util.Map;

public class ChestHologramManager implements Listener {

    public record BlockPos(int x, int y, int z) {
        public static BlockPos of(Block b) {
            return new BlockPos(b.getX(), b.getY(), b.getZ());
        }
    }

    private final CartBRPlugin plugin;
    private final Map<BlockPos, TextDisplay> holograms = new HashMap<>();

    public ChestHologramManager(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent event) {
        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        if (arenaWorld != null && event.getWorld().equals(arenaWorld)) {
            scanChunkForChests(event.getChunk());
        }
    }

    public void scanChunkForChests(Chunk chunk) {
        for (BlockState state : chunk.getTileEntities()) {
            ChestTier tier = ChestTier.fromMaterial(state.getType());
            if (tier == null) continue;

            BlockPos pos = BlockPos.of(state.getBlock());
            TextDisplay existing = holograms.get(pos);
            if (existing != null && existing.isValid()) continue;

            Location holoLoc = state.getBlock().getLocation().add(0.5, 1.35, 0.5);
            TextDisplay display = state.getWorld().spawn(holoLoc, TextDisplay.class, td -> {
                td.setBillboard(Display.Billboard.CENTER);
                td.setShadowed(true);
                td.setBackgroundColor(Color.fromARGB(100, 30, 30, 46));
                td.setViewRange(0.3f);
                td.setPersistent(false);
                if (plugin.getLootManager().isChestOpened(pos)) {
                    td.text(plugin.getLocaleManager().getLootedHologramText());
                } else {
                    td.text(plugin.getLocaleManager().getHologramText(tier));
                }
            });

            holograms.put(pos, display);
        }
    }

    public void markChestLooted(BlockPos pos) {
        TextDisplay display = holograms.get(pos);
        if (display != null && display.isValid()) {
            display.text(plugin.getLocaleManager().getLootedHologramText());
        }
    }

    public void removeAllHolograms() {
        for (TextDisplay td : holograms.values()) {
            if (td != null && td.isValid()) {
                td.remove();
            }
        }
        holograms.clear();
    }
}
