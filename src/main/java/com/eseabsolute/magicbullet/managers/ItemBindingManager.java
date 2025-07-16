package com.eseabsolute.magicbullet.managers;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.models.BindingConfig;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * 物品绑定管理器
 * 管理物品与子弹的绑定关系
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class ItemBindingManager {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final Map<UUID, String> playerBindings = new HashMap<>(); // 玩家UUID -> 子弹名称
    private final Map<String, String> itemBindings = new HashMap<>(); // 物品标识 -> 子弹名称
    private final Map<String, BindingConfig> bindingConfigs = new HashMap<>(); // 物品标识 -> 绑定配置
    
    public ItemBindingManager(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    
    /**
     * 绑定子弹到玩家手持物品
     */
    public boolean bindBulletToItem(Player player, String bulletName) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() == Material.AIR) {
            return false;
        }
        
        // 检查子弹是否存在
        if (!plugin.getBulletManager().hasBullet(bulletName)) {
            return false;
        }
        
        // 生成物品标识
        String itemId = generateItemId(item);
        
        // 绑定子弹
        itemBindings.put(itemId, bulletName);
        
        // 更新物品显示名称
        updateItemDisplayName(item, bulletName);
        
        return true;
    }
    
    /**
     * 解绑玩家手持物品的子弹
     */
    public boolean unbindBulletFromItem(Player player) {
        ItemStack item = player.getInventory().getItemInMainHand();
        
        if (item.getType() == Material.AIR) {
            return false;
        }
        
        String itemId = generateItemId(item);
        String bulletName = itemBindings.remove(itemId);
        
        if (bulletName != null) {
            // 同时移除绑定配置
            bindingConfigs.remove(itemId);
            // 移除物品显示名称
            removeItemDisplayName(item);
            return true;
        }
        
        return false;
    }
    
    /**
     * 获取物品绑定的子弹名称
     */
    public String getBulletForItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        String itemId = generateItemId(item);
        return itemBindings.get(itemId);
    }
    
    /**
     * 检查物品是否绑定了子弹
     */
    public boolean hasBulletBinding(ItemStack item) {
        return getBulletForItem(item) != null;
    }
    
    /**
     * 生成物品唯一标识
     */
    private String generateItemId(ItemStack item) {
        // 使用物品类型和耐久度作为标识
        // 注意：这种方法在物品被修改时可能会失效
        return item.getType().name() + ":" + item.getDurability();
    }
    
    /**
     * 更新物品显示名称
     */
    private void updateItemDisplayName(ItemStack item, String bulletName) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§b" + bulletName + " §7(魔法子弹)");
            item.setItemMeta(meta);
        }
    }
    
    /**
     * 移除物品显示名称
     */
    private void removeItemDisplayName(ItemStack item) {
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(null);
            item.setItemMeta(meta);
        }
    }
    
    /**
     * 清理玩家绑定
     */
    public void clearPlayerBinding(Player player) {
        playerBindings.remove(player.getUniqueId());
    }
    
    /**
     * 保存绑定配置
     */
    public void saveBindingConfig(Player player, ItemStack item, BindingConfig config) {
        String itemId = generateItemId(item);
        itemBindings.put(itemId, config.getBulletName());
        bindingConfigs.put(itemId, config);
    }
    
    /**
     * 获取绑定配置
     */
    public BindingConfig getBindingConfig(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return null;
        }
        
        String itemId = generateItemId(item);
        return bindingConfigs.get(itemId);
    }
    
    /**
     * 获取所有绑定信息（用于调试）
     */
    public Map<String, String> getAllBindings() {
        return new HashMap<>(itemBindings);
    }
} 