package com.eseabsolute.magicbullet.listeners;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

/**
 * 聊天监听器
 * 处理交互式绑定过程中的玩家输入
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class ChatListener implements Listener {
    
    private final Abs01uteMagicBulletPlugin plugin;
    
    public ChatListener(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        // 检查玩家是否在绑定会话中
        if (plugin.getInteractiveBindingManager().isInBindingSession(event.getPlayer())) {
            // 取消聊天事件，防止消息发送到聊天框
            event.setCancelled(true);
            
            // 处理绑定输入
            String input = event.getMessage().trim();
            
            // 检查是否要取消绑定
            if (input.equalsIgnoreCase("cancel") || input.equalsIgnoreCase("取消")) {
                plugin.getInteractiveBindingManager().cancelBindingSession(event.getPlayer());
                return;
            }
            
            // 处理绑定输入
            plugin.getInteractiveBindingManager().handlePlayerInput(event.getPlayer(), input);
        }
    }
} 