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
