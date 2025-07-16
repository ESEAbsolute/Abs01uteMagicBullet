package com.eseabsolute.magicbullet.listeners;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityChangeBlockEvent;
import org.bukkit.entity.FallingBlock;

/**
 * 玩家事件监听器
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class PlayerListener implements Listener {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final MessageUtils messageUtils;
    
    public PlayerListener(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
    }
    
    /**
     * 玩家加入服务器事件
     * 
     * @param event 玩家加入事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否启用调试模式
        if (plugin.getConfigManager().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 加入了服务器");
        }
        
        // 这里可以添加玩家加入时的自定义逻辑
        // 例如：发送欢迎消息、检查玩家数据等
        
        // 示例：发送欢迎消息
        // if (player.hasPermission("abs01utemagicbullet.user")) {
        //     messageUtils.sendInfoMessage(player, "&a欢迎来到服务器！");
        // }
    }
    
    /**
     * 玩家离开服务器事件
     * 
     * @param event 玩家离开事件
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // 检查是否启用调试模式
        if (plugin.getConfigManager().getBoolean("settings.debug", false)) {
            plugin.getLogger().info("玩家 " + player.getName() + " 离开了服务器");
        }
        
        // 这里可以添加玩家离开时的自定义逻辑
        // 例如：保存玩家数据、清理缓存等
    }

    @EventHandler
    public void onFallingBlockChange(EntityChangeBlockEvent event) {
        if (event.getEntity() instanceof FallingBlock) {
            FallingBlock fb = (FallingBlock) event.getEntity();
            if (fb.getCustomName() != null && fb.getCustomName().contains("MagicBullet")) {
                event.setCancelled(true);
                fb.remove();
            }
        }
    }
} 