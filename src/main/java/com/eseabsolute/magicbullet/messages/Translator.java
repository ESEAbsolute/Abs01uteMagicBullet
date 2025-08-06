package com.eseabsolute.magicbullet.messages;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Translator {
    private static final Pattern ARG_FORMAT = Pattern.compile("%(?:(\\d+)\\$)?s");
    public static String get(String raw, String... args) {
        StringBuilder sb = new StringBuilder();
        Matcher matcher = ARG_FORMAT.matcher(raw);

        int argIndex = 0;
        while (matcher.find()) {
            String indexGroup = matcher.group(1);
            int index;
            if (indexGroup != null) {
                index = Integer.parseInt(indexGroup) - 1;
            } else {
                index = argIndex++;
            }

            String replacement;
            if (index >= 0 && index < args.length) {
                replacement = args[index] == null ? "null" : args[index];
            } else {
                replacement = "%s";
            }

            matcher.appendReplacement(sb, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(sb);

        return sb.toString();
    }
}
