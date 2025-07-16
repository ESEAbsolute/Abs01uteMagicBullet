package com.eseabsolute.magicbullet.managers;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.models.BulletConfig;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.List;

/**
 * 子弹管理器
 * 负责加载和管理子弹配置
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class BulletManager {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final Map<String, BulletConfig> bullets = new HashMap<>();
    private YamlConfiguration bulletsConfig; // 新增成员变量
    
    public BulletManager(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }
    

    public void loadBullets() {
        bullets.clear();
        File bulletsFile = new File(plugin.getDataFolder(), "bullets.yml");
        if (!bulletsFile.exists()) {
            plugin.saveResource("bullets.yml", false);
        }
        bulletsConfig = YamlConfiguration.loadConfiguration(bulletsFile); // 保存对象
        ConfigurationSection bulletsSection = bulletsConfig.getConfigurationSection("bullets");
        
        if (bulletsSection == null) {
            plugin.getLogger().warning("未找到 bullets.yml 中的 bullets 配置部分！");
            return;
        }
        
        for (String bulletName : bulletsSection.getKeys(false)) {
            try {
                BulletConfig bullet = loadBullet(bulletName, bulletsSection.getConfigurationSection(bulletName));
                if (bullet != null) {
                    bullets.put(bulletName, bullet);
                    plugin.getLogger().info("成功加载子弹配置: " + bulletName);
                }
            } catch (Exception e) {
                plugin.getLogger().log(Level.SEVERE, "加载子弹配置失败: " + bulletName, e);
            }
        }
        
        plugin.getLogger().info("共加载了 " + bullets.size() + " 个子弹配置");
    }
    

    private BulletConfig loadBullet(String name, ConfigurationSection section) {
        if (section == null) {
            return null;
        }
        
        try {

            String modelType = section.getString("model_type", "item");
            String item = section.getString("item", "STONE");
            String block = section.getString("block", "STONE");
            Particle particle = null;
            
            try {
                particle = Particle.valueOf(section.getString("particle", "SMOKE").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的粒子类型: " + section.getString("particle") + "，使用默认粒子");
                particle = Particle.SMOKE;
            }
            
            double damage = section.getDouble("damage", 5.0);
            boolean ignoreArmor = section.getBoolean("ignore_armor", false);
            int penetration = section.getInt("penetration", 1);
            

            ConfigurationSection explosionSection = section.getConfigurationSection("explosion");
            BulletConfig.ExplosionConfig explosion = null;
            if (explosionSection != null) {
                boolean enabled = explosionSection.getBoolean("enabled", true);
                double radius = explosionSection.getDouble("radius", 3.0);
                double explosionDamage = explosionSection.getDouble("damage", 8.0);
                Particle explosionParticle = null;
                try {
                    explosionParticle = Particle.valueOf(explosionSection.getString("particle", "EXPLOSION").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的爆炸粒子类型: " + explosionSection.getString("particle") + "，使用默认粒子");
                    explosionParticle = Particle.EXPLOSION;
                }
                String explosionSound = explosionSection.getString("sound", "ENTITY_GENERIC_EXPLODE");
                float soundVolume = (float) explosionSection.getDouble("sound_volume", 2.0);
                float soundPitch = (float) explosionSection.getDouble("sound_pitch", 1.0);
                int particleCount = explosionSection.getInt("particle_count", 20);
                double particleSpread = explosionSection.getDouble("particle_spread", 0.5);
                explosion = new BulletConfig.ExplosionConfig(enabled, radius, explosionDamage, explosionParticle, 
                                                           explosionSound, soundVolume, soundPitch, 
                                                           particleCount, particleSpread);
            }
            

            ConfigurationSection physicsSection = section.getConfigurationSection("physics");
            BulletConfig.PhysicsConfig physics = null;
            if (physicsSection != null) {
                double gravity = physicsSection.getDouble("gravity", 0.1);
                double knockback = physicsSection.getDouble("knockback", 1.5);
                boolean bounce = physicsSection.getBoolean("bounce", true);
                physics = new BulletConfig.PhysicsConfig(gravity, knockback, bounce);
            }
            

            BulletConfig.ParticlePresetConfig particlePreset = null;
            ConfigurationSection presetSection = section.getConfigurationSection("particle_preset");
            if (presetSection != null) {
                String presetType = presetSection.getString("type", "spiral");
                Particle presetParticle;
                try {
                    presetParticle = Particle.valueOf(presetSection.getString("particle", "FLAME").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("无效的粒子预设类型: " + presetSection.getString("particle") + "，使用 FLAME");
                    presetParticle = Particle.FLAME;
                }
                double radius = presetSection.getDouble("radius", 0.4);
                int count = presetSection.getInt("count", 8);
                double rotateSpeed = presetSection.getDouble("rotate_speed", 0.25);
                double interval = presetSection.getDouble("interval", 0.2);
                int density = presetSection.getInt("density", 6);
                particlePreset = new BulletConfig.ParticlePresetConfig(
                    presetType, presetParticle, radius, count, rotateSpeed, interval, density
                );
            }
            

            BulletConfig.ShootSoundConfig shootSound = null;
            ConfigurationSection soundSection = section.getConfigurationSection("shoot_sound");
            if (soundSection != null) {
                boolean enabled = soundSection.getBoolean("enabled", true);
                String sound = soundSection.getString("sound", "ENTITY_ARROW_SHOOT");
                float volume = (float) soundSection.getDouble("volume", 1.0);
                float pitch = (float) soundSection.getDouble("pitch", 1.0);
                shootSound = new BulletConfig.ShootSoundConfig(enabled, sound, volume, pitch);
            } else {

                shootSound = new BulletConfig.ShootSoundConfig(true, "ENTITY_ARROW_SHOOT", 1.0f, 1.0f);
            }
            

            double cooldown = section.getDouble("cooldown", plugin.getConfig().getDouble("features.magic-bullet.cooldown", 1.0));
            double maxRange = section.getDouble("max-range", plugin.getConfig().getDouble("features.magic-bullet.max-range", 100.0));
            int bounceLimit = section.getInt("bounce_limit", 10);


            boolean headshotEnabled = section.getBoolean("headshot", false);
            double headshotMultiplier = section.getDouble("headshot_multiplier", 2.0);

            boolean rotate = section.getBoolean("rotate", false);


            ConfigurationSection onFlySection = section.getConfigurationSection("on_fly");
            if (onFlySection != null) {
                List<String> commands = onFlySection.getStringList("commands");
                if (commands != null && !commands.isEmpty()) {
                    plugin.getLogger().info("子弹 " + name + " 加载了 " + commands.size() + " 个飞行命令");
                }
            }
            

            ConfigurationSection onLandSection = section.getConfigurationSection("on_land");
            if (onLandSection != null) {
                List<String> commands = onLandSection.getStringList("commands");
                if (commands != null && !commands.isEmpty()) {
                    plugin.getLogger().info("子弹 " + name + " 加载了 " + commands.size() + " 个落地命令");
                }
            }
            

            ConfigurationSection onHitSection = section.getConfigurationSection("on_hit");
            if (onHitSection != null) {
                List<String> commands = onHitSection.getStringList("commands");
                if (commands != null && !commands.isEmpty()) {
                    plugin.getLogger().info("子弹 " + name + " 加载了 " + commands.size() + " 个命中命令");
                }
            }

            return new BulletConfig(name, modelType, item, block, particle, damage, ignoreArmor, penetration, explosion, physics, cooldown, maxRange, bounceLimit, particlePreset, shootSound, headshotEnabled, headshotMultiplier, rotate);
            
        } catch (Exception e) {
            plugin.getLogger().severe("子弹配置错误 " + name + ": " + e.getMessage());
            return null;
        }
    }
    

    public BulletConfig getBullet(String name) {
        return bullets.get(name);
    }
    

    public java.util.Set<String> getBulletNames() {
        return bullets.keySet();
    }
    

    public boolean hasBullet(String name) {
        return bullets.containsKey(name);
    }
    

    public void reloadBullets() {
        loadBullets();
    }


    public YamlConfiguration getBulletsConfig() {
        return bulletsConfig;
    }
} 