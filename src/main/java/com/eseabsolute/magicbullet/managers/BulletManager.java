package com.eseabsolute.magicbullet.managers;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.entities.properties.BulletData;
import org.bukkit.Particle;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;

import java.util.*;
import java.util.logging.Level;

public class BulletManager {
    private final String[] sampleFiles = {
            "bullets/FullExampleBullet.yml",
            "bullets/examples/ExampleBullet.yml",
            "bullets/examples/FireBullet.yml",
            "bullets/examples/LightningBullet.yml",
            "bullets/examples/SpiralArrow.yml",
            "bullets/examples/TrialArrow.yml",
            "bullets/examples/WeatherControlBullet.yml"
    };

    private final Abs01uteMagicBulletPlugin plugin;
    private final Map<String, BulletData> bullets = new HashMap<>();
    
    public BulletManager(Abs01uteMagicBulletPlugin plugin) {
        this.plugin = plugin;
    }

    public void loadBullets() {
        bullets.clear();
        File bulletsDirectory = new File(plugin.getDataFolder(), "bullets");

        if (!bulletsDirectory.exists()) {
            for (String path : sampleFiles) {
                plugin.saveResource(path, false);
            }
        }

        List<File> bulletConfigurations = getAllYamlFiles(bulletsDirectory);

        for (File bulletConfiguration : bulletConfigurations) {
            YamlConfiguration configuration = YamlConfiguration.loadConfiguration(bulletConfiguration);
            try {
                String bulletName = bulletConfiguration.getName().replaceFirst("\\.yml$", "");
                BulletData bullet = loadBullet(bulletName, configuration);
                if (bullet != null) {
                    bullets.put(bulletName, bullet);
                    plugin.getMessageUtils().log(Level.INFO, "log.info.config.bullet.load.success", bulletName);
                }
            } catch (Exception e) {
                plugin.getMessageUtils().log(Level.SEVERE, "log.error.config.bullet.load.failed", bulletConfiguration.getName(), e.toString());
            }
        }
    }

    private List<File> getAllYamlFiles(File directory) {
        List<File> yamlFiles = new ArrayList<>();
        File[] files = directory.listFiles();

        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    yamlFiles.addAll(getAllYamlFiles(file));
                } else if (file.getName().toLowerCase().endsWith(".yml")) {
                    yamlFiles.add(file);
                }
            }
        }

        return yamlFiles;
    }
    

    private BulletData loadBullet(String name, YamlConfiguration configuration) {
        if (configuration == null) {
            return null;
        }
        
        try {
            String modelType = configuration.getString("model_type", "item");
            String item = configuration.getString("item", "STONE");
            String block = configuration.getString("block", "STONE");
            Particle particle;
            
            try {
                particle = Particle.valueOf(configuration.getString("particle", "SMOKE").toUpperCase());
            } catch (IllegalArgumentException e) {
                plugin.getMessageUtils().log(Level.WARNING, "log.warning.particle.type.default", configuration.getString("particle"));
                particle = Particle.SMOKE;
            }
            
            double damage = configuration.getDouble("damage", 5.0);
            boolean ignoreArmor = configuration.getBoolean("ignore_armor", false);
            int penetration = configuration.getInt("penetration", 1);
            

            ConfigurationSection explosionSection = configuration.getConfigurationSection("explosion");
            BulletData.ExplosionConfig explosion = null;
            if (explosionSection != null) {
                boolean enabled = explosionSection.getBoolean("enabled", true);
                double radius = explosionSection.getDouble("radius", 3.0);
                double explosionDamage = explosionSection.getDouble("damage", 8.0);
                Particle explosionParticle;
                try {
                    explosionParticle = Particle.valueOf(explosionSection.getString("particle", "EXPLOSION").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getMessageUtils().log(Level.WARNING, "log.warning.particle.type.explosion", explosionSection.getString("particle"));
                    explosionParticle = Particle.EXPLOSION;
                }
                String explosionSound = explosionSection.getString("sound", "ENTITY_GENERIC_EXPLODE");
                float soundVolume = (float) explosionSection.getDouble("sound_volume", 2.0);
                float soundPitch = (float) explosionSection.getDouble("sound_pitch", 1.0);
                int particleCount = explosionSection.getInt("particle_count", 20);
                double particleSpread = explosionSection.getDouble("particle_spread", 0.5);
                explosion = new BulletData.ExplosionConfig(enabled, radius, explosionDamage, explosionParticle,
                                                           explosionSound, soundVolume, soundPitch, 
                                                           particleCount, particleSpread);
            }
            

            ConfigurationSection physicsSection = configuration.getConfigurationSection("physics");
            BulletData.PhysicsConfig physics = null;
            if (physicsSection != null) {
                double gravity = physicsSection.getDouble("gravity", 0.1);
                double knockback = physicsSection.getDouble("knockback", 1.5);
                boolean bounce = physicsSection.getBoolean("bounce", true);
                physics = new BulletData.PhysicsConfig(gravity, knockback, bounce);
            }
            

            BulletData.ParticlePresetConfig particlePreset = null;
            ConfigurationSection presetSection = configuration.getConfigurationSection("particle_preset");
            if (presetSection != null) {
                String presetType = presetSection.getString("type", "spiral");
                Particle presetParticle;
                try {
                    presetParticle = Particle.valueOf(presetSection.getString("particle", "FLAME").toUpperCase());
                } catch (IllegalArgumentException e) {
                    plugin.getMessageUtils().log(Level.WARNING, "log.warning.particle.type.preset", presetSection.getString("particle"));
                    presetParticle = Particle.FLAME;
                }
                double radius = presetSection.getDouble("radius", 0.4);
                int count = presetSection.getInt("count", 8);
                double rotateSpeed = presetSection.getDouble("rotate_speed", 0.25);
                double interval = presetSection.getDouble("interval", 0.2);
                int density = presetSection.getInt("density", 6);
                particlePreset = new BulletData.ParticlePresetConfig(
                    presetType, presetParticle, radius, count, rotateSpeed, interval, density
                );
            }
            

            BulletData.ShootSoundConfig shootSound;
            ConfigurationSection soundSection = configuration.getConfigurationSection("shoot_sound");
            if (soundSection != null) {
                boolean enabled = soundSection.getBoolean("enabled", true);
                String sound = soundSection.getString("sound", "ENTITY_ARROW_SHOOT");
                float volume = (float) soundSection.getDouble("volume", 1.0);
                float pitch = (float) soundSection.getDouble("pitch", 1.0);
                shootSound = new BulletData.ShootSoundConfig(enabled, sound, volume, pitch);
            } else {
                shootSound = new BulletData.ShootSoundConfig(true, "ENTITY_ARROW_SHOOT", 1.0f, 1.0f);
            }
            

            double cooldown = configuration.getDouble("cooldown", plugin.getConfig().getDouble("features.magic-bullet.cooldown", 1.0));
            double maxRange = configuration.getDouble("max-range", plugin.getConfig().getDouble("features.magic-bullet.max-range", 100.0));
            int bounceLimit = configuration.getInt("bounce_limit", 10);


            boolean headshotEnabled = configuration.getBoolean("headshot_enabled", false);
            double headshotMultiplier = configuration.getDouble("headshot_multiplier", 2.0);

            boolean rotate = configuration.getBoolean("rotate", false);


            ConfigurationSection onFlySection = configuration.getConfigurationSection("on_fly");
            if (onFlySection != null) {
                List<String> commands = onFlySection.getStringList("commands");
                if (!commands.isEmpty()) {
                    plugin.getMessageUtils().log(Level.INFO, "log.info.config.bullet.commands.fly", name, ((Integer) commands.size()).toString());
                }
            }
            

            ConfigurationSection onLandSection = configuration.getConfigurationSection("on_land");
            if (onLandSection != null) {
                List<String> commands = onLandSection.getStringList("commands");
                if (!commands.isEmpty()) {
                    plugin.getMessageUtils().log(Level.INFO, "log.info.config.bullet.commands.land", name, ((Integer) commands.size()).toString());
                }
            }
            

            ConfigurationSection onHitSection = configuration.getConfigurationSection("on_hit");
            if (onHitSection != null) {
                List<String> commands = onHitSection.getStringList("commands");
                if (!commands.isEmpty()) {
                    plugin.getMessageUtils().log(Level.INFO, "log.info.config.bullet.commands.hit", name, ((Integer) commands.size()).toString());
                }
            }

            return new BulletData(name, modelType, item, block, particle, damage, ignoreArmor,
                    penetration, explosion, physics, cooldown, maxRange, bounceLimit, particlePreset,
                    shootSound, headshotEnabled, headshotMultiplier, rotate, configuration);
            
        } catch (Exception e) {
            plugin.getMessageUtils().log(Level.SEVERE, "log.error.config.bullet.load.error", name, e.getMessage());
            return null;
        }
    }

    public BulletData getBullet(String name) {
        return bullets.get(name);
    }

    public Set<String> getBulletNames() {
        return bullets.keySet();
    }
    

    public boolean hasBullet(String name) {
        return bullets.containsKey(name);
    }
    

    public void reloadBullets() {
        loadBullets();
    }
} 