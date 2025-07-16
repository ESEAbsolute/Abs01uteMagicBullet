package com.eseabsolute.magicbullet.utils;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.configuration.ConfigurationSection;

import java.util.List;

/**
 * 子弹命令执行器
 * 处理子弹飞行、落地、命中时的命令执行
 * 
 * @author EseAbsolute
 * @version 1.0.1
 */
public class BulletCommandExecutor {
    
    private final Abs01uteMagicBulletPlugin plugin;
    
    public BulletCommandExecutor(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 执行飞行命令
     * @param bulletName 子弹名称
     * @param location 当前位置
     * @param shooter 射击者
     * @param tick 当前tick
     */
    public void executeOnFlyCommands(String bulletName, Location location, Player shooter, int tick) {
        if (location == null || bulletName == null) return;
        
        // 获取子弹飞行命令配置（从bullets.yml读取）
        ConfigurationSection bulletSection = plugin.getBulletManager().getBulletsConfig().getConfigurationSection("bullets." + bulletName);
        if (bulletSection == null) return;
        
        ConfigurationSection onFlySection = bulletSection.getConfigurationSection("on_fly");
        if (onFlySection == null) return;
        
        int interval = onFlySection.getInt("interval", 20);
        if (tick % interval != 0) return;
        
        List<String> commands = onFlySection.getStringList("commands");
        if (commands == null || commands.isEmpty()) return;
        
        executeCommands(commands, location, null, shooter, bulletName);
    }
    
    /**
     * 执行落地命令
     * @param bulletName 子弹名称
     * @param location 落地位置
     * @param shooter 射击者
     */
    public void executeOnLandCommands(String bulletName, Location location, Player shooter) {
        if (location == null || bulletName == null) return;
        
        // 获取子弹落地命令配置（从bullets.yml读取）
        ConfigurationSection bulletSection = plugin.getBulletManager().getBulletsConfig().getConfigurationSection("bullets." + bulletName);
        if (bulletSection == null) return;
        
        ConfigurationSection onLandSection = bulletSection.getConfigurationSection("on_land");
        if (onLandSection == null) return;
        
        List<String> commands = onLandSection.getStringList("commands");
        if (commands == null || commands.isEmpty()) return;
        
        executeCommands(commands, location, null, shooter, bulletName);
    }
    
    /**
     * 执行命中命令
     * @param bulletName 子弹名称
     * @param location 命中位置
     * @param target 命中目标
     * @param shooter 射击者
     */
    public void executeOnHitCommands(String bulletName, Location location, LivingEntity target, Player shooter) {
        if (location == null || bulletName == null || target == null) return;
        
        // 获取子弹命中命令配置（从bullets.yml读取）
        ConfigurationSection bulletSection = plugin.getBulletManager().getBulletsConfig().getConfigurationSection("bullets." + bulletName);
        if (bulletSection == null) return;
        
        ConfigurationSection onHitSection = bulletSection.getConfigurationSection("on_hit");
        if (onHitSection == null) return;
        
        List<String> commands = onHitSection.getStringList("commands");
        if (commands == null || commands.isEmpty()) return;
        
        executeCommands(commands, location, target, shooter, bulletName);
    }
    
    /**
     * 执行命令
     * @param commands 命令列表
     * @param location 位置
     * @param target 目标实体（可为null）
     * @param shooter 射击者
     * @param bulletName 子弹名称
     */
    public void executeCommands(List<String> commands, Location location, LivingEntity target, Player shooter, String bulletName) {
        if (commands == null || commands.isEmpty() || location == null) return;
        
        // 检查插件是否已经被禁用
        if (!plugin.isEnabled()) {
            plugin.getLogger().warning("插件已被禁用，无法执行命令");
            return;
        }
        
        // 记录当前线程信息
        boolean isMainThread = Bukkit.isPrimaryThread();
        plugin.getLogger().info("命令执行线程: " + (isMainThread ? "主线程" : "异步线程 " + Thread.currentThread().getName()));
        
        for (String cmd : commands) {
            if (cmd == null || cmd.trim().isEmpty()) {
                continue;
            }
            
            try {
                // 替换变量
                final String processedCmd = replaceCommandVariables(cmd, location, target, shooter, bulletName);
                
                if (processedCmd == null || processedCmd.trim().isEmpty()) {
                    plugin.getLogger().warning("处理后的命令为空");
                    continue;
                }

                // 新增：识别 circle_lightning 特殊命令
                if (processedCmd.startsWith("circle_lightning")) {
                    String[] args = processedCmd.split(" ");
                    double radius = args.length > 1 ? Double.parseDouble(args[1]) : 2.0;
                    int count = args.length > 2 ? Integer.parseInt(args[2]) : 8;
                    summonLightningCircle(location, radius, count);
                    continue;
                }

                // 新增：命令前缀支持 server: player: op:
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
                plugin.getLogger().warning("处理子弹命令时出错: " + e.getMessage());
                e.printStackTrace(); // 打印完整堆栈跟踪
            }
        }
    }
    
    /**
     * 在主线程上执行命令（Folia兼容：全局主线程）
     * @param command 要执行的命令
     */
    private void executeCommandOnMainThread(String command) {
        try {
            // Folia: 全局主线程调度
            Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
                try {
                    plugin.getLogger().info("Folia全局主线程执行命令: " + command);
                    boolean success = Bukkit.dispatchCommand(Bukkit.getConsoleSender(), command);
                    if (success) {
                        plugin.getLogger().info("命令执行成功: " + command);
                    } else {
                        plugin.getLogger().warning("命令执行失败: " + command);
                    }
                } catch (Exception e) {
                    plugin.getLogger().severe("命令执行异常: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Folia全局调度命令时发生严重错误: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // 控制台执行
    private void dispatchAsConsole(String cmd) {
        Bukkit.getGlobalRegionScheduler().execute(plugin, () -> {
            Bukkit.dispatchCommand(Bukkit.getConsoleSender(), cmd);
        });
    }

    // 玩家普通权限执行
    private void dispatchAsPlayer(Player player, String cmd) {
        Bukkit.getRegionScheduler().execute(plugin, player.getLocation(), () -> {
            Bukkit.dispatchCommand(player, cmd);
        });
    }

    // 玩家OP权限执行
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
    
    /**
     * 替换命令中的变量
     * @param command 原始命令
     * @param location 位置
     * @param target 目标实体（可为null）
     * @param shooter 射击者
     * @param bulletName 子弹名称
     * @return 替换变量后的命令
     */
    private String replaceCommandVariables(String command, Location location, LivingEntity target, Player shooter, String bulletName) {
        if (command == null || location == null) return command;
        
        String result = command;
        
        try {
            // 记录原始命令
            plugin.getLogger().info("原始命令: " + command);
            
            // 替换坐标变量
            int x = location.getBlockX();
            int y = location.getBlockY();
            int z = location.getBlockZ();
            
            // 修复命令中的坐标替换，确保正确格式
            result = result.replace("%x%", String.valueOf(x))
                          .replace("%y%", String.valueOf(y))
                          .replace("%z%", String.valueOf(z));
            
            // 替换精确坐标变量
            result = result.replace("%exact_x%", String.format("%.2f", location.getX()))
                          .replace("%exact_y%", String.format("%.2f", location.getY()))
                          .replace("%exact_z%", String.format("%.2f", location.getZ()));
            
            // 替换世界变量
            if (location.getWorld() != null) {
                result = result.replace("%world%", location.getWorld().getName());
            }
            
            // 替换发射者变量
            if (shooter != null) {
                result = result.replace("%shooter%", shooter.getName())
                              .replace("%shooter_uuid%", shooter.getUniqueId().toString())
                              .replace("%player_name%", shooter.getName());
            }
            
            // 替换目标变量
            if (target != null) {
                String targetName = target instanceof Player ? ((Player)target).getName() : target.getType().toString();
                result = result.replace("%target%", targetName)
                              .replace("%target_name%", targetName) // 添加对target_name变量的支持
                              .replace("%target_uuid%", target.getUniqueId().toString())
                              .replace("%target_x%", String.valueOf(target.getLocation().getBlockX()))
                              .replace("%target_y%", String.valueOf(target.getLocation().getBlockY()))
                              .replace("%target_z%", String.valueOf(target.getLocation().getBlockZ()))
                              .replace("%target_eye_y%", String.format("%.2f", target instanceof Player ? 
                                  ((Player)target).getEyeLocation().getY() : 
                                  target.getLocation().getY() + target.getHeight() * 0.75)); // 添加眼睛高度变量
            }
            
            // 替换子弹变量
            result = result.replace("%bullet%", bulletName);
            
            // 记录处理后的命令
            plugin.getLogger().info("处理后命令: " + result);
            
            return result;
        } catch (Exception e) {
            plugin.getLogger().warning("替换命令变量时出错: " + e.getMessage());
            e.printStackTrace();
            return command; // 出错时返回原始命令
        }
    }

    // 新增方法：在一圈内召唤雷电（Folia区块调度）
    private void summonLightningCircle(Location center, double radius, int count) {
        for (int i = 0; i < count; i++) {
            double angle = 2 * Math.PI * i / count;
            double x = center.getX() + radius * Math.cos(angle);
            double z = center.getZ() + radius * Math.sin(angle);
            double y = center.getY();
            Location loc = new Location(center.getWorld(), x, y, z);
            // 用 RegionScheduler 调度每个点的雷电
            Bukkit.getRegionScheduler().execute(plugin, loc, () -> {
                loc.getWorld().strikeLightning(loc);
            });
        }
    }
} 