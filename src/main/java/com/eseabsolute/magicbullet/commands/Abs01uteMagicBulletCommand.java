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
            case "bind":
                handleBind(sender, args);
                break;
            case "unbind":
                handleUnbind(sender);
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
        messageUtils.sendInfoMessage(sender, "&e/amb bind <子弹> &7- 绑定子弹到手持物品");
        messageUtils.sendInfoMessage(sender, "&e/amb unbind &7- 解绑手持物品的子弹");
        messageUtils.sendInfoMessage(sender, "&e/amb shoot [玩家名] <子弹名> <起点偏移x,y,z> <终点偏移x,y,z> [存活时间] &7- 发射子弹");
        messageUtils.sendInfoMessage(sender, "&7  例如: &e/amb shoot Perfect_Bullet 0,1,0 30,0,0 &7- 从头顶1格处发射到前方30格");
        messageUtils.sendInfoMessage(sender, "&7  例如: &e/amb shoot FAFA Perfect_Bullet 0,1,0 30,0,0 &7- 让玩家FAFA发射子弹");
        if (sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendInfoMessage(sender, "&e/amb reload &7- 重载插件配置");
        }
        messageUtils.sendInfoMessage(sender, "&6================================");
    }
    
    /**
     * 处理绑定命令
     */
    private void handleBind(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            messageUtils.sendConfigMessage(sender, "messages.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        if (args.length < 2) {
            messageUtils.sendErrorMessage(player, "用法: /amb bind <子弹名称>");
            return;
        }
        
        String bulletName = args[1];
        
        // 开始交互式绑定会话
        if (plugin.getInteractiveBindingManager().startBindingSession(player, bulletName)) {
            messageUtils.sendInfoMessage(player, "&a开始交互式绑定！请按照提示输入参数。");
        }
    }
    
    /**
     * 处理解绑命令
     */
    private void handleUnbind(CommandSender sender) {
        if (!(sender instanceof Player)) {
            messageUtils.sendConfigMessage(sender, "messages.player-only");
            return;
        }
        
        Player player = (Player) sender;
        
        if (player.getInventory().getItemInMainHand().getType() == Material.AIR) {
            messageUtils.sendConfigMessage(player, "messages.no-item-in-hand");
            return;
        }
        
        if (plugin.getItemBindingManager().unbindBulletFromItem(player)) {
            messageUtils.sendConfigMessage(player, "messages.bullet-unbound");
        } else {
            messageUtils.sendErrorMessage(player, "解绑失败！");
        }
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
            messageUtils.sendErrorMessage(sender, "用法: /amb shoot [玩家名] <子弹名> <局部起点偏移^x,^y,^z> <局部终点偏移^x,^y,^z> [存活时间(ticks)] [速度]");
            messageUtils.sendInfoMessage(sender, "速度为0时将创建激光效果，存活时间控制子弹/激光持续时间");
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
        
        Location playerLoc = shooter.getLocation();
        Vector playerDirection = playerLoc.getDirection();
        Vector playerRight = new Vector(-playerDirection.getZ(), 0, playerDirection.getX()).normalize();
        Vector playerUp = playerDirection.clone().crossProduct(playerRight).normalize();
        
        Vector startWorldOffset = convertLocalToWorld(startOffsetVec, playerDirection, playerRight, playerUp);
        Vector endWorldOffset = convertLocalToWorld(endOffsetVec, playerDirection, playerRight, playerUp);
        Location spawnLocation = playerLoc.clone().add(startWorldOffset);
        
        Vector direction = endWorldOffset.clone().subtract(startWorldOffset).normalize();
        double speed = 2.0; // 默认速度
        boolean isInstantTravel = false;
        if (args.length > argIndex) {
            try {
                speed = Double.parseDouble(args[argIndex]);
                if (speed == 0) {
                    isInstantTravel = true;
                    double distance = endWorldOffset.clone().subtract(startWorldOffset).length();
                    speed = Math.max(distance, 20.0);
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
                direction.multiply(speed),
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
                                    "，持续时间: " + maxLifeTicks + " ticks");
        } else {
            messageUtils.sendInfoMessage(sender, "已发射子弹 " + bulletName + "，局部坐标从 ^" + 
                                    startOffsetVec.getX() + ",^" + startOffsetVec.getY() + ",^" + startOffsetVec.getZ() + 
                                    " 到 ^" + endOffsetVec.getX() + ",^" + endOffsetVec.getY() + ",^" + endOffsetVec.getZ() +
                                    "，速度: " + speed + "，持续时间: " + maxLifeTicks + " ticks");
        }
    }
    
    /**
     * 将局部坐标转换为世界坐标
     * @param local 局部坐标
     * @param forward 前方向量
     * @param right 右方向量
     * @param up 上方向量
     * @return 世界坐标
     */
    private Vector convertLocalToWorld(Vector local, Vector forward, Vector right, Vector up) {
        // 局部坐标系: ^x(右) ^y(上) ^z(前)
        // 将局部坐标转换为世界坐标
        double x = local.getX();
        double y = local.getY();
        double z = local.getZ();
        
        // 右向量 * x + 上向量 * y + 前向量 * z
        return right.clone().multiply(x)
                .add(up.clone().multiply(y))
                .add(forward.clone().multiply(z));
    }
    
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        if (args.length == 1) {
            // 补全子命令
            List<String> subCommands = new ArrayList<>(Arrays.asList("help", "info", "bind", "unbind", "list", "shoot"));
            if (sender.hasPermission("abs01utemagicbullet.admin")) {
                subCommands.add("reload");
            }
            for (String subCommand : subCommands) {
                if (subCommand.toLowerCase().startsWith(args[0].toLowerCase())) {
                    completions.add(subCommand);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("bind")) {
            // /amb bind <子弹名> 补全子弹名
            for (String bulletName : plugin.getBulletManager().getBulletNames()) {
                if (bulletName.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(bulletName);
                }
            }
        } else if (args.length == 2 && args[0].equalsIgnoreCase("shoot")) {
            // /amb shoot [玩家名] 补全玩家名或子弹名
            // 首先尝试补全玩家名
            for (Player player : plugin.getServer().getOnlinePlayers()) {
                if (player.getName().toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(player.getName());
                }
            }
            // 然后尝试补全子弹名
            for (String bulletName : plugin.getBulletManager().getBulletNames()) {
                if (bulletName.toLowerCase().startsWith(args[1].toLowerCase())) {
                    completions.add(bulletName);
                }
            }
        } else if (args.length == 3 && args[0].equalsIgnoreCase("shoot")) {
            // 可能是 /amb shoot <玩家名> <子弹名> 或 /amb shoot <子弹名> <起点偏移>
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
            completions.add("5.0");  // 高速
        } else if (args.length == 7 && args[0].equalsIgnoreCase("shoot")) {
            // 只可能是 /amb shoot <玩家名> <子弹名> <起点偏移> <终点偏移> <存活时间> <速度>
            // 补全速度示例
            completions.add("0");    // 激光模式
            completions.add("1.0");  // 慢速
            completions.add("2.0");  // 默认速度
            completions.add("5.0");  // 高速
        }
        return completions;
    }
} 