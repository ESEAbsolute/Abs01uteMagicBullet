package com.eseabsolute.magicbullet.managers;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.models.BindingConfig;
import com.eseabsolute.magicbullet.utils.MessageUtils;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 交互式绑定管理器
 * 处理分步骤的参数输入和绑定配置
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class InteractiveBindingManager {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final MessageUtils messageUtils;
    private final Map<UUID, BindingSession> activeSessions = new HashMap<>();
    
    public InteractiveBindingManager(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
        this.messageUtils = plugin.getMessageUtils();
        
        // 安全检查
        if (this.messageUtils == null) {
            plugin.getLogger().severe("MessageUtils 初始化失败！");
        }
    }
    
    /**
     * 开始绑定会话
     */
    public boolean startBindingSession(Player player, String bulletName) {
        // 安全检查
        if (messageUtils == null) {
            plugin.getLogger().severe("MessageUtils 未初始化，无法开始绑定会话");
            return false;
        }
        
        // 检查子弹是否存在
        if (!plugin.getBulletManager().hasBullet(bulletName)) {
            String message = plugin.getConfigManager().getString("messages.bullet-not-found", "子弹 {bullet} 不存在！")
                    .replace("{bullet}", bulletName);
            messageUtils.sendMessage(player, message);
            return false;
        }
        
        // 检查手持物品
        if (player.getInventory().getItemInMainHand().getType().isAir()) {
            messageUtils.sendConfigMessage(player, "messages.no-item-in-hand");
            return false;
        }
        
        // 创建绑定会话
        BindingSession session = new BindingSession(bulletName, player.getInventory().getItemInMainHand());
        activeSessions.put(player.getUniqueId(), session);
        
        // 发送欢迎消息
        messageUtils.sendInfoMessage(player, "&6&l⚡ 魔法子弹绑定系统");
        messageUtils.sendInfoMessage(player, "&e子弹: &f" + bulletName + " &7| &e物品: &f" + player.getInventory().getItemInMainHand().getType().name());
        messageUtils.sendInfoMessage(player, "&7输入 &ccancel &7或 &c取消 &7可以取消绑定。");
        
        // 开始第一步：射击方向
        promptForDirection(player);
        
        return true;
    }
    
    /**
     * 处理玩家输入
     */
    public boolean handlePlayerInput(Player player, String input) {
        BindingSession session = activeSessions.get(player.getUniqueId());
        if (session == null) {
            return false;
        }
        
        switch (session.getCurrentStep()) {
            case DIRECTION:
                return handleDirectionInput(player, input, session);
            case SPEED:
                return handleSpeedInput(player, input, session);
            case TRIGGER:
                return handleTriggerInput(player, input, session);
            default:
                return false;
        }
    }
    
    /**
     * 处理方向输入
     */
    private boolean handleDirectionInput(Player player, String input, BindingSession session) {
        String direction = input.toLowerCase();
        if (!isValidDirection(direction)) {
            messageUtils.sendErrorMessage(player, "&c无效方向！可用: &eforward&7, &ebackward&7, &eleft&7, &eright&7, &eup&7, &edown");
            return false;
        }
        
        session.setDirection(direction);
        session.setCurrentStep(BindingStep.SPEED);
        promptForSpeed(player);
        return true;
    }
    
    /**
     * 处理速度输入
     */
    private boolean handleSpeedInput(Player player, String input, BindingSession session) {
        try {
            double speed = Double.parseDouble(input);
            if (speed != 0 && speed < 0.1 || speed > 10.0) {
                messageUtils.sendErrorMessage(player, "&c速度范围: &e0 或 0.1 &7- &e10.0");
                return false;
            }
            
            session.setSpeed(speed > 0 ? speed : 20);
            session.setCurrentStep(BindingStep.TRIGGER);
            promptForTrigger(player);
            return true;
        } catch (NumberFormatException e) {
            messageUtils.sendErrorMessage(player, "&c请输入有效数字！");
            return false;
        }
    }
    
    /**
     * 处理触发方式输入
     */
    private boolean handleTriggerInput(Player player, String input, BindingSession session) {
        String trigger = input.toLowerCase();
        if (!isValidTrigger(trigger)) {
            messageUtils.sendErrorMessage(player, "&c无效触发方式！可用: &eright&7, &eleft&7, &eshift_right&7, &eshift_left");
            return false;
        }
        
        session.setTrigger(trigger);
        
        // 完成绑定
        completeBinding(player, session);
        return true;
    }
    
    /**
     * 完成绑定
     */
    private void completeBinding(Player player, BindingSession session) {
        // 创建绑定配置
        BindingConfig config = new BindingConfig(
                session.getBulletName(),
                session.getDirection(),
                session.getSpeed(),
                session.getTrigger()
        );
        
        // 保存绑定配置
        plugin.getItemBindingManager().saveBindingConfig(player, session.getItemStack(), config);
        
        // 更新物品显示名称
        updateItemDisplayName(player, session.getItemStack(), session.getBulletName());
        
        // 发送完成消息
        messageUtils.sendSuccessMessage(player, "&a&l✓ 绑定完成！");
        messageUtils.sendInfoMessage(player, "&e配置: &f" + session.getBulletName() + " &7| &e" + session.getDirection() + " &7| &e" + session.getSpeed() + " &7| &e" + session.getTrigger());
        messageUtils.sendInfoMessage(player, "&a手持物品并按触发方式即可发射！");
        
        // 清理会话
        activeSessions.remove(player.getUniqueId());
    }
    
    /**
     * 提示输入方向
     */
    private void promptForDirection(Player player) {
        messageUtils.sendInfoMessage(player, "&6&l➤ 请输入射击方向:");
        messageUtils.sendInfoMessage(player, "&e可用: &fforward&7, &fbackward&7, &fleft&7, &fright&7, &fup&7, &fdown");
    }
    
    /**
     * 提示输入速度
     */
    private void promptForSpeed(Player player) {
        messageUtils.sendInfoMessage(player, "&6&l➤ 请输入射击速度:");
        messageUtils.sendInfoMessage(player, "&e范围: &f0 或 0.1 &7- &f10.0");
    }
    
    /**
     * 提示输入触发方式
     */
    private void promptForTrigger(Player player) {
        messageUtils.sendInfoMessage(player, "&6&l➤ 请选择触发方式:");
        messageUtils.sendInfoMessage(player, "&e• &fright &7- 右键");
        messageUtils.sendInfoMessage(player, "&e• &fleft &7- 左键");
        messageUtils.sendInfoMessage(player, "&e• &fshift_right &7- Shift+右键");
        messageUtils.sendInfoMessage(player, "&e• &fshift_left &7- Shift+左键");
    }
    
    /**
     * 验证方向是否有效
     */
    private boolean isValidDirection(String direction) {
        return direction.matches("forward|backward|left|right|up|down");
    }
    
    /**
     * 验证触发方式是否有效
     */
    private boolean isValidTrigger(String trigger) {
        return trigger.matches("right|left|shift_right|shift_left");
    }
    
    /**
     * 更新物品显示名称
     */
    private void updateItemDisplayName(Player player, ItemStack item, String bulletName) {
        item.getItemMeta().setDisplayName("§b" + bulletName + " §7(魔法子弹)");
        player.getInventory().setItemInMainHand(item);
    }
    
    /**
     * 检查玩家是否在绑定会话中
     */
    public boolean isInBindingSession(Player player) {
        return activeSessions.containsKey(player.getUniqueId());
    }
    
    /**
     * 取消绑定会话
     */
    public void cancelBindingSession(Player player) {
        activeSessions.remove(player.getUniqueId());
        messageUtils.sendInfoMessage(player, "&c&l✗ 绑定会话已取消");
    }
    
    /**
     * 绑定步骤枚举
     */
    public enum BindingStep {
        DIRECTION, SPEED, TRIGGER
    }
    
    /**
     * 绑定会话类
     */
    private static class BindingSession {
        private final String bulletName;
        private final ItemStack itemStack;
        private BindingStep currentStep = BindingStep.DIRECTION;
        private String direction;
        private double speed;
        private String trigger;
        
        public BindingSession(String bulletName, ItemStack itemStack) {
            this.bulletName = bulletName;
            this.itemStack = itemStack;
        }
        
        // Getters and Setters
        public String getBulletName() { return bulletName; }
        public ItemStack getItemStack() { return itemStack; }
        public BindingStep getCurrentStep() { return currentStep; }
        public void setCurrentStep(BindingStep currentStep) { this.currentStep = currentStep; }
        public String getDirection() { return direction; }
        public void setDirection(String direction) { this.direction = direction; }
        public double getSpeed() { return speed; }
        public void setSpeed(double speed) { this.speed = speed; }
        public String getTrigger() { return trigger; }
        public void setTrigger(String trigger) { this.trigger = trigger; }
    }
} 