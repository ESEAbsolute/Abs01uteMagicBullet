package com.eseabsolute.magicbullet.listeners;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.entities.MagicBullet;
import com.eseabsolute.magicbullet.models.BindingConfig;
import com.eseabsolute.magicbullet.utils.MessageUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

/**
 * 物品使用监听器
 * 处理绑定物品的触发方式
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class ItemUseListener implements Listener {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final MessageUtils messageUtils;
    
    public ItemUseListener(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        
        // 检查物品是否为空
        if (item == null || item.getType() == Material.AIR) {
            return;
        }
        
        // 获取绑定配置
        BindingConfig config = plugin.getItemBindingManager().getBindingConfig(item);
        if (config == null) {
            return;
        }
        
        // 检查触发方式
        String trigger = config.getTrigger();
        Action action = event.getAction();
        boolean isSneaking = player.isSneaking();
        
        boolean shouldTrigger = false;
        
        switch (trigger) {
            case "right":
                shouldTrigger = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && !isSneaking;
                break;
            case "left":
                shouldTrigger = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && !isSneaking;
                break;
            case "shift_right":
                shouldTrigger = (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK) && isSneaking;
                break;
            case "shift_left":
                shouldTrigger = (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK) && isSneaking;
                break;
        }
        
        if (!shouldTrigger) {
            return;
        }
        
        // 取消事件（防止破坏方块等）
        event.setCancelled(true);
        
        // 发射子弹
        shootBullet(player, config);
    }
    
    /**
     * 发射子弹
     */
    private void shootBullet(Player player, BindingConfig config) {
        // 获取子弹配置
        if (!plugin.getBulletManager().hasBullet(config.getBulletName())) {
            messageUtils.sendErrorMessage(player, "绑定的子弹 " + config.getBulletName() + " 不存在！");
            return;
        }
        
        // 计算射击方向
        Vector direction = calculateDirection(player, config.getDirection());
        if (direction == null) {
            messageUtils.sendErrorMessage(player, "无效的射击方向！");
            return;
        }
        
        // 获取子弹配置
        var bulletConfig = plugin.getBulletManager().getBullet(config.getBulletName());
        
        // 让子弹出现在玩家准星正前方0.7格
        Location spawnLocation = player.getEyeLocation().clone();
        Vector forward = player.getLocation().getDirection().normalize();
        spawnLocation.add(forward.multiply(0.7));
        
        // 创建魔法子弹
        MagicBullet magicBullet = new MagicBullet(
                plugin,
                bulletConfig,
                player,
                spawnLocation,
                direction.multiply(config.getSpeed())
        );
        
        // 开始子弹更新任务
        startBulletTask(magicBullet, player);
    }
    
    /**
     * 计算射击方向
     */
    private Vector calculateDirection(Player player, String direction) {
        switch (direction.toLowerCase()) {
            case "forward":
                return player.getLocation().getDirection().normalize();
            case "backward":
                return player.getLocation().getDirection().multiply(-1).normalize();
            case "left":
                Vector dir = player.getLocation().getDirection();
                return new Vector(dir.getZ(), 0, -dir.getX()).normalize();
            case "right":
                Vector dir2 = player.getLocation().getDirection();
                return new Vector(-dir2.getZ(), 0, dir2.getX()).normalize();
            case "up":
                return new Vector(0, 1, 0);
            case "down":
                return new Vector(0, -1, 0);
            default:
                return null;
        }
    }
    
    /**
     * 开始子弹更新任务
     */
    private void startBulletTask(MagicBullet bullet, Player anchor) {
        com.eseabsolute.magicbullet.utils.BulletTaskUtil.runBulletTask(plugin, anchor, () -> {
            // 检查子弹是否已销毁
            if (bullet.isDead()) {
                // 子弹已销毁，在Folia下需要取消任务
                return;
            }
            
            try {
                // 更新子弹
                bullet.update();
                
                // 再次检查更新后子弹是否已销毁
                if (bullet.isDead()) {
                    // 子弹在更新过程中被销毁
                    return;
                }
            } catch (Exception e) {
                // 捕获并记录任何更新过程中的异常
                plugin.getLogger().warning("子弹更新过程中发生错误: " + e.getMessage());
                // 出现异常时销毁子弹
                bullet.destroy();
            }
        });
    }
} 