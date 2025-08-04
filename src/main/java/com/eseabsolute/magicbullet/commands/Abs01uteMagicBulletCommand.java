package com.eseabsolute.magicbullet.commands;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.entities.properties.BulletShape;
import com.eseabsolute.magicbullet.entities.properties.BulletType;
import com.eseabsolute.magicbullet.entities.properties.CoordinateType;
import com.eseabsolute.magicbullet.entities.MagicBullet;
import com.eseabsolute.magicbullet.entities.properties.BulletData;
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
        if (!sender.hasPermission("abs01utemagicbullet.user") || !sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendConfigMessage(sender, "messages.no-permission");
            return true;
        }

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

    // Command: amb shoot Player Bullet BulletType BulletShape LifeTicks LaunchPositionOffsetVectorType x1 y1 z1 VelocityVectorType x2 y2 z2
    private void handleShoot(CommandSender sender, String[] args) {
        // TODO rewrite i18n
        if (args.length != 14) {
            // TODO rewrite command help
            messageUtils.sendErrorMessage(sender, "Usage & Sample:");
            messageUtils.sendErrorMessage(sender, "/amb shoot Player Bullet BulletType BulletShape LifeTicks LaunchPositionOffsetVectorType x1 y1 z1 VelocityVectorType x2 y2 z2");
            messageUtils.sendErrorMessage(sender, "/amb shoot ESEAbsolute Example_bullet PROJECTILE NORMAL 2000 LOCAL 0 0 0 LOCAL 0 0 1");
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
        BulletData bullet = plugin.getBulletManager().getBullet(bulletNameRaw);

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

        // [4] BulletShape arg logic
        String bulletShapeRaw = args[4].toUpperCase();
        BulletShape bulletShape;
        try {
            bulletShape = BulletShape.valueOf(bulletShapeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "子弹样式 " + bulletShapeRaw + " 不存在！");
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

        // [6] Launch Coordinate Vector Type arg logic
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

        // [10] Velocity Vector Type arg logic
        String velocityCoordinateTypeRaw = args[10].toUpperCase();
        CoordinateType velocityCoordinateType;
        try {
            velocityCoordinateType = CoordinateType.valueOf(velocityCoordinateTypeRaw);
        } catch (IllegalArgumentException e) {
            // TODO i18n
            messageUtils.sendErrorMessage(sender, "坐标格式 " + velocityCoordinateTypeRaw + " 不存在！");
            return;
        }

        // [11] ~ [13] Velocity Vector arg logic
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
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "info", "list", "shoot"));
            if (sender.hasPermission("abs01utemagicbullet.admin")) {
                subCommands.add("reload");
            }
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        }

        if (args[0].equalsIgnoreCase("shoot")) {
            switch (args.length) {
                case 2: // [1] Player
                    for (Player player : plugin.getServer().getOnlinePlayers()) {
                        if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                            completions.add(player.getName());
                        }
                    }
                    break;
                case 3: // [2] Bullet
                    for (String bulletName : plugin.getBulletManager().getBulletNames()) {
                        if (bulletName.toLowerCase().startsWith(args[2].toLowerCase())) {
                            completions.add(bulletName);
                        }
                    }
                    break;
                case 4: // [3] BulletType
                    completions = BulletType.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[3].toUpperCase()))
                            .toList();
                    break;
                case 5: // [4] BulletShape
                    completions = BulletShape.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[4].toUpperCase()))
                            .toList();
                    break;
                case 6: // [5] LifeTicks
                    break;
                case 7: // [6] Launch Coordinate Vector Type
                    completions = CoordinateType.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[6].toUpperCase()))
                            .toList();
                    break;
                case 8: // [7] ~ [9] Launch Coordinate
                    break;
                case 9: // [7] ~ [9] Launch Coordinate
                    break;
                case 10: // [7] ~ [9] Launch Coordinate
                    break;
                case 11: // [10] Velocity Vector Type
                    completions = CoordinateType.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[10].toUpperCase()))
                            .toList();
                    break;
                case 12: // [11] ~ [13] Velocity Vector
                    break;
                case 13: // [11] ~ [13] Velocity Vector
                    break;
                case 14: // [11] ~ [13] Velocity Vector
                    break;
                default:
                    break;
            }
        }
        return completions;
    }
} 