package com.eseabsolute.magicbullet;

import com.eseabsolute.magicbullet.commands.Abs01uteMagicBulletCommand;
import com.eseabsolute.magicbullet.listeners.PlayerListener;
import com.eseabsolute.magicbullet.managers.BulletManager;
import com.eseabsolute.magicbullet.managers.ConfigManager;
import com.eseabsolute.magicbullet.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

/**
 * Abs01uteMagicBullet 插件主类
 * 适用于 Folia 1.21.4 服务器
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class Abs01uteMagicBulletPlugin extends JavaPlugin {
    
    private static Abs01uteMagicBulletPlugin instance;
    private ConfigManager configManager;
    private MessageUtils messageUtils;
    private BulletManager bulletManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        configManager = new ConfigManager(this);
        configManager.loadConfig();
        
        bulletManager = new BulletManager(this);
        bulletManager.loadBullets();
        
        messageUtils = new MessageUtils(this);
        
        registerCommands();
        
        registerListeners();
        
        if (configManager.getBoolean("settings.show-startup-message", true)) {
            getLogger().info("==========================================");
            getLogger().info("Abs01uteMagicBullet 插件已启动！");
            getLogger().info("版本: " + getDescription().getVersion());
            getLogger().info("作者: " + getDescription().getAuthors());
            getLogger().info("==========================================");
        }
        
        // 发送启动消息
        messageUtils.broadcastConfigMessage("messages.plugin-enabled");
    }
    
    @Override
    public void onDisable() {
        if (configManager.getBoolean("settings.show-startup-message", true)) {
            getLogger().info("==========================================");
            getLogger().info("Abs01uteMagicBullet 插件已关闭！");
            getLogger().info("==========================================");
        }
        
        messageUtils.broadcastConfigMessage("messages.plugin-disabled");
        
        instance = null;
    }
    

    private void registerCommands() {
        Abs01uteMagicBulletCommand abs01uteMagicBulletCommand = new Abs01uteMagicBulletCommand(this);
        getCommand("abs01utemagicbullet").setExecutor(abs01uteMagicBulletCommand);
        getCommand("abs01utemagicbullet").setTabCompleter(abs01uteMagicBulletCommand);
        getCommand("amb").setExecutor(abs01uteMagicBulletCommand);
        getCommand("amb").setTabCompleter(abs01uteMagicBulletCommand);
    }
    

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this);
    }
    
    /**
     * 获取插件实例
     * 
     * @return 插件实例
     */
    public static Abs01uteMagicBulletPlugin getInstance() {
        return instance;
    }
    
    /**
     * 获取配置管理器
     * 
     * @return 配置管理器
     */
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    /**
     * 获取消息工具
     * 
     * @return 消息工具
     */
    public MessageUtils getMessageUtils() {
        return messageUtils;
    }
    
    /**
     * 获取子弹管理器
     * 
     * @return 子弹管理器
     */
    public BulletManager getBulletManager() {
        return bulletManager;
    }
    
    /**
     * 重载插件配置
     * 
     * @return 是否重载成功
     */
    public boolean reloadPlugin() {
        try {
            configManager.reloadConfig();
            bulletManager.reloadBullets();
            messageUtils = new MessageUtils(this);
            return true;
        } catch (Exception e) {
            getLogger().severe("重载配置时发生错误: " + e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 