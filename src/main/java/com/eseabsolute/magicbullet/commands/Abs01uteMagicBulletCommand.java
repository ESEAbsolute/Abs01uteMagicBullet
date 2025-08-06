package com.eseabsolute.magicbullet.commands;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.entities.properties.BulletShape;
import com.eseabsolute.magicbullet.entities.properties.BulletType;
import com.eseabsolute.magicbullet.entities.properties.CoordinateType;
import com.eseabsolute.magicbullet.entities.MagicBullet;
import com.eseabsolute.magicbullet.entities.properties.BulletData;
import com.eseabsolute.magicbullet.messages.MessageLevel;
import com.eseabsolute.magicbullet.utils.BulletTaskUtil;
import com.eseabsolute.magicbullet.messages.MessageUtils;
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
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, String @NotNull [] args) {
        if (!sender.hasPermission("abs01utemagicbullet.user") || !sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.no-permission");
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
                messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.invalid");
                break;
        }
        
        return true;
    }

    private void handleReload(CommandSender sender) {
        if (!sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.no-permission");
            return;
        }
        
        if (plugin.reloadPlugin()) {
            messageUtils.sendMessage(sender, MessageLevel.SUCCESS, "message.command.reload.success");
        } else {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.reload.failed");
        }
    }

    private void handleInfo(CommandSender sender) {
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.header");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.author", plugin.getDescription().getAuthors().toString());
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.description", plugin.getDescription().getDescription());
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.version.plugin",  plugin.getDescription().getVersion());
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.version.api", plugin.getDescription().getAPIVersion());
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.version.server", plugin.getServer().getVersion());
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.info.footer");
    }

    private void showHelp(CommandSender sender) {
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.header");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.help");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.info");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.list");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.shoot.basic");
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.shoot.args");
        if (sender.hasPermission("abs01utemagicbullet.admin")) {
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.command.reload");
        }
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.plugin.help.footer");
    }

    private void handleList(CommandSender sender) {
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.list.header");
        for (String bulletName : plugin.getBulletManager().getBulletNames()) {
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.list.display", bulletName);
        }
        messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.list.footer");
    }

    private void handleShoot(CommandSender sender, String[] args) {
        if (args.length != 14) {
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.shoot.help.header");
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.shoot.help.usage");
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.shoot.help.example");
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.shoot.help.coordinate.relative");
            messageUtils.sendMessage(sender, MessageLevel.NONE, "message.command.shoot.help.coordinate.local");
            return;
        }

        Player shooter;

        // [1] Player arg logic
        String playerNameRaw = args[1];
        shooter = plugin.getServer().getPlayer(playerNameRaw);
        if (shooter == null) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.player", playerNameRaw);
            return;
        }

        // [2] Bullet arg logic
        String bulletNameRaw = args[2];
        if (!plugin.getBulletManager().hasBullet(bulletNameRaw)) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.bullet.name", bulletNameRaw);
            return;
        }
        BulletData bullet = plugin.getBulletManager().getBullet(bulletNameRaw);

        // [3] BulletType arg logic
        String bulletTypeRaw = args[3].toUpperCase();
        BulletType bulletType;
        try {
            bulletType = BulletType.valueOf(bulletTypeRaw);
        } catch (IllegalArgumentException e) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.bullet.type", bulletTypeRaw);
            return;
        }

        // [4] BulletShape arg logic
        String bulletShapeRaw = args[4].toUpperCase();
        BulletShape bulletShape;
        try {
            bulletShape = BulletShape.valueOf(bulletShapeRaw);
        } catch (IllegalArgumentException e) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.bullet.shape", bulletShapeRaw);
            return;
        }

        // [5] LifeTicks arg logic
        String lifeTicksRaw = args[5];
        int maxLifeTicks;
        try {
            maxLifeTicks = Integer.parseInt(lifeTicksRaw);
            if (maxLifeTicks < 20 || maxLifeTicks > 6000) {
                messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.lifetime.range");
                return;
            }
        } catch (NumberFormatException e) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.lifetime.format");
            return;
        }

        // [6] Launch Coordinate Vector Type arg logic
        String launchCoordinateTypeRaw = args[6].toUpperCase();
        CoordinateType launchCoordinateType;
        try {
            launchCoordinateType = CoordinateType.valueOf(launchCoordinateTypeRaw);
        } catch (IllegalArgumentException e) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.coordinate.launch.type", launchCoordinateTypeRaw);
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
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.coordinate.launch.number", e.getMessage());
            return;
        }

        // [10] Velocity Vector Type arg logic
        String velocityCoordinateTypeRaw = args[10].toUpperCase();
        CoordinateType velocityCoordinateType;
        try {
            velocityCoordinateType = CoordinateType.valueOf(velocityCoordinateTypeRaw);
        } catch (IllegalArgumentException e) {
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.coordinate.velocity.type", velocityCoordinateTypeRaw);
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
            messageUtils.sendMessage(sender, MessageLevel.ERROR, "message.command.shoot.error.coordinate.velocity.number", e.getMessage());
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
                    completions.add("<LifeTicks:20~6000>");
                    break;
                case 7: // [6] Launch Coordinate Vector Type
                    completions = CoordinateType.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[6].toUpperCase()))
                            .toList();
                    break;
                case 8: // [7] ~ [9] Launch Coordinate
                    completions.add( (args[6].equalsIgnoreCase("LOCAL")) ? "<ΔXlocal>" : "<ΔX>" );
                    break;
                case 9: // [7] ~ [9] Launch Coordinate
                    completions.add( (args[6].equalsIgnoreCase("LOCAL")) ? "<ΔYlocal>" : "<ΔY>" );
                    break;
                case 10: // [7] ~ [9] Launch Coordinate
                    completions.add( (args[6].equalsIgnoreCase("LOCAL")) ? "<ΔZlocal>" : "<ΔZ>" );
                    break;
                case 11: // [10] Velocity Vector Type
                    completions = CoordinateType.getAllTypes().stream()
                            .filter(name -> name.toUpperCase().startsWith(args[10].toUpperCase()))
                            .toList();
                    break;
                case 12: // [11] ~ [13] Velocity Vector
                    completions.add( (args[10].equalsIgnoreCase("LOCAL")) ? "<ΔXlocal>" : "<ΔX>" );
                    break;
                case 13: // [11] ~ [13] Velocity Vector
                    completions.add( (args[10].equalsIgnoreCase("LOCAL")) ? "<ΔYlocal>" : "<ΔY>" );
                    break;
                case 14: // [11] ~ [13] Velocity Vector
                    completions.add( (args[10].equalsIgnoreCase("LOCAL")) ? "<ΔZlocal>" : "<ΔZ>" );
                    break;
                default:
                    break;
            }
        }
        return completions;
    }
} 