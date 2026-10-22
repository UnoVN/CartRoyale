package me.nhan.cartbr.locale;

import me.nhan.cartbr.CartBRPlugin;
import me.nhan.cartbr.loot.ChestTier;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.title.Title;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.persistence.PersistentDataType;

import java.io.File;
import java.time.Duration;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

public class LocaleManager {

    private final CartBRPlugin plugin;
    private final NamespacedKey langKey;
    private final MiniMessage mm = MiniMessage.miniMessage();
    private final Map<Language, FileConfiguration> configs = new EnumMap<>(Language.class);

    public LocaleManager(CartBRPlugin plugin) {
        this.plugin = plugin;
        this.langKey = new NamespacedKey(plugin, "language");
        reload();
    }

    public void reload() {
        configs.clear();
        for (Language lang : Language.values()) {
            File file = new File(plugin.getDataFolder(), lang.getFileName());
            if (!file.exists()) {
                plugin.saveResource(lang.getFileName(), false);
            }
            configs.put(lang, YamlConfiguration.loadConfiguration(file));
        }
    }

    public Language getLanguage(Player player) {
        String code = player.getPersistentDataContainer().get(langKey, PersistentDataType.STRING);
        return Language.fromCode(code);
    }

    public void setLanguage(Player player, Language language) {
        player.getPersistentDataContainer().set(langKey, PersistentDataType.STRING, language.getCode());
    }

    public String getRaw(Language lang, String path) {
        FileConfiguration cfg = configs.getOrDefault(lang, configs.get(Language.EN));
        return cfg.getString(path, path);
    }

    public Component getComponent(Language lang, String path, String... placeholders) {
        String raw = replacePlaceholders(getRaw(lang, path), placeholders);
        return mm.deserialize(raw).decoration(TextDecoration.ITALIC, false);
    }

    public Component getComponent(Player player, String path, String... placeholders) {
        return getComponent(getLanguage(player), path, placeholders);
    }

    public List<Component> getComponentList(Player player, String path, String... placeholders) {
        Language lang = getLanguage(player);
        FileConfiguration cfg = configs.getOrDefault(lang, configs.get(Language.EN));
        List<String> lines = cfg.getStringList(path);
        List<Component> result = new ArrayList<>(lines.size());
        for (String line : lines) {
            result.add(mm.deserialize(replacePlaceholders(line, placeholders))
                    .decoration(TextDecoration.ITALIC, false));
        }
        return result;
    }

    public void sendMessage(Player player, String path, String... placeholders) {
        Language lang = getLanguage(player);
        String prefix = getRaw(lang, "prefix");
        String body = replacePlaceholders(getRaw(lang, path), placeholders);
        if (body.contains("\n")) {
            player.sendMessage(mm.deserialize(body));
        } else {
            player.sendMessage(mm.deserialize(prefix + body));
        }
    }

    public void sendActionBar(Player player, String path, String... placeholders) {
        player.sendActionBar(getComponent(player, path, placeholders));
    }

    public void sendTitle(Player player, String basePath, long inMs, long stayMs, long outMs, String... placeholders) {
        Component title = getComponent(player, basePath + ".title", placeholders);
        Component subtitle = getComponent(player, basePath + ".subtitle", placeholders);
        Title.Times times = Title.Times.times(Duration.ofMillis(inMs), Duration.ofMillis(stayMs), Duration.ofMillis(outMs));
        player.showTitle(Title.title(title, subtitle, times));
    }

    public Component getHologramText(ChestTier tier) {
        String path = switch (tier) {
            case TIER_1 -> "chest-holograms.tier-1";
            case TIER_2 -> "chest-holograms.tier-2";
            case TIER_3 -> "chest-holograms.tier-3";
        };
        return mm.deserialize(getRaw(Language.EN, path).trim());
    }

    public Component getLootedHologramText() {
        return mm.deserialize(getRaw(Language.EN, "chest-holograms.looted").trim());
    }

    private String replacePlaceholders(String input, String... placeholders) {
        if (input == null || placeholders == null) return "";
        String out = input;
        for (int i = 0; i + 1 < placeholders.length; i += 2) {
            out = out.replace(placeholders[i], placeholders[i + 1]);
        }
        return out;
    }
}
