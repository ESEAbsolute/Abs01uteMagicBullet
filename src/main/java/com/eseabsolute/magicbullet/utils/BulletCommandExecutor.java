package com.eseabsolute.magicbullet.utils;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;
import java.util.logging.Level;

public class BulletCommandExecutor {
    
    private final Abs01uteMagicBulletPlugin plugin;
    
    public BulletCommandExecutor(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }

    public void executeOnFlyCommands(String bulletName, Location location, Player shooter, int tick) {
        if (location == null || bulletName == null) return;

        YamlConfiguration configuration = plugin.getBulletManager().getBullet(bulletName).getRawConfiguration();
        if (configuration == null) return;
        
        ConfigurationSection onFlySection = configuration.getConfigurationSection("on_fly");
        if (onFlySection == null) return;
        
        int interval = onFlySection.getInt("interval", 20);
        if (tick % interval != 0) return;
        
        List<String> commands = onFlySection.getStringList("commands");
        if (commands.isEmpty()) return;
        
        executeCommands(commands, location, null, shooter, bulletName);
    }

    public void executeOnLandCommands(String bulletName, Location location, Player shooter) {
        if (location == null || bulletName == null) return;

        YamlConfiguration configuration = plugin.getBulletManager().getBullet(bulletName).getRawConfiguration();
        if (configuration == null) return;
        
        ConfigurationSection onLandSection = configuration.getConfigurationSection("on_land");
        if (onLandSection == null) return;
        
        List<String> commands = onLandSection.getStringList("commands");
        if (commands.isEmpty()) return;
        
        executeCommands(commands, location, null, shooter, bulletName);
    }

    public void executeOnHitCommands(String bulletName, Location location, LivingEntity target, Player shooter) {
        if (location == null || bulletName == null || target == null) return;

        YamlConfiguration configuration = plugin.getBulletManager().getBullet(bulletName).getRawConfiguration();
        if (configuration == null) return;
        
        ConfigurationSection onHitSection = configuration.getConfigurationSection("on_hit");
        if (onHitSection == null) return;
        
        List<String> commands = onHitSection.getStringList("commands");
        if (commands.isEmpty()) return;
        
        executeCommands(commands, location, target, shooter, bulletName);
    }

    public void executeCommands(List<String> commands, Location location, LivingEntity target, Player shooter, String bulletName) {
        if (commands == null || commands.isEmpty() || location == null) return;
        
        if (!plugin.isEnabled()) {
            plugin.getMessageUtils().log(Level.WARNING, "log.warning.command.ignore.plugin.disabled");
            return;
        }
        
        boolean isMainThread = Bukkit.isPrimaryThread();
        if (isMainThread) {
            plugin.getMessageUtils().debugLog("log.debug.command.thread.main");
        } else {
            plugin.getMessageUtils().debugLog("log.debug.command.thread.async", Thread.currentThread().getName());
        }
        
        for (String cmd : commands) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
            
            try {
                final String processedCmd = replaceCommandVariables(cmd, location, target, shooter, bulletName);
                
                if (processedCmd == null || processedCmd.trim().isEmpty()) {
                    plugin.getMessageUtils().log(Level.WARNING, "log.warning.command.ignore.trim.empty");
                    continue;
                }

                String prefix = "server";
                String realCmd = processedCmd;
                int idx = processedCmd.indexOf(":");
                if (idx > 0) {
                    prefix = processedCmd.substring(0, idx).trim().toLowerCase();
                    realCmd = processedCmd.substring(idx + 1).trim();
                }
                switch (prefix) {
                    case "server":
                        dispatchAsConsole(realCmd);
                        break;
                    case "player":
                        if (shooter != null) {
                            dispatchAsPlayer(shooter, realCmd);
                        }
                        break;
                    case "op":
                        if (shooter != null) {
                            dispatchAsOp(shooter, realCmd);
                        }
                        break;
                    default:
                        dispatchAsConsole(processedCmd);
                }
            } catch (Exception e) {
                plugin.getMessageUtils().log(Level.WARNING, "log.warning.command.ignore.internal.error", cmd, e.getMessage());
                e.printStackTrace();
            }
        }
    }

    private void dispatchAsConsole(String cmd) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });
    }

    private void dispatchAsPlayer(Player player, String cmd) {
        Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            Bukkit.dispatchCommand(player, cmd);
        });
    }

    private void dispatchAsOp(Player player, String cmd) {
        Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            boolean wasOp = player.isOp();
            try {
                if (!wasOp) player.setOp(true);
                Bukkit.dispatchCommand(player, cmd);
            } finally {
                if (!wasOp) player.setOp(false);
            }
        });
    }

    private String replaceCommandVariables(String command, Location location, LivingEntity target, Player shooter, String bulletName) {
        if (command == null || location == null) return command;
        
        String result = command;
        
        try {
            plugin.getMessageUtils().debugLog("log.debug.command.raw", command);
            
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            
            result = result.replace("%x%", String.valueOf(x))
                          .replace("%y%", String.valueOf(y))
                          .replace("%z%", String.valueOf(z));
            
            result = result.replace("%exact_x%", String.format("%.2f", location.getX()))
                          .replace("%exact_y%", String.format("%.2f", location.getY()))
                          .replace("%exact_z%", String.format("%.2f", location.getZ()));
            
            if (location.getWorld() != null) {
                result = result.replace("%world%", location.getWorld().getName());
            }
            
            if (shooter != null) {
                result = result.replace("%shooter%", shooter.getName())
                              .replace("%shooter_uuid%", shooter.getUniqueId().toString())
                              .replace("%player_name%", shooter.getName());
            }
            
            if (target != null) {
                String targetName = target instanceof Player ? ((Player)target).getName() : target.getType().toString();
                result = result.replace("%target%", targetName)
                              .replace("%target_name%", targetName) // 添加对target_name变量的支持
                              .replace("%target_uuid%", target.getUniqueId().toString())
                              .replace("%target_x%", String.valueOf(target.getLocation().getBlockX()))
                              .replace("%target_y%", String.valueOf(target.getLocation().getBlockY()))
                              .replace("%target_z%", String.valueOf(target.getLocation().getBlockZ()))
                              .replace("%target_eye_y%", String.format("%.2f", target instanceof Player ? 
                                  target.getEyeLocation().getY() :
                                  target.getLocation().getY() + target.getHeight() * 0.75)); // 添加眼睛高度变量
            }
            
            result = result.replace("%bullet%", bulletName);

            plugin.getMessageUtils().debugLog("log.debug.command.processed", result);

            return result;
        } catch (Exception e) {
            plugin.getMessageUtils().log(Level.WARNING, "log.warning.command.placeholder.replace", e.getMessage());
            e.printStackTrace();
            return command;
        }
    }
} 