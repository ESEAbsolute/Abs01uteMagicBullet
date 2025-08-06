package com.eseabsolute.magicbullet.messages;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class I18n {
    private final Abs01uteMagicBulletPlugin plugin;
    private final String[] langFiles = {
            "lang/zh_cn.yml",
            "lang/en_us.yml"
    };

    private final String fallbackLang;
    private final String currentLang;

    private final Map<String, String> translations = new HashMap<>();

    I18n(Abs01uteMagicBulletPlugin plugin, String primaryLang, String fallbackLang) {
        this.plugin = plugin;
        this.currentLang = primaryLang;
        this.fallbackLang = fallbackLang;
        loadLanguage();
    }

    public void loadLanguage() {
        YamlConfiguration primaryLangFile = loadYaml(currentLang);
        YamlConfiguration fallbackLangFile = loadYaml(fallbackLang);

        for (String key : primaryLangFile.getKeys(true)) {
            if (primaryLangFile.isString(key)) {
                String value = primaryLangFile.getString(key);
                translations.put(key, value);
            }
        }

        for (String key : fallbackLangFile.getKeys(true)) {
            if (fallbackLangFile.isString(key)) {
                String value = fallbackLangFile.getString(key);
                translations.putIfAbsent(key, value);
            }
        }
    }

    private YamlConfiguration loadYaml(String langCode) {
        File langDirectory = new File(plugin.getDataFolder(), "lang");

        if (!langDirectory.exists()) {
            for (String path : langFiles) {
                plugin.saveResource(path, false);
            }
        }

        String path = "lang/" + langCode + ".yml";
        File langFile = new File(plugin.getDataFolder(), path);
        if (langFile.exists() && langFile.isFile()) {
            return YamlConfiguration.loadConfiguration(langFile);
        } else {
            plugin.getMessageUtils().sendMessageWithoutI18n(
                    plugin.getServer().getConsoleSender(), MessageLevel.ERROR,
                    "Could not load language file: " + path
            );
            return new YamlConfiguration();
        }
    }

    public String get(String key, String... args) {
        String raw = getRaw(key);
        if (raw == null) {
            return "!%s!".formatted(key);
        }
        return Translator.get(raw, args);
    }

    private String getRaw(String key) {
        return translations.get(key);
    }

    public String getCurrentLang() {
        return currentLang;
    }

    public String getFallbackLang() {
        return fallbackLang;
    }
}
