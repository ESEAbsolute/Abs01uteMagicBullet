package com.eseabsolute.magicbullet.messages;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;

public enum MessageLevel {
    NONE(""),
    SUCCESS("&a"),
    INFO("&b"),
    WARNING("&e"),
    ERROR("&c");

    private final String colorCode;

    MessageLevel(String colorCode) {
        this.colorCode = colorCode;
    }

    public String getColor() {
        return colorCode;
    }

    private static final Map<Level, MessageLevel> levelMap = new HashMap<>();
    static {
        levelMap.put(Level.SEVERE, ERROR);
        levelMap.put(Level.WARNING, WARNING);
        levelMap.put(Level.INFO, INFO);
        levelMap.put(Level.CONFIG, INFO);
        levelMap.put(Level.FINE, INFO);
        levelMap.put(Level.FINER, INFO);
        levelMap.put(Level.FINEST, INFO);
        levelMap.put(Level.ALL, NONE);
        levelMap.put(Level.OFF, NONE);
    }

    public static MessageLevel fromJavaUtilLevel(Level level) {
        return levelMap.getOrDefault(level, INFO); // Default to INFO
    }
}
