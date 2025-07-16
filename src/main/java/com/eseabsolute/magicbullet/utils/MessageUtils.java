package com.eseabsolute.magicbullet.utils;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * 消息工具类
 * 负责处理插件消息的发送和格式化
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class MessageUtils {
    
    private final Abs01uteMagicBulletPlugin plugin;
    
    public MessageUtils(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 格式化消息
     * 
     * @param message 原始消息
     * @return 格式化后的消息
     */
    public String formatMessage(String message) {
        if (message == null) return "";
        String prefix = plugin.getConfigManager().getString("settings.prefix", "&8[&bMagicBullet&8] &r");
        String rawPrefix = org.bukkit.ChatColor.stripColor(org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix));
        String rawMsg = org.bukkit.ChatColor.stripColor(org.bukkit.ChatColor.translateAlternateColorCodes('&', message));
        // 防止重复前缀（无论颜色码、空格、换行等）
        if (rawMsg.startsWith(rawPrefix)) {
            return org.bukkit.ChatColor.translateAlternateColorCodes('&', message);
        }
        return org.bukkit.ChatColor.translateAlternateColorCodes('&', prefix + message);
    }
    
    /**
     * 从配置文件获取消息并格式化
     * 
     * @param path 配置路径
     * @return 格式化后的消息
     */
    public String getMessage(String path) {
        String message = plugin.getConfigManager().getString(path, "消息未找到: " + path);
        return formatMessage(message);
    }
    
    /**
     * 从配置文件获取消息并格式化
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 格式化后的消息
     */
    public String getMessage(String path, String defaultValue) {
        String message = plugin.getConfigManager().getString(path, defaultValue);
        return formatMessage(message);
    }
    
    /**
     * 发送消息给指定发送者
     * 
     * @param sender 消息接收者
     * @param message 消息内容
     */
    public void sendMessage(CommandSender sender, String message) {
        if (sender != null && message != null && !message.isEmpty()) {
            sender.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * 发送消息给指定发送者（从配置文件）
     * 
     * @param sender 消息接收者
     * @param path 配置路径
     */
    public void sendConfigMessage(CommandSender sender, String path) {
        sendMessage(sender, getMessage(path));
    }
    
    /**
     * 发送消息给指定玩家
     * 
     * @param player 玩家
     * @param message 消息内容
     */
    public void sendMessage(Player player, String message) {
        if (player != null && player.isOnline() && message != null && !message.isEmpty()) {
            player.sendMessage(formatMessage(message));
        }
    }
    
    /**
     * 发送消息给指定玩家（从配置文件）
     * 
     * @param player 玩家
     * @param path 配置路径
     */
    public void sendConfigMessage(Player player, String path) {
        sendMessage(player, getMessage(path));
    }
    
    /**
     * 广播消息给所有在线玩家
     * 
     * @param message 消息内容
     */
    public void broadcastMessage(String message) {
        if (message != null && !message.isEmpty()) {
            String formattedMessage = formatMessage(message);
            Bukkit.broadcastMessage(formattedMessage);
        }
    }
    
    /**
     * 广播消息给所有在线玩家（从配置文件）
     * 
     * @param path 配置路径
     */
    public void broadcastConfigMessage(String path) {
        broadcastMessage(getMessage(path));
    }
    
    /**
     * 发送消息给控制台
     * 
     * @param message 消息内容
     */
    public void sendConsoleMessage(String message) {
        if (message != null && !message.isEmpty()) {
            Bukkit.getConsoleSender().sendMessage(formatMessage(message));
        }
    }
    
    /**
     * 发送消息给控制台（从配置文件）
     * 
     * @param path 配置路径
     */
    public void sendConsoleConfigMessage(String path) {
        sendConsoleMessage(getMessage(path));
    }
    
    /**
     * 发送错误消息给指定发送者
     * 
     * @param sender 消息接收者
     * @param message 错误消息
     */
    public void sendErrorMessage(CommandSender sender, String message) {
        sendMessage(sender, "&c" + message);
    }
    
    /**
     * 发送成功消息给指定发送者
     * 
     * @param sender 消息接收者
     * @param message 成功消息
     */
    public void sendSuccessMessage(CommandSender sender, String message) {
        sendMessage(sender, "&a" + message);
    }
    
    /**
     * 发送警告消息给指定发送者
     * 
     * @param sender 消息接收者
     * @param message 警告消息
     */
    public void sendWarningMessage(CommandSender sender, String message) {
        sendMessage(sender, "&e" + message);
    }
    
    /**
     * 发送信息消息给指定发送者
     * 
     * @param sender 消息接收者
     * @param message 信息消息
     */
    public void sendInfoMessage(CommandSender sender, String message) {
        sendMessage(sender, "&b" + message);
    }
} 