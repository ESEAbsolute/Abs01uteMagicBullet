package com.eseabsolute.magicbullet;

import com.eseabsolute.magicbullet.commands.Abs01uteMagicBulletCommand;
import com.eseabsolute.magicbullet.managers.BulletManager;
import com.eseabsolute.magicbullet.managers.ConfigManager;
import com.eseabsolute.magicbullet.messages.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Level;

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
            messageUtils.log(Level.INFO, "log.plugin.enable.header");
            messageUtils.log(Level.INFO, "log.plugin.enable.message");
            messageUtils.log(Level.INFO, "log.plugin.enable.version", getDescription().getVersion());
            messageUtils.log(Level.INFO, "log.plugin.enable.author", String.valueOf(getDescription().getAuthors()));
            messageUtils.log(Level.INFO, "log.plugin.enable.footer");
        }
    }
    
    @Override
    public void onDisable() {
        if (configManager.getBoolean("settings.show-startup-message", true)) {
            messageUtils.log(Level.INFO, "log.plugin.disable.header");
            messageUtils.log(Level.INFO, "log.plugin.disable.message");
            messageUtils.log(Level.INFO, "log.plugin.disable.footer");
        }
        
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
    }

    public static Abs01uteMagicBulletPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public BulletManager getBulletManager() {
        return bulletManager;
    }

    public boolean reloadPlugin() {
        try {
            configManager.reloadConfig();
            bulletManager.reloadBullets();
            messageUtils = new MessageUtils(this);
            return true;
        } catch (Exception e) {
            messageUtils.log(Level.SEVERE, "log.error.reload", e.getMessage());
            e.printStackTrace();
            return false;
        }
    }
} 