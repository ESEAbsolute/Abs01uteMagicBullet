package com.eseabsolute.magicbullet.utils;

import org.bukkit.configuration.file.YamlConfiguration;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.text.MessageFormat;
import java.util.HashMap;
import java.util.Map;

public class I18nUtils {
    private static final String DEFAULT_LANG = "en_us";
    private static String currentLang = DEFAULT_LANG;
    private static final Map<String, YamlConfiguration> translations = new HashMap<>();

    public static void loadLanguage(String langCode) {
        currentLang = langCode;
        translations.clear();
        translations.put(DEFAULT_LANG, loadYaml(DEFAULT_LANG));
        if (!DEFAULT_LANG.equals(langCode)) {
            translations.put(langCode, loadYaml(langCode));
        }
    }

    private static YamlConfiguration loadYaml(String langCode) {
        String path = "lang/" + langCode + ".yml";
        InputStream input = I18nUtils.class.getClassLoader().getResourceAsStream(path);
        if (input == null) {
            System.err.println("Could not load language file: " + path);
            return new YamlConfiguration();
        }
        return YamlConfiguration.loadConfiguration(new InputStreamReader(input, StandardCharsets.UTF_8));
    }

    public static String get(String key, Object... args) {
        String raw = getRaw(key);
        return MessageFormat.format(raw, args);
    }

    private static String getRaw(String key) {
        YamlConfiguration current = translations.get(currentLang);
        if (current != null && current.contains(key)) {
            return current.getString(key);
        }

        YamlConfiguration fallback = translations.get(DEFAULT_LANG);
        if (fallback != null && fallback.contains(key)) {
            return fallback.getString(key);
        }

        return "!" + key + "!";
    }

    public static String getCurrentLang() {
        return currentLang;
    }
}
