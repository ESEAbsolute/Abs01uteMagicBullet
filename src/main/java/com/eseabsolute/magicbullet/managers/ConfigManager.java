package com.eseabsolute.magicbullet.managers;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.configuration.file.FileConfiguration;

/**
 * 配置管理器
 * 负责管理插件的配置文件
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class ConfigManager {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private FileConfiguration config;
    
    public ConfigManager(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 加载配置文件
     */
    public void loadConfig() {
        plugin.saveDefaultConfig();
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    /**
     * 重载配置文件
     */
    public void reloadConfig() {
        plugin.reloadConfig();
        config = plugin.getConfig();
    }
    
    /**
     * 保存配置文件
     */
    public void saveConfig() {
        plugin.saveConfig();
    }
    
    /**
     * 获取字符串配置
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    public String getString(String path, String defaultValue) {
        return config.getString(path, defaultValue);
    }
    
    /**
     * 获取字符串配置
     * 
     * @param path 配置路径
     * @return 配置值
     */
    public String getString(String path) {
        return config.getString(path);
    }
    
    /**
     * 获取整数配置
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    public int getInt(String path, int defaultValue) {
        return config.getInt(path, defaultValue);
    }
    
    /**
     * 获取整数配置
     * 
     * @param path 配置路径
     * @return 配置值
     */
    public int getInt(String path) {
        return config.getInt(path);
    }
    
    /**
     * 获取双精度浮点数配置
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    public double getDouble(String path, double defaultValue) {
        return config.getDouble(path, defaultValue);
    }
    
    /**
     * 获取双精度浮点数配置
     * 
     * @param path 配置路径
     * @return 配置值
     */
    public double getDouble(String path) {
        return config.getDouble(path);
    }
    
    /**
     * 获取布尔配置
     * 
     * @param path 配置路径
     * @param defaultValue 默认值
     * @return 配置值
     */
    public boolean getBoolean(String path, boolean defaultValue) {
        return config.getBoolean(path, defaultValue);
    }
    
    /**
     * 获取布尔配置
     * 
     * @param path 配置路径
     * @return 配置值
     */
    public boolean getBoolean(String path) {
        return config.getBoolean(path);
    }
    
    /**
     * 设置配置值
     * 
     * @param path 配置路径
     * @param value 配置值
     */
    public void set(String path, Object value) {
        config.set(path, value);
    }
    
    /**
     * 检查配置路径是否存在
     * 
     * @param path 配置路径
     * @return 是否存在
     */
    public boolean contains(String path) {
        return config.contains(path);
    }
    
    /**
     * 获取原始配置文件
     * 
     * @return 配置文件
     */
    public FileConfiguration getConfig() {
        return config;
    }
} 