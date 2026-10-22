package me.nhan.cartbr.loot;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.arena.GameState;
import me.nhan.cartbr.loot.ChestHologramManager.BlockPos;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootManager implements Listener {

    private final CartBRPlugin plugin;
    private final Set<BlockPos> openedChests = new HashSet<>();

    public LootManager(CartBRPlugin plugin) {
        this.plugin = plugin;
    }

    public void resetOpenedChests() {
        openedChests.clear();
    }

    public boolean isChestOpened(BlockPos pos) {
        return openedChests.contains(pos);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onOpenChest(PlayerInteractEvent event) {
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        Block block = event.getClickedBlock();
        if (block == null) return;

        World arenaWorld = plugin.getSlimeArenaManager().getCurrentArenaWorld();
        if (arenaWorld == null || !block.getWorld().equals(arenaWorld)) return;

        ChestTier tier = ChestTier.fromMaterial(block.getType());
        if (tier == null) return;

        Player player = event.getPlayer();
        if (!plugin.getMatchManager().getAlivePlayers().contains(player.getUniqueId())) {
            event.setCancelled(true);
            return;
        }

        BlockPos pos = BlockPos.of(block);
        if (openedChests.contains(pos)) return;

        openedChests.add(pos);
        plugin.getHologramManager().markChestLooted(pos);

        List<ItemStack> lootItems = plugin.getLootConfig().generateLoot(tier);
        if (block.getState() instanceof Container container) {
            Inventory inv = container.getInventory();
            inv.clear();
            List<Integer> slots = new ArrayList<>(inv.getSize());
            for (int i = 0; i < inv.getSize(); i++) slots.add(i);
            Collections.shuffle(slots, ThreadLocalRandom.current());

            for (int i = 0; i < Math.min(lootItems.size(), slots.size()); i++) {
                inv.setItem(slots.get(i), lootItems.get(i));
            }
        } else if (block.getType() == Material.ENDER_CHEST) {
            event.setCancelled(true);
            Location dropLoc = block.getLocation().add(0.5, 1.0, 0.5);
            for (ItemStack item : lootItems) {
                block.getWorld().dropItemNaturally(dropLoc, item);
            }
            player.playSound(dropLoc, Sound.BLOCK_ENDER_CHEST_OPEN, 1f, 1f);
        }

        String actionbarPath = switch (tier) {
            case TIER_1 -> "loot.tier-1-actionbar";
            case TIER_2 -> "loot.tier-2-actionbar";
            case TIER_3 -> "loot.tier-3-actionbar";
        };
        plugin.getLocaleManager().sendActionBar(player, actionbarPath);
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        filterExplosionBlocks(event.blockList());
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        filterExplosionBlocks(event.blockList());
    }

    private void filterExplosionBlocks(List<Block> blocks) {
        int dmFloorY = plugin.getConfigManager().getDeathmatchFloorY();
        GameState state = plugin.getMatchManager().getState();

        blocks.removeIf(block -> {
            if (ChestTier.isLootContainer(block.getType())) {
                return true;
            }
            return state == GameState.DEATHMATCH && block.getY() <= dmFloorY;
        });
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Block block = event.getBlock();
        if (ChestTier.isLootContainer(block.getType())) {
            event.setCancelled(true);
            return;
        }
        if (plugin.getMatchManager().getState() == GameState.DEATHMATCH
                && block.getY() <= plugin.getConfigManager().getDeathmatchFloorY()) {
            event.setCancelled(true);
        }
    }
}
