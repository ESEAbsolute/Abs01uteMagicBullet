package com.eseabsolute.magicbullet.commands;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.entities.BulletShape;
import com.eseabsolute.magicbullet.entities.BulletType;
import com.eseabsolute.magicbullet.entities.CoordinateType;
import com.eseabsolute.magicbullet.entities.MagicBullet;
import com.eseabsolute.magicbullet.models.BulletConfig;
import com.eseabsolute.magicbullet.utils.BulletTaskUtil;
import com.eseabsolute.magicbullet.utils.MessageUtils;
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
import org.jetbrains.annotations.NotNull;

public class Abs01uteMagicBulletCommand implements CommandExecutor, TabCompleter {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final MessageUtils messageUtils;
    
    public Abs01uteMagicBulletCommand(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String[] args) {
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

    // TODO rewrite i18n
    private void handleInfo(CommandSender sender) {
        messageUtils.sendInfoMessage(sender, "&6=== Abs01uteMagicBullet 插件信息 ===");
        messageUtils.sendInfoMessage(sender, "&e版本: &f" + plugin.getDescription().getVersion());
        messageUtils.sendInfoMessage(sender, "&e作者: &f" + plugin.getDescription().getAuthors());
        messageUtils.sendInfoMessage(sender, "&e描述: &f" + plugin.getDescription().getDescription());
        messageUtils.sendInfoMessage(sender, "&eAPI版本: &f" + plugin.getDescription().getAPIVersion());
        messageUtils.sendInfoMessage(sender, "&e服务器版本: &f" + plugin.getServer().getVersion());
        messageUtils.sendInfoMessage(sender, "&6================================");
    }

    // TODO rewrite i18n
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

    // TODO rewrite i18n
    private void handleList(CommandSender sender) {
        messageUtils.sendInfoMessage(sender, "&6=== 可用子弹列表 ===");
        
        for (String bulletName : plugin.getBulletManager().getBulletNames()) {
            messageUtils.sendInfoMessage(sender, "&e- " + bulletName);
        }
        
        messageUtils.sendInfoMessage(sender, "&6==================");
    }

    // Command: amb [0]shoot [1]Player [2]Bullet [3]BulletType [4]BulletShape [5]LifeTicks [6]CoordType1 [7]x1 [8]y1 [9]z1 [10]CoordType2 [11]x2 [12]y2 [13]z2
    private void handleShoot(CommandSender sender, String[] args) {
//        no need to check CommandSender?
//        if (!(sender instanceof Player) && args.length < 3) {
//            messageUtils.sendConfigMessage(sender, "messages.player-only");
//            return;
//        }

        // TODO rewrite i18n
        if (args.length != 14) {
//            messageUtils.sendErrorMessage(sender, "Usage: /amb shoot <player> <bullet> <^x0> <^y0> <^z0> <^x1> <^y1> <^z1> <lifetime (ticks)> <velocity>");
//            messageUtils.sendInfoMessage(sender, "Velocity = 0 indicates a laser with length of 'lifetime' and speed of the distance between two local coordinates");
//            messageUtils.sendInfoMessage(sender, "Local coordinate format: ^x directed to the left, ^y directed upwards and ^z directed to the facing");


            // TODO rewrite command help
//            messageUtils.sendErrorMessage(sender, "Usage: /amb shoot <玩家名> <子弹名> <^x0> <^y0> <^z0> <^x1> <^y1> <^z1> <存活游戏刻> <>");
            messageUtils.sendInfoMessage(sender, "Velocity = 0 indicates a laser with length of 'lifetime' and speed of the distance between two local coordinates");
            messageUtils.sendInfoMessage(sender, "Local coordinate format: ^x directed to the left, ^y directed upwards and ^z directed to the facing");
            return;
        }

        Player shooter;

        // [1] Player arg logic
        String playerNameRaw = args[1];
        shooter = plugin.getServer().getPlayer(playerNameRaw);
        if (shooter == null) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "找不到玩家: " + playerNameRaw);
            return;
        }

        // [2] Bullet arg logic
        String bulletNameRaw = args[2];
        if (!plugin.getBulletManager().hasBullet(bulletNameRaw)) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "子弹 " + bulletNameRaw + " 不存在！");
            return;
        }
        BulletConfig bullet = plugin.getBulletManager().getBullet(bulletNameRaw);

        // [3] BulletType arg logic
        String bulletTypeRaw = args[3].toUpperCase();
        BulletType bulletType;
        try {
            bulletType = BulletType.valueOf(bulletTypeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "子弹类型 " + bulletTypeRaw + " 不存在！");
            return;
        }

        // [4] BulletType arg logic
        String bulletShapeRaw = args[4].toUpperCase();
        BulletShape bulletShape;
        try {
            bulletShape = BulletShape.valueOf(bulletShapeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "子弹样式 " + bulletTypeRaw + " 不存在！");
            return;
        }

        // [5] LifeTicks arg logic
        String lifeTicksRaw = args[5];
        int maxLifeTicks;
        try {
            maxLifeTicks = Integer.parseInt(lifeTicksRaw);
            if (maxLifeTicks < 20 || maxLifeTicks > 6000) {
                // TODO i18n
                messageUtils.sendErrorMessage(sender, "存活时间必须在 20~6000 ticks之间 (1~300秒)");
                return;
            }
        } catch (NumberFormatException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "无效的存活时间值！请输入整数。");
            return;
        }

        // [6] CoordType1 arg logic
        String launchCoordinateTypeRaw = args[6].toUpperCase();
        CoordinateType launchCoordinateType;
        try {
            launchCoordinateType = CoordinateType.valueOf(launchCoordinateTypeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "坐标格式 " + launchCoordinateTypeRaw + " 不存在！");
            return;
        }

        // [7] ~ [9] Launch Coordinate arg logic
        Vector launchOffsetVector;
        try {
            double x = Double.parseDouble(args[7]);
            double y = Double.parseDouble(args[8]);
            double z = Double.parseDouble(args[9]);
            launchOffsetVector = new Vector(x, y, z);
        } catch (Exception e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "起始点坐标解析失败: " + e.getMessage());
            return;
        }

        // [10] CoordType1 arg logic
        String velocityCoordinateTypeRaw = args[10].toUpperCase();
        CoordinateType velocityCoordinateType;
        try {
            velocityCoordinateType = CoordinateType.valueOf(velocityCoordinateTypeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "坐标格式 " + velocityCoordinateTypeRaw + " 不存在！");
            return;
        }

        // [11] ~ [13] Launch Coordinate arg logic
        Vector velocityVector;
        try {
            double x = Double.parseDouble(args[11]);
            double y = Double.parseDouble(args[12]);
            double z = Double.parseDouble(args[13]);
            velocityVector = new Vector(x, y, z);
        } catch (Exception e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "速度向量解析失败: " + e.getMessage());
            return;
        }

        Location playerLoc = shooter.getEyeLocation();
        List<Vector> bulletVelocities = bulletShape.generate(
                velocityCoordinateType.toRelativeOffset(playerLoc, velocityVector)
        );
        Location launchLoc = launchCoordinateType.calculateLocation(playerLoc, launchOffsetVector);

        for (Vector velocity : bulletVelocities) {
            MagicBullet magicBullet = new MagicBullet(
                    plugin,
                    bullet,
                    shooter,
                    launchLoc,
                    velocity,
                    bulletType,
                    maxLifeTicks
            );

            plugin.getServer().getGlobalRegionScheduler().runDelayed(plugin, (task) -> {
                BulletTaskUtil.runBulletTask(plugin, shooter, () -> {
                    if (!magicBullet.isDead()) {
                        magicBullet.update();
                    }
                });
            }, 1);

        }
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