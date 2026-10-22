package me.nhan.cartbr.config;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.loot.ChestTier;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.Damageable;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.util.*;
import java.util.concurrent.ThreadLocalRandom;

public class LootConfig {

    private final CartBRPlugin plugin;
    private final Map<ChestTier, TierLootTable> tierTables = new EnumMap<>(ChestTier.class);

    public LootConfig(CartBRPlugin plugin) {
        this.plugin = plugin;
        load();
    }

    public void load() {
        File file = new File(plugin.getDataFolder(), "loot.yml");
        if (!file.exists()) {
            plugin.saveResource("loot.yml", false);
        }
        FileConfiguration lootCfg = YamlConfiguration.loadConfiguration(file);
        this.tierTables.clear();

        for (ChestTier tier : ChestTier.values()) {
            ConfigurationSection sec = lootCfg.getConfigurationSection("tiers." + tier.name());
            if (sec == null) continue;
            int min = sec.getInt("min-items", 4);
            int max = sec.getInt("max-items", 7);
            List<LootEntry> entries = new ArrayList<>();

            for (String raw : sec.getStringList("items")) {
                LootEntry entry = parseEntry(raw, tier);
                if (entry != null) entries.add(entry);
            }
            tierTables.put(tier, new TierLootTable(min, max, entries));
        }
    }

    private LootEntry parseEntry(String raw, ChestTier tier) {
        try {
            String[] parts = raw.split(":");
            Material mat = Material.matchMaterial(parts[0].trim());
            if (mat == null) return null;

            int min = parts.length > 1 ? Integer.parseInt(parts[1].trim()) : 1;
            int max = parts.length > 2 ? Integer.parseInt(parts[2].trim()) : min;
            double chance = parts.length > 3 ? Double.parseDouble(parts[3].trim()) : 1.0;

            Map<Enchantment, Integer> enchants = new HashMap<>();
            if (parts.length > 4 && !parts[4].isBlank()) {
                for (String enchRaw : parts[4].split(",")) {
                    String[] kv = enchRaw.split("=");
                    Enchantment ench = Registry.ENCHANTMENT.get(NamespacedKey.minecraft(kv[0].trim().toLowerCase(Locale.ROOT)));
                    int lvl = kv.length > 1 ? Integer.parseInt(kv[1].trim()) : 1;
                    if (ench != null) enchants.put(ench, lvl);
                }
            }

            if (isArmor(mat) && !enchants.containsKey(Enchantment.BLAST_PROTECTION)) {
                int defaultBlastLvl = switch (tier) {
                    case TIER_1 -> 2;
                    case TIER_2 -> 3;
                    case TIER_3 -> 4;
                };
                enchants.put(Enchantment.BLAST_PROTECTION, defaultBlastLvl);
            }

            return new LootEntry(mat, min, max, chance, enchants);
        } catch (Exception e) {
            plugin.getLogger().warning("Invalid loot entry: " + raw);
            return null;
        }
    }

    private boolean isArmor(Material mat) {
        String name = mat.name();
        return name.endsWith("_HELMET") || name.endsWith("_CHESTPLATE")
                || name.endsWith("_LEGGINGS") || name.endsWith("_BOOTS");
    }

    public List<ItemStack> generateLoot(ChestTier tier) {
        TierLootTable table = tierTables.get(tier);
        if (table == null || table.entries.isEmpty()) return Collections.emptyList();

        ThreadLocalRandom rand = ThreadLocalRandom.current();
        int targetCount = rand.nextInt(table.minItems, table.maxItems + 1);

        List<LootEntry> pool = new ArrayList<>(table.entries);
        Collections.shuffle(pool, rand);

        List<ItemStack> result = new ArrayList<>();
        int maxStack = plugin.getConfigManager().getTntMinecartMaxStack();
        for (LootEntry entry : pool) {
            if (result.size() >= targetCount) break;
            if (rand.nextDouble() <= entry.chance) {
                result.add(entry.buildItem(maxStack));
            }
        }
        return result;
    }

    public ItemStack createTntMinecartItem(int amount) {
        ItemStack item = new ItemStack(Material.TNT_MINECART, amount);
        int maxStack = plugin.getConfigManager().getTntMinecartMaxStack();
        if (maxStack > 1) {
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setMaxStackSize(Math.min(maxStack, 99));
                item.setItemMeta(meta);
            }
        }
        return item;
    }

    private record TierLootTable(int minItems, int maxItems, List<LootEntry> entries) {}

    private record LootEntry(Material material, int min, int max, double chance, Map<Enchantment, Integer> enchants) {
        public ItemStack buildItem(int tntCartMaxStack) {
            int amount = min >= max ? min : ThreadLocalRandom.current().nextInt(min, max + 1);
            ItemStack item = new ItemStack(material, amount);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                for (var entry : enchants.entrySet()) {
                    meta.addEnchant(entry.getKey(), entry.getValue(), true);
                }
                if (material == Material.TNT_MINECART && tntCartMaxStack > 1) {
                    meta.setMaxStackSize(Math.min(tntCartMaxStack, 99));
                }
                if (material == Material.SHIELD && meta instanceof Damageable dmg) {
                    int maxDurability = Material.SHIELD.getMaxDurability();
                    dmg.setDamage((int) (maxDurability * 0.70));
                }
                item.setItemMeta(meta);
            }
            return item;
        }
    }
}
