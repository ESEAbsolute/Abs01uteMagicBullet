package com.eseabsolute.magicbullet.entities.properties;

import org.bukkit.Particle;
import org.bukkit.configuration.file.YamlConfiguration;

public class BulletData {
    
    private final String name;
    private final String modelType; // "item" 或 "block"
    private final String item; // 物品材料名称
    private final String block; // 方块材料名称
    private final Particle particle;
    private final double damage;
    private final boolean ignoreArmor;
    private final int penetration;
    private final ExplosionConfig explosion;
    private final PhysicsConfig physics;
    private final double cooldown;
    private final double maxRange;
    private final int bounceLimit;
    private final ParticlePresetConfig particlePreset;
    private final ShootSoundConfig shootSound; // 射击音效配置
    private final boolean headshotEnabled;
    private final double headshotMultiplier;
    private final boolean rotate;

    private final YamlConfiguration rawConfiguration;
    
    public BulletData(String name, String modelType, String item, String block,
                      Particle particle, double damage, boolean ignoreArmor,
                      int penetration, ExplosionConfig explosion, PhysicsConfig physics,
                      double cooldown, double maxRange, int bounceLimit,
                      ParticlePresetConfig particlePreset, ShootSoundConfig shootSound,
                      boolean headshotEnabled, double headshotMultiplier, boolean rotate,
                      YamlConfiguration rawConfiguration) {
        this.name = name;
        this.modelType = modelType;
        this.item = item;
        this.block = block;
        this.particle = particle;
        this.damage = damage;
        this.ignoreArmor = ignoreArmor;
        this.penetration = penetration;
        this.explosion = explosion;
        this.physics = physics;
        this.cooldown = cooldown;
        this.maxRange = maxRange;
        this.bounceLimit = bounceLimit;
        this.particlePreset = particlePreset;
        this.shootSound = shootSound;
        this.headshotEnabled = headshotEnabled;
        this.headshotMultiplier = headshotMultiplier;
        this.rotate = rotate;
        this.rawConfiguration = rawConfiguration;
    }
    
    // Getters
    public String getName() {
        return name;
    }
    
    public String getModel() {
        return modelType;
    }
    
    public String getItem() {
        return item;
    }
    
    public String getBlock() {
        return block;
    }
    
    public Particle getParticle() {
        return particle;
    }
    
    public double getDamage() {
        return damage;
    }
    
    public boolean isIgnoreArmor() {
        return ignoreArmor;
    }
    
    public int getPenetration() {
        return penetration;
    }
    
    public ExplosionConfig getExplosion() {
        return explosion;
    }
    
    public PhysicsConfig getPhysics() {
        return physics;
    }

    public YamlConfiguration getRawConfiguration() {
        return rawConfiguration;
    }

    public double getCooldown() { return cooldown; }
    public double getMaxRange() { return maxRange; }
    public int getBounceLimit() { return bounceLimit; }
    public ParticlePresetConfig getParticlePreset() {
        return particlePreset;
    }
    
    public ShootSoundConfig getShootSound() {
        return shootSound;
    }

    public boolean isHeadshotEnabled() { return headshotEnabled; }
    public double getHeadshotMultiplier() { return headshotMultiplier; }
    public boolean isRotate() { return rotate; }

    public static class ExplosionConfig {
        private final boolean enabled;
        private final double radius;
        private final double damage;
        private final Particle particle;
        private final String sound;
        private final float soundVolume; // 爆炸音效音量
        private final float soundPitch; // 爆炸音效音调
        private final int particleCount;
        private final double particleSpread;
        
        public ExplosionConfig(boolean enabled, double radius, double damage, Particle particle, 
                              String sound, float soundVolume, float soundPitch, 
                              int particleCount, double particleSpread) {
            this.enabled = enabled;
            this.radius = radius;
            this.damage = damage;
            this.particle = particle;
            this.sound = sound;
            this.soundVolume = soundVolume;
            this.soundPitch = soundPitch;
            this.particleCount = particleCount;
            this.particleSpread = particleSpread;
        }
        
        public boolean isEnabled() { return enabled; }
        public double getRadius() { return radius; }
        public double getDamage() { return damage; }
        public Particle getParticle() { return particle; }
        public String getSound() { return sound; }
        public float getSoundVolume() { return soundVolume; }
        public float getSoundPitch() { return soundPitch; }
        public int getParticleCount() { return particleCount; }
        public double getParticleSpread() { return particleSpread; }
    }

    public static class PhysicsConfig {
        private final double gravity;
        private final double knockback;
        private final boolean bounce;
        
        public PhysicsConfig(double gravity, double knockback, boolean bounce) {
            this.gravity = gravity;
            this.knockback = knockback;
            this.bounce = bounce;
        }
        
        public double getGravity() {
            return gravity;
        }
        
        public double getKnockback() {
            return knockback;
        }
        
        public boolean isBounce() {
            return bounce;
        }
    }

    public static class ParticlePresetConfig {
        private final String type; // spiral, trail
        private final Particle particle;
        // spiral
        private final double radius;
        private final int count;
        private final double rotateSpeed;
        private final int density;
        // trail
        private final double interval;
        public ParticlePresetConfig(String type, Particle particle, double radius, int count, double rotateSpeed, double interval, int density) {
            this.type = type;
            this.particle = particle;
            this.radius = radius;
            this.count = count;
            this.rotateSpeed = rotateSpeed;
            this.interval = interval;
            this.density = density;
        }
        public String getType() { return type; }
        public Particle getParticle() { return particle; }
        public double getRadius() { return radius; }
        public int getCount() { return count; }
        public double getRotateSpeed() { return rotateSpeed; }
        public double getInterval() { return interval; }
        public int getDensity() { return density; }
    }

    public static class ShootSoundConfig {
        private final boolean enabled;
        private final String sound;
        private final float volume;
        private final float pitch;
        
        public ShootSoundConfig(boolean enabled, String sound, float volume, float pitch) {
            this.enabled = enabled;
            this.sound = sound;
            this.volume = volume;
            this.pitch = pitch;
        }
        
        public boolean isEnabled() {
            return enabled;
        }
        
        public String getSound() {
            return sound;
        }
        
        public float getVolume() {
            return volume;
        }
        
        public float getPitch() {
            return pitch;
        }
    }
} 