package com.eseabsolute.magicbullet.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Formatter {
    private static final Pattern HEX_COLOR_PATTERN = Pattern.compile("&#([A-Fa-f0-9]{6})");
    private static final Pattern LEGACY_COLOR_PATTERN = Pattern.compile("&([0-9a-fk-orA-FK-OR])");

    public static String format(String message) {
        if (message == null) return "null";

        Matcher hexMatcher = HEX_COLOR_PATTERN.matcher(message);
        StringBuilder hexBuffer = new StringBuilder();
        while (hexMatcher.find()) {
            String hex = hexMatcher.group(1);
            StringBuilder replacement = new StringBuilder("§x");
            for (char c : hex.toCharArray()) {
                replacement.append('§').append(c);
            }
            hexMatcher.appendReplacement(hexBuffer, Matcher.quoteReplacement(replacement.toString()));
        }
        hexMatcher.appendTail(hexBuffer);
        String messageFormatted = hexBuffer.toString();

        Matcher legacyMatcher = LEGACY_COLOR_PATTERN.matcher(messageFormatted);
        StringBuilder legacyBuffer = new StringBuilder();
        while (legacyMatcher.find()) {
            legacyMatcher.appendReplacement(legacyBuffer, "§" + legacyMatcher.group(1));
        }
        legacyMatcher.appendTail(legacyBuffer);

        return legacyBuffer.toString();
    }
}
