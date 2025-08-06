package com.eseabsolute.magicbullet.messages;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.logging.Level;

public class MessageUtils {
    private final Abs01uteMagicBulletPlugin plugin;
    private final I18n i18n;
    
    public MessageUtils(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        i18n = new I18n(
                plugin,
                plugin.getConfigManager().getString("language.default"),
                plugin.getConfigManager().getString("language.fallback")
        );
    }

    public void debugLog(@NotNull String translationKey, String... args) {
        if (plugin.getConfigManager().getBoolean("settings.debug", false)) {
            log(Level.INFO, translationKey, args);
        }
    }

    public void debugMessage(CommandSender sender, @NotNull String translationKey, String... args) {
        if (plugin.getConfigManager().getBoolean("settings.debug", false)) {
            sendMessage(sender, MessageLevel.INFO, translationKey, args);
        }
    }

    public void log(@NotNull Level level, @NotNull String translationKey, String... args) {
        String messageTranslated = MessageLevel.fromJavaUtilLevel(level).getColor() + i18n.get(translationKey, args);
        plugin.getLogger().log(Level.ALL, Formatter.format(messageTranslated));
    }

    public void sendMessage(CommandSender sender, @NotNull MessageLevel level, @NotNull String translationKey, String... args) {
        String messageTranslated = level.getColor() + i18n.get(translationKey, args);
        if (sender != null) {
            sender.sendMessage(Formatter.format(messageTranslated));
        }
    }

    public void sendMessageWithoutI18n(CommandSender sender, @NotNull MessageLevel level, @NotNull String message) {
        String messageRaw = level.getColor() + message;
        if (sender != null) {
            sender.sendMessage(Formatter.format(messageRaw));
        }
    }
} 