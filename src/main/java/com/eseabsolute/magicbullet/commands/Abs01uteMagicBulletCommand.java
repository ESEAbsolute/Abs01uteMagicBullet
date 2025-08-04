package com.eseabsolute.magicbullet.commands;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.utils.MessageUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.bukkit.util.Vector;
import org.bukkit.Location;

/**
 * Abs01uteMagicBullet 插件主命令
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class Abs01uteMagicBulletCommand implements CommandExecutor, TabCompleter {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final MessageUtils messageUtils;
    
    public Abs01uteMagicBulletCommand(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            showHelp(sender);
            return true;
        }
        
        String subCommand = args[0].toLowerCase();
        
        switch (subCommand) {
            case "reload":
                handleReload(sender);
                break;
            case "info":
                handleInfo(sender);
                break;
            case "help":
                showHelp(sender);
                break;
            case "list":
                handleList(sender);
                break;
            case "shoot":
                handleShoot(sender, args);
                break;
            default:
                messageUtils.sendConfigMessage(sender, "messages.invalid-command");
                break;
        }
        
        return true;
    }
    
    /**
     * 处理重载命令
     * 
     * @param sender 命令发送者
     */
    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendConfigMessage(sender, "messages.no-permission");
            return;
        }
        
        if (plugin.reloadPlugin()) {
            messageUtils.sendConfigMessage(sender, "messages.reload-success");
        } else {
            messageUtils.sendConfigMessage(sender, "messages.reload-failed");
        }
    }
    
    /**
     * 处理信息命令
     * 
     * @param sender 命令发送者
     */
    private void handleInfo(CommandSender sender) {
        messageUtils.sendInfoMessage(sender, "&6=== Abs01uteMagicBullet 插件信息 ===");
        messageUtils.sendInfoMessage(sender, "&e版本: &f" + plugin.getDescription().getVersion());
        messageUtils.sendInfoMessage(sender, "&e作者: &f" + plugin.getDescription().getAuthors());
        messageUtils.sendInfoMessage(sender, "&e描述: &f" + plugin.getDescription().getDescription());
        messageUtils.sendInfoMessage(sender, "&eAPI版本: &f" + plugin.getDescription().getAPIVersion());
        messageUtils.sendInfoMessage(sender, "&e服务器版本: &f" + plugin.getServer().getVersion());
        messageUtils.sendInfoMessage(sender, "&6================================");
    }
    
    /**
     * 显示帮助信息
     * 
     * @param sender 命令发送者
     */
    private void showHelp(CommandSender sender) {
        messageUtils.sendInfoMessage(sender, "&6=== Abs01uteMagicBullet 命令帮助 ===");
        messageUtils.sendInfoMessage(sender, "&e/amb help &7- 显示此帮助信息");
        messageUtils.sendInfoMessage(sender, "&e/amb info &7- 显示插件信息");
        messageUtils.sendInfoMessage(sender, "&e/amb list &7- 显示可用子弹列表");
        messageUtils.sendInfoMessage(sender, "&e/amb shoot [玩家名] <子弹名> <局部起点偏移^x,^y,^z> <局部终点偏移^x,^y,^z> [存活时间(ticks)] [速度]");
        messageUtils.sendInfoMessage(sender, "&7  速度为0时将创建激光效果，存活时间即激光长度");
        if (sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendInfoMessage(sender, "&e/amb reload &7- 重载插件配置");
        }
        messageUtils.sendInfoMessage(sender, "&6================================");
    }
    
    /**
     * 处理列表命令
     */
    private void handleList(CommandSender sender) {
        messageUtils.sendInfoMessage(sender, "&6=== 可用子弹列表 ===");
        
        for (String bulletName : plugin.getBulletManager().getBulletNames()) {
            messageUtils.sendInfoMessage(sender, "&e- " + bulletName);
        }
        
        messageUtils.sendInfoMessage(sender, "&6==================");
    }
    
    /**
     * 处理射击命令
     */
    private void handleShoot(CommandSender sender, String[] args) {
        // 检查命令发送者
        if (!(sender instanceof Player) && args.length < 3) {
            messageUtils.sendConfigMessage(sender, "messages.player-only");
            return;
        }


        if (args.length < 3) {
//            messageUtils.sendErrorMessage(sender, "Usage: /amb shoot <player> <bullet> <^x0> <^y0> <^z0> <^x1> <^y1> <^z1> <lifetime (ticks)> <velocity>");
//            messageUtils.sendInfoMessage(sender, "Velocity = 0 indicates a laser with length of 'lifetime' and speed of the distance between two local coordinates");
//            messageUtils.sendInfoMessage(sender, "Local coordinate format: ^x directed to the left, ^y directed upwards and ^z directed to the facing");
            messageUtils.sendErrorMessage(sender, "Usage: /amb shoot <玩家名> <子弹名> <^x0> <^y0> <^z0> <^x1> <^y1> <^z1> <存活游戏刻> <>");
            messageUtils.sendInfoMessage(sender, "Velocity = 0 indicates a laser with length of 'lifetime' and speed of the distance between two local coordinates");
            messageUtils.sendInfoMessage(sender, "Local coordinate format: ^x directed to the left, ^y directed upwards and ^z directed to the facing");
            return;
        }

        Player shooter;
        String bulletName;
        String startOffset;
        String endOffset;
        int maxLifeTicks = 200; // 默认10秒(200ticks)

        int argIndex = 1;

        if (args.length >= 5) {
            String playerName = args[argIndex++];
            shooter = plugin.getServer().getPlayer(playerName);
            if (shooter == null) {
                messageUtils.sendErrorMessage(sender, "找不到玩家: " + playerName);
                return;
            }
        } else {
            if (!(sender instanceof Player)) {
                messageUtils.sendErrorMessage(sender, "控制台必须指定玩家名!");
                return;
            }
            shooter = (Player) sender;
        }

        bulletName = args[argIndex++];
        if (!plugin.getBulletManager().hasBullet(bulletName)) {
            messageUtils.sendErrorMessage(sender, "子弹 " + bulletName + " 不存在！");
            return;
        }

        startOffset = args[argIndex++];
        endOffset = args[argIndex++];
        if (args.length > argIndex) {
            try {
                maxLifeTicks = Integer.parseInt(args[argIndex++]);
                if (maxLifeTicks < 20 || maxLifeTicks > 6000) {
                    messageUtils.sendErrorMessage(sender, "存活时间必须在 20~6000 ticks之间 (1~300秒)");
                    return;
                }
            } catch (NumberFormatException e) {
                messageUtils.sendErrorMessage(sender, "无效的存活时间值！请输入整数。");
                return;
            }
        }

        Vector startOffsetVec;
        try {
            String[] startParts = startOffset.split(",");
            if (startParts.length != 3) {
                messageUtils.sendErrorMessage(sender, "局部起点偏移格式错误，应为: ^x,^y,^z");
                return;
            }
            double x = Double.parseDouble(startParts[0]);
            double y = Double.parseDouble(startParts[1]);
            double z = Double.parseDouble(startParts[2]);
            startOffsetVec = new Vector(x, y, z);
        } catch (Exception e) {
            messageUtils.sendErrorMessage(sender, "解析局部起点偏移失败: " + e.getMessage());
            return;
        }

        Vector endOffsetVec;
        try {
            String[] endParts = endOffset.split(",");
            if (endParts.length != 3) {
                messageUtils.sendErrorMessage(sender, "局部终点偏移格式错误，应为: ^x,^y,^z");
                return;
            }
            double x = Double.parseDouble(endParts[0]);
            double y = Double.parseDouble(endParts[1]);
            double z = Double.parseDouble(endParts[2]);
            endOffsetVec = new Vector(x, y, z);
        } catch (Exception e) {
            messageUtils.sendErrorMessage(sender, "解析局部终点偏移失败: " + e.getMessage());
            return;
        }

        var bulletConfig = plugin.getBulletManager().getBullet(bulletName);
        Location playerLoc = shooter.getEyeLocation();

        // 直接计算：起点位置 = 玩家位置 + 起点偏移（转换到世界坐标）
        Location spawnLocation = applyLocalOffset(playerLoc, startOffsetVec);

        // 计算局部方向向量（终点 - 起点）
        Vector localDirection = endOffsetVec.clone().subtract(startOffsetVec);

        // 将局部方向向量转换为世界方向向量
        Vector worldDirection = transformLocalDirectionToWorld(localDirection, playerLoc).normalize();

        double speed = 2.0; // 默认速度
        boolean isInstantTravel = false;
        if (args.length > argIndex) {
            try {
                speed = Double.parseDouble(args[argIndex]);
                if (speed == 0) {
                    isInstantTravel = true;
                    // 激光模式：速度设为局部距离，确保能到达终点
                    speed = Math.max(localDirection.length(), 20.0);
                } else if (speed < 0.1 || speed > 10.0) {
                    messageUtils.sendErrorMessage(sender, "速度必须是0(激光效果)或在 0.1~10.0 之间");
                    return;
                }
            } catch (NumberFormatException e) {
                messageUtils.sendErrorMessage(sender, "无效的速度值！请输入数字。");
                return;
            }
        }

        com.eseabsolute.magicbullet.entities.MagicBullet magicBullet = new com.eseabsolute.magicbullet.entities.MagicBullet(
                plugin,
                bulletConfig,
                shooter,
                spawnLocation,
                worldDirection.multiply(speed),
                maxLifeTicks
        );

        plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
            com.eseabsolute.magicbullet.utils.BulletTaskUtil.runBulletTask(plugin, shooter, () -> {
                if (!magicBullet.isDead()) {
                    magicBullet.update();
                }
            });
        }, 1);


        if (isInstantTravel) {
            messageUtils.sendInfoMessage(sender, "已发射激光 " + bulletName + "，局部坐标从 ^" +
                    startOffsetVec.getX() + ",^" + startOffsetVec.getY() + ",^" + startOffsetVec.getZ() +
                    " 到 ^" + endOffsetVec.getX() + ",^" + endOffsetVec.getY() + ",^" + endOffsetVec.getZ() +
                    "，探测距离: " + maxLifeTicks + " 格");
        } else {
            messageUtils.sendInfoMessage(sender, "已发射子弹 " + bulletName + "，局部坐标从 ^" +
                    startOffsetVec.getX() + ",^" + startOffsetVec.getY() + ",^" + startOffsetVec.getZ() +
                    " 到 ^" + endOffsetVec.getX() + ",^" + endOffsetVec.getY() + ",^" + endOffsetVec.getZ() +
                    "，速度: " + speed + "，持续时间: " + maxLifeTicks + " ticks");
        }
    }

    /**
     * 将局部偏移应用到玩家位置，返回世界坐标位置
     * 只在需要确定起点位置时使用一次
     */
    private Location applyLocalOffset(Location playerLoc, Vector localOffset) {
        // 获取玩家朝向
        float yaw = (float) Math.toRadians(playerLoc.getYaw());
        float pitch = (float) Math.toRadians(-playerLoc.getPitch()); // 注意pitch的符号

        // 计算基于玩家朝向的三个轴向量
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        // 前向量（Z轴正方向）
        double forwardX = -sinYaw * cosPitch;
        double forwardY = sinPitch;
        double forwardZ = cosYaw * cosPitch;

        // 右向量（X轴正方向）
        double rightX = cosYaw;
        double rightY = 0;
        double rightZ = sinYaw;

        // 上向量（Y轴正方向）
        double upX = sinYaw * sinPitch;
        double upY = cosPitch;
        double upZ = -cosYaw * sinPitch;

        // 应用局部偏移：世界偏移 = 右*x + 上*y + 前*z
        double worldOffsetX = rightX * localOffset.getX() + upX * localOffset.getY() + forwardX * localOffset.getZ();
        double worldOffsetY = rightY * localOffset.getX() + upY * localOffset.getY() + forwardY * localOffset.getZ();
        double worldOffsetZ = rightZ * localOffset.getX() + upZ * localOffset.getY() + forwardZ * localOffset.getZ();

        return playerLoc.clone().add(worldOffsetX, worldOffsetY, worldOffsetZ);
    }

    /**
     * 将局部方向向量转换为世界方向向量
     * 这里不需要位置，只需要方向的转换
     */
    private Vector transformLocalDirectionToWorld(Vector localDirection, Location playerLoc) {
        // 获取玩家朝向
        float yaw = (float) Math.toRadians(playerLoc.getYaw());
        float pitch = (float) Math.toRadians(-playerLoc.getPitch()); // 注意pitch的符号

        // 计算基于玩家朝向的三个轴向量
        double cosYaw = Math.cos(yaw);
        double sinYaw = Math.sin(yaw);
        double cosPitch = Math.cos(pitch);
        double sinPitch = Math.sin(pitch);

        // 前向量（Z轴正方向）
        double forwardX = -sinYaw * cosPitch;
        double forwardY = sinPitch;
        double forwardZ = cosYaw * cosPitch;

        // 右向量（X轴正方向）
        double rightX = cosYaw;
        double rightY = 0;
        double rightZ = sinYaw;

        // 上向量（Y轴正方向）
        double upX = sinYaw * sinPitch;
        double upY = cosPitch;
        double upZ = -cosYaw * sinPitch;

        // 转换局部方向向量：世界方向 = 右*x + 上*y + 前*z
        double worldDirX = rightX * localDirection.getX() + upX * localDirection.getY() + forwardX * localDirection.getZ();
        double worldDirY = rightY * localDirection.getX() + upY * localDirection.getY() + forwardY * localDirection.getZ();
        double worldDirZ = rightZ * localDirection.getX() + upZ * localDirection.getY() + forwardZ * localDirection.getZ();

        return new Vector(worldDirX, worldDirY, worldDirZ);
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // 补全子命令
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "info", "list", "shoot"));
            if (sender.hasPermission("abs01utemagicbullet.admin")) {
                subCommands.add("reload");
            }
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("shoot")) {
            // /amb shoot <玩家名>
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("shoot")) {
            // 可能是 /amb shoot <玩家名> <子弹名>
            // 检查args[1]是否是玩家名
            if (plugin.getServer().getPlayer(args[1]) != null) {
                // 如果是玩家名，补全子弹名
                for (String bulletName : plugin.getBulletManager().getBulletNames()) {
                    if (bulletName.toLowerCase().startsWith(args[2].toLowerCase())) {
                        completions.add(bulletName);
                    }
                }
            } else {
                // 如果不是玩家名，补全起点偏移示例 (局部坐标)
                completions.add("0,1,0");  // 正上方1格
                completions.add("0,0,1");  // 正前方1格
                completions.add("1,0,0");  // 右侧1格
                completions.add("-1,0,0"); // 左侧1格
            }
        } else if (args.length == 4 && args[0].equalsIgnoreCase("shoot")) {
            // 可能是 /amb shoot <玩家名> <子弹名> <起点偏移> 或 /amb shoot <子弹名> <起点偏移> <终点偏移>
            // 补全偏移示例 (局部坐标)
            completions.add("0,0,10");  // 正前方10格
            completions.add("0,5,10");  // 前方10格上方5格
            completions.add("5,0,10");  // 前方10格右侧5格
            completions.add("-5,0,10"); // 前方10格左侧5格
        } else if (args.length == 5 && args[0].equalsIgnoreCase("shoot")) {
            // 可能是 /amb shoot <玩家名> <子弹名> <起点偏移> <终点偏移> 或 /amb shoot <子弹名> <起点偏移> <终点偏移> <存活时间>
            // 补全存活时间示例
            completions.add("60");   // 3秒
            completions.add("100");  // 5秒
            completions.add("200");  // 10秒
            completions.add("400");  // 20秒
        } else if (args.length == 6 && args[0].equalsIgnoreCase("shoot")) {
            // 可能是 /amb shoot <玩家名> <子弹名> <起点偏移> <终点偏移> <存活时间> 或 /amb shoot <子弹名> <起点偏移> <终点偏移> <存活时间> <速度>
            // 补全速度示例
            completions.add("0");    // 激光模式
            completions.add("1.0");  // 慢速
            completions.add("2.0");  // 默认速度
            completions.add("4.0");  // 高速
        } else if (args.length == 7 && args[0].equalsIgnoreCase("shoot")) {
            // 只可能是 /amb shoot <玩家名> <子弹名> <起点偏移> <终点偏移> <存活时间> <速度>
            // 补全速度示例
            completions.add("0");    // 激光模式
            completions.add("1.0");  // 慢速
            completions.add("2.0");  // 默认速度
            completions.add("4.0");  // 高速
        }
        return completions;
    }
} 