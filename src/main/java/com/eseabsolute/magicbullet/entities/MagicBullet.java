package com.eseabsolute.magicbullet.entities;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import com.eseabsolute.magicbullet.models.BulletConfig;
import com.eseabsolute.magicbullet.utils.BulletCommandExecutor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.FallingBlock;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.bukkit.util.BlockIterator;
import org.bukkit.entity.ItemDisplay;
import org.bukkit.entity.Display.Billboard;
import org.bukkit.entity.Arrow;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.Bukkit;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * 魔法子弹实体
 * 实现子弹的物理行为和效果
 * 
 * @author EseAbsolute
 * @version 1.0.1
 */
public class MagicBullet {
    
    private final Abs01uteMagicBulletPlugin plugin;
    private final BulletConfig config;
    private final Player shooter;
    private final Vector velocity;
    private final Set<UUID> hitEntities = new HashSet<>();
    private int penetrationCount = 0;
    private boolean isDead = false;
    private int bounceCount = 0;
    
    // 子弹实体
    private ArmorStand bulletEntity;
    private FallingBlock fallingBlockEntity;
    private ItemDisplay itemDisplayEntity;
    private Arrow arrowEntity;
    private Location lastLocation;
    private int spiralTick = 0;
    private Location lastTrailLocation = null;
    
    // 命令执行计数器
    private int commandTick = 0;
    
    // 飞行命令tick计数器（每条命令独立）
    private java.util.List<Integer> flyCommandTicks = null;
    private java.util.List<Integer> flyCommandIntervals = null;
    private java.util.List<String> flyCommandCmds = null;
    private java.util.List<Integer> flyCommandMax = null;
    private java.util.List<Integer> flyCommandFired = null;
    
    // 命令执行器
    private final BulletCommandExecutor commandExecutor;
    
    // 子弹存活时间控制
    private int lifeTicks = 0;
    private final int maxLifeTicks;
    
    public MagicBullet(Abs01uteMagicBulletPlugin plugin, BulletConfig config, Player shooter, 
                      Location location, Vector velocity) {
        this(plugin, config, shooter, location, velocity, 200); // 默认存活10秒(200ticks)
    }
    
    public MagicBullet(Abs01uteMagicBulletPlugin plugin, BulletConfig config, Player shooter, 
                      Location location, Vector velocity, int maxLifeTicks) {
        this.plugin = plugin;
        this.config = config;
        this.shooter = shooter;
        this.velocity = velocity.clone();
        this.commandExecutor = new BulletCommandExecutor(plugin);
        this.maxLifeTicks = maxLifeTicks;
        
        if (velocity.length() > 20) {
            plugin.getLogger().info("高速子弹已激活，速度: " + velocity.length());
        }
        
        createBulletEntity(location);
        playShootSound(location);


        flyCommandTicks = new java.util.ArrayList<>();
        flyCommandIntervals = new java.util.ArrayList<>();
        flyCommandCmds = new java.util.ArrayList<>();
        flyCommandMax = new java.util.ArrayList<>();
        flyCommandFired = new java.util.ArrayList<>();
        ConfigurationSection bulletSection = plugin.getBulletManager().getBulletsConfig().getConfigurationSection("bullets." + config.getName());
        if (bulletSection != null) {

            ConfigurationSection onShootSection = bulletSection.getConfigurationSection("on_shoot");
            if (onShootSection != null && onShootSection.isList("commands")) {
                java.util.List<?> shootCmds = onShootSection.getList("commands");
                java.util.List<String> shootCmdList = new java.util.ArrayList<>();
                for (Object obj : shootCmds) {
                    if (obj instanceof String) shootCmdList.add((String)obj);
                    else if (obj instanceof java.util.Map) {
                        Object c = ((java.util.Map<?,?>)obj).get("cmd");
                        if (c != null) shootCmdList.add(c.toString());
                    }
                }
                if (!shootCmdList.isEmpty()) {
                    commandExecutor.executeCommands(shootCmdList, location, null, shooter, config.getName());
                }
            }

            ConfigurationSection onFlySection = bulletSection.getConfigurationSection("on_fly");
            if (onFlySection != null && onFlySection.isList("commands")) {
                java.util.List<?> list = onFlySection.getList("commands");
                for (Object obj : list) {
                    if (obj instanceof String) {
                        flyCommandCmds.add((String)obj);
                        flyCommandIntervals.add(onFlySection.getInt("interval", 20));
                        flyCommandTicks.add(0);
                        flyCommandMax.add(-1);
                        flyCommandFired.add(0);
                    } else if (obj instanceof java.util.Map) {
                        java.util.Map<?,?> map = (java.util.Map<?,?>)obj;
                        String cmd = map.get("cmd") != null ? map.get("cmd").toString() : "";
                        int interval = 20;
                        Object intervalObj = map.get("interval");
                        if (intervalObj != null) {
                            try { interval = Integer.parseInt(intervalObj.toString()); } catch(Exception ignore){}
                        }
                        int max = -1;
                        Object maxObj = map.get("max");
                        if (maxObj != null) {
                            try { max = Integer.parseInt(maxObj.toString()); } catch(Exception ignore){}
                        }
                        flyCommandCmds.add(cmd);
                        flyCommandIntervals.add(interval);
                        flyCommandTicks.add(0);
                        flyCommandMax.add(max);
                        flyCommandFired.add(0);
                    }
                }
            }
        }
    }
    

    private void createBulletEntity(Location location) {
        String modelType = config.getModel();
        
        if ("block".equalsIgnoreCase(modelType)) {
            // 创建方块子弹
            createBlockBullet(location);
        } else if ("arrow".equalsIgnoreCase(modelType)) {
            // 创建箭头子弹
            createArrowBullet(location);
        } else {
            // 创建物品子弹
            createItemBullet(location);
        }
    }
    

    private void createBlockBullet(Location location) {
        Material blockMaterial = Material.valueOf(config.getBlock().toUpperCase());
        // 使用Folia的RegionScheduler在正确的线程上执行实体操作
        plugin.getServer().getRegionScheduler().execute(plugin, location, () -> {
            try {
                fallingBlockEntity = location.getWorld().spawnFallingBlock(location, blockMaterial.createBlockData());
                fallingBlockEntity.setDropItem(false); // 严格禁止生成方块
                fallingBlockEntity.setGravity(false);
                fallingBlockEntity.setInvulnerable(true);
                fallingBlockEntity.setSilent(true);
                // 确保FallingBlock实体不会持久化，防止离线后加载导致生成方块
                fallingBlockEntity.setPersistent(false);
                // 设置TicksLived较大的值，确保实体在未正确移除时也会被自动移除
                fallingBlockEntity.setTicksLived(fallingBlockEntity.getTicksLived() + 1);
                // fallingBlockEntity.setCustomName("MagicBullet");
                // fallingBlockEntity.setCustomNameVisible(false);
            } catch (Exception e) {
                plugin.getLogger().severe("在区域线程上创建方块子弹失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    

    private void createItemBullet(Location location) {
        try {
            final ItemStack itemStack = new ItemStack(Material.valueOf(config.getItem().toUpperCase()));
            // 使用Folia的RegionScheduler在正确的线程上执行实体操作
            plugin.getServer().getRegionScheduler().execute(plugin, location, () -> {
                try {
                    itemDisplayEntity = location.getWorld().spawn(location, ItemDisplay.class, display -> {
                        // 在RegionScheduler线程中设置物品
                        plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                            display.setItemStack(itemStack);
                            display.setBillboard(Billboard.FIXED);
                            display.setViewRange(128f);
                            display.setBrightness(null);
                            display.setRotation(0, 0);
                            display.setInterpolationDuration(0);
                            display.setInterpolationDelay(0);
                            display.setTeleportDuration(0);
                            display.setInvulnerable(true);
                            // display.setCustomName("§b" + config.getName() + " §7(魔法子弹)");
                            // display.setCustomNameVisible(true);
                        });
                    });
                } catch (Exception e) {
                    plugin.getLogger().severe("在区域线程上创建物品子弹失败: " + e.getMessage());
                    e.printStackTrace();
                }
            });
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("无效的物品材料: " + config.getItem());
            final ItemStack defaultItem = new ItemStack(Material.STONE);
            // 使用Folia的RegionScheduler在正确的线程上执行实体操作
            plugin.getServer().getRegionScheduler().execute(plugin, location, () -> {
                try {
                    itemDisplayEntity = location.getWorld().spawn(location, ItemDisplay.class, display -> {
                        // 在RegionScheduler线程中设置物品
                        plugin.getServer().getRegionScheduler().run(plugin, location, task -> {
                            display.setItemStack(defaultItem);
                            display.setBillboard(Billboard.FIXED);
                            display.setViewRange(128f);
                            display.setBrightness(null);
                            display.setRotation(0, 0);
                            display.setInterpolationDuration(0);
                            display.setInterpolationDelay(0);
                            display.setTeleportDuration(0);
                            display.setInvulnerable(true);
                            // display.setCustomName("§b" + config.getName() + " §7(魔法子弹)");
                            // display.setCustomNameVisible(true);
                        });
                    });
                } catch (Exception ex) {
                    plugin.getLogger().severe("在区域线程上创建默认物品子弹失败: " + ex.getMessage());
                    ex.printStackTrace();
                }
            });
        }
    }
    

    private void createArrowBullet(Location location) {
        // 使用Folia的RegionScheduler在正确的线程上执行实体操作
        plugin.getServer().getRegionScheduler().execute(plugin, location, () -> {
            try {
                arrowEntity = location.getWorld().spawnArrow(location, velocity.clone().normalize(), (float) velocity.length(), 0f);
                arrowEntity.setShooter(shooter);
                arrowEntity.setPickupStatus(Arrow.PickupStatus.DISALLOWED);
                arrowEntity.setCritical(true);
                arrowEntity.setGravity(false);
                arrowEntity.setSilent(true); // 确保箭矢本身不会发出声音
                arrowEntity.setInvulnerable(true);
                

                arrowEntity.setPersistent(false);
            } catch (Exception e) {
                plugin.getLogger().severe("在区域线程上创建箭头子弹失败: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }
    

    public void update() {
        if (isDead) return;
        

        lifeTicks++;
        if (lifeTicks >= maxLifeTicks) {
            destroy();
            return;
        }

        Location currentLocation = getCurrentLocation();
        if (currentLocation == null) {
            destroy();
            return;
        }
        if (lastLocation == null) lastLocation = currentLocation.clone();
        

        if (velocity.length() > 20 && lifeTicks == 1) {
            Vector direction = velocity.clone().normalize();
            
            double maxDistance = velocity.length();
            
            Location from = currentLocation.clone();
            Location to = from.clone().add(direction.clone().multiply(maxDistance));
            
            Block collidedBlock = getCollidedBlock(from, to);
            Location hitLocation = null;
            
            if (collidedBlock != null) {
                hitLocation = collidedBlock.getLocation().add(0.5, 0.5, 0.5);
                Vector toBlockCenter = hitLocation.toVector();
                Vector fromVec = from.toVector();
                Vector dirToBlock = toBlockCenter.clone().subtract(fromVec).normalize();
                to = from.clone().add(dirToBlock.multiply(fromVec.distance(toBlockCenter) - 0.5));
            }
            renderLaserBeam(from, to);
            

            for (Entity entity : from.getWorld().getNearbyEntities(from, maxDistance, maxDistance, maxDistance)) {
                if (entity instanceof LivingEntity && entity != shooter &&
                    entity != bulletEntity && entity != fallingBlockEntity && entity != arrowEntity &&
                    !hitEntities.contains(entity.getUniqueId())) {
                    
                    Vector entityVec = entity.getLocation().toVector();
                    Vector fromVec = from.toVector();
                    Vector toVec = to.toVector();
                    if (isEntityInPath(fromVec, toVec, entityVec, 1.5)) {
                        // 处理实体碰撞
                        handleEntityCollision((LivingEntity) entity);
                    }
                }
            }
            

            if (collidedBlock != null) {
                handleBlockCollision(collidedBlock);
            }
            

            executeOnFlyCommands(to);
            

            destroy();
            return;
        }


        if (config.getPhysics() != null && config.getPhysics().getGravity() > 0) {
            velocity.add(new Vector(0, -config.getPhysics().getGravity(), 0));
        }


        if (fallingBlockEntity != null && config.isRotate()) {
            float yaw = fallingBlockEntity.getLocation().getYaw() + 15f; // 每tick旋转15°
            float pitch = fallingBlockEntity.getLocation().getPitch(); // pitch保持不变
            fallingBlockEntity.setRotation(yaw, pitch);
        }


        Location newLocation = currentLocation.clone().add(velocity);


        if (!isDead && checkBlockCollision(lastLocation, newLocation)) {

            Block collidedBlock = getCollidedBlock(lastLocation, newLocation);
            if (collidedBlock != null) {
                handleBlockCollision(collidedBlock);
                return;
            }
        }

        if (isDead) return;


        moveEntityWithVelocity(velocity);


        if (!isDead) {
            checkEntityCollision(newLocation);
        }

        if (isDead) return;


        renderParticles(newLocation);


        executeOnFlyCommands(newLocation);


        double distance = newLocation.distance(shooter.getLocation());
        if (distance > config.getMaxRange()) {
            destroy();
            return;
        }

        lastLocation = newLocation.clone();
    }
    

    private Location getCurrentLocation() {
        if (arrowEntity != null && !arrowEntity.isDead()) {
            return arrowEntity.getLocation();
        } else if (itemDisplayEntity != null && !itemDisplayEntity.isDead()) {
            return itemDisplayEntity.getLocation();
        } else if (bulletEntity != null && !bulletEntity.isDead()) {
            return bulletEntity.getLocation();
        } else if (fallingBlockEntity != null && !fallingBlockEntity.isDead()) {
            return fallingBlockEntity.getLocation();
        }
        return null;
    }
    

    private Block getCollidedBlock(Location from, Location to) {
        Vector direction = to.toVector().subtract(from.toVector());
        double distance = direction.length();
        

        if (distance < 0.1) {
            return null;
        }
        
        Vector normalizedDirection = direction.normalize();
        

        if ("arrow".equalsIgnoreCase(config.getModel())) {

            double step = 0.15; // 减小步长提高精度
            int steps = (int) Math.ceil(distance / step);
            
            for (int i = 0; i < steps; i++) {
                Vector pos = from.toVector().add(normalizedDirection.clone().multiply(i * step));
                Block block = from.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                

                if (block.getType() != Material.AIR && (block.getType().isSolid() || isLeafBlock(block.getType()))) {

                    BoundingBox box = block.getBoundingBox().expand(0.05);
                    if (box.contains(pos)) {
                        return block;
                    }
                }
            }
            
            return null;
        } else {

            try {

                BlockIterator iter = new BlockIterator(from.getWorld(), from.toVector(), normalizedDirection, 0, (int) Math.ceil(distance) + 1);
                while (iter.hasNext()) {
                    Block block = iter.next();
                    if (block.getType() != Material.AIR && (block.getType().isSolid() || isLeafBlock(block.getType()))) {
                        return block;
                    }
                }
                

                double step = 0.2;
                int steps = (int) Math.ceil(distance / step);
                
                for (int i = 0; i < steps; i++) {
                    Vector pos = from.toVector().add(normalizedDirection.clone().multiply(i * step));
                    Block block = from.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                    
                    if (block.getType() != Material.AIR && (block.getType().isSolid() || isLeafBlock(block.getType()))) {

                        BoundingBox box = block.getBoundingBox().expand(0.05);
                        if (box.contains(pos)) {
                            return block;
                        }
                    }
                }
            } catch (Exception e) {

                double step = 0.2;
                int steps = (int) Math.ceil(distance / step);
                
                for (int i = 0; i < steps; i++) {
                    Vector pos = from.toVector().add(normalizedDirection.clone().multiply(i * step));
                    Block block = from.getWorld().getBlockAt(pos.getBlockX(), pos.getBlockY(), pos.getBlockZ());
                    
                    if (block.getType() != Material.AIR && (block.getType().isSolid() || isLeafBlock(block.getType()))) {
                        return block;
                    }
                }
            }
            
            return null;
        }
    }
    

    private boolean checkBlockCollision(Location from, Location to) {
        return getCollidedBlock(from, to) != null;
    }


    private void moveEntityWithVelocity(Vector velocity) {
        if (arrowEntity != null && !arrowEntity.isDead()) {
            arrowEntity.setVelocity(velocity);
        } else if (itemDisplayEntity != null && !itemDisplayEntity.isDead()) {
            Location loc = itemDisplayEntity.getLocation().add(velocity);
            itemDisplayEntity.teleportAsync(loc);
        } else if (bulletEntity != null && !bulletEntity.isDead()) {
            bulletEntity.setVelocity(velocity);
        } else if (fallingBlockEntity != null && !fallingBlockEntity.isDead()) {
            fallingBlockEntity.setVelocity(velocity);
        }
    }


    private void checkEntityCollision(Location location) {

        double checkRange = "arrow".equalsIgnoreCase(config.getModel()) ? 1.5 : 1.2;

        double verticalRange = checkRange * 1.5;
        
        for (Entity entity : location.getWorld().getNearbyEntities(location, checkRange, verticalRange, checkRange)) {
            if (entity instanceof LivingEntity && entity != shooter &&
                entity != bulletEntity && entity != fallingBlockEntity && entity != arrowEntity &&
                !hitEntities.contains(entity.getUniqueId())) {
                

                if ("arrow".equalsIgnoreCase(config.getModel())) {

                    BoundingBox entityBox = entity.getBoundingBox().expand(0.2);

                    Vector direction = velocity.clone().normalize();
                    Location from = lastLocation != null ? lastLocation.clone() : location.clone().subtract(direction);
                    

                    Vector ray = direction.clone().multiply(checkRange * 2.5);
                    

                    RayTraceResult result = entityBox.rayTrace(from.toVector(), direction, ray.length());
                    if (result != null) {
                        handleEntityCollision((LivingEntity) entity);
                        break;
                    } else {

                        boolean hit = false;
                        for (double offset : new double[]{0.5, 1.0, -0.5, -1.0}) {
                            Vector offsetDir = direction.clone().add(new Vector(0, offset * 0.2, 0)).normalize();
                            result = entityBox.rayTrace(from.toVector(), offsetDir, ray.length());
                            if (result != null) {
                                hit = true;
                                break;
                            }
                        }
                        
                        if (hit) {
                            handleEntityCollision((LivingEntity) entity);
                            break;
                        }
                    }
                } else {
                    // 非箭矢类型使用改进的距离+射线检测
                    // 首先检查实体中心与子弹的直线距离
                    Location entityLoc = entity.getLocation().add(0, entity.getHeight() / 2, 0);
                    double distance = location.distance(entityLoc);
                    
                    if (distance <= checkRange) {
                        // 直接命中
                        handleEntityCollision((LivingEntity) entity);
                        break;
                    } else {
                        // 如果距离稍远，尝试使用射线检测
                        BoundingBox entityBox = entity.getBoundingBox().expand(0.2);
                        Vector direction = velocity.clone().normalize();
                        Location from = lastLocation != null ? lastLocation.clone() : location.clone().subtract(direction);
                        Vector ray = direction.clone().multiply(checkRange * 1.5);
                        
                        RayTraceResult result = entityBox.rayTrace(from.toVector(), direction, ray.length());
                        if (result != null) {
                            handleEntityCollision((LivingEntity) entity);
                            break;
                        }
                    }
                }
            }
        }
    }

    /**
     * 处理方块碰撞
     */
    private void handleBlockCollision(Block block) {
        // 箭矢类型的子弹特殊处理 - 不再让箭矢插在方块上，直接销毁
        if ("arrow".equalsIgnoreCase(config.getModel()) && arrowEntity != null && !arrowEntity.isDead()) {
            // 获取当前位置
            Location currentLocation = arrowEntity.getLocation();
            
            // 播放箭矢射中方块的音效 - 只播放一次
            currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_ARROW_HIT, 1.0f, 1.0f);
            
            // 如果配置了爆炸效果，触发爆炸
            if (config.getExplosion() != null && config.getExplosion().isEnabled()) {
                createExplosion(currentLocation);
            }
            
            // 执行落地命令
            executeOnLandCommands(currentLocation);
            
            // 直接销毁子弹
            destroy();
            return;
        }
        
        // 非箭矢类型子弹的原有处理逻辑
        if (config.getPhysics() != null && config.getPhysics().isBounce()) {
            Vector normal = getBlockNormal(block);
            double dot = velocity.dot(normal);
            Vector reflection = velocity.clone().subtract(normal.clone().multiply(2 * dot));
            velocity.setX(reflection.getX());
            velocity.setY(reflection.getY());
            velocity.setZ(reflection.getZ());
            velocity.multiply(0.5);
            bounceCount++;
            // block类型弹射时重建FallingBlock实体
            if (fallingBlockEntity != null && !fallingBlockEntity.isDead()) {
                Location loc = fallingBlockEntity.getLocation();
                fallingBlockEntity.remove();
                Material blockMaterial = Material.valueOf(config.getBlock().toUpperCase());
                fallingBlockEntity = loc.getWorld().spawnFallingBlock(loc, blockMaterial.createBlockData());
                fallingBlockEntity.setDropItem(false); // 严格禁止生成方块
                fallingBlockEntity.setGravity(false);
                fallingBlockEntity.setInvulnerable(true);
                fallingBlockEntity.setSilent(true);
                // 确保FallingBlock实体不会持久化，防止离线后加载导致生成方块
                fallingBlockEntity.setPersistent(false);
                // 设置TicksLived较大的值，确保实体在未正确移除时也会被自动移除
                fallingBlockEntity.setTicksLived(fallingBlockEntity.getTicksLived() + 1);
                fallingBlockEntity.setVelocity(velocity);
            }
            // 弹射次数或速度过小时自动销毁
            if (bounceCount >= config.getBounceLimit() || velocity.length() < 0.1) {
                Location currentLocation = getCurrentLocation();
                if (currentLocation != null) {
                    executeOnLandCommands(currentLocation);
                }
                destroy();
                return;
            }
        } else {
            Location currentLocation = getCurrentLocation();
            if (currentLocation != null) {
                executeOnLandCommands(currentLocation);
            }
            destroy();
        }
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            // 非箭矢类型播放石头碰撞音效
            if (!"arrow".equalsIgnoreCase(config.getModel())) {
                currentLocation.getWorld().playSound(currentLocation, Sound.BLOCK_STONE_BREAK, 0.5f, 1.0f);
            }
        }
    }
    
    /**
     * 处理实体碰撞
     */
    private void handleEntityCollision(LivingEntity entity) {
        hitEntities.add(entity.getUniqueId());
        penetrationCount++;
        double damage = config.getDamage();
        double hitY = getCurrentLocation() != null ? getCurrentLocation().getY() : entity.getLocation().getY();
        double headThresholdY;
        if (config.isHeadshotEnabled()) {
            if (entity instanceof Player) {
                headThresholdY = ((Player)entity).getEyeLocation().getY();
            } else {
                BoundingBox box = entity.getBoundingBox();
                headThresholdY = box.getMaxY() - (box.getHeight() / 4.0);
            }
            if (hitY >= headThresholdY) {
                damage *= config.getHeadshotMultiplier();
                // 爆头粒子/音效
                if (getCurrentLocation() != null) {
                    getCurrentLocation().getWorld().spawnParticle(Particle.CRIT, getCurrentLocation(), 10, 0.2, 0.2, 0.2, 0.1);
                    getCurrentLocation().getWorld().playSound(getCurrentLocation(), Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.2f, 1.5f);
                }
            }
        }
        if (config.isIgnoreArmor()) {
            entity.damage(damage, shooter);
        } else {
            entity.damage(damage, shooter);
        }
        if (config.getPhysics() != null) {
            Vector knockback = velocity.clone().normalize().multiply(config.getPhysics().getKnockback());
            entity.setVelocity(entity.getVelocity().add(knockback));
        }
        Location currentLocation = getCurrentLocation();
        if (currentLocation != null) {
            currentLocation.getWorld().playSound(currentLocation, Sound.ENTITY_PLAYER_ATTACK_CRIT, 0.5f, 1.0f);
            
            // 执行命中命令
            executeOnHitCommands(currentLocation, entity);
        }
        if (penetrationCount >= config.getPenetration()) {
            destroy();
        }
    }
    
    /**
     * 获取方块法向量（简化版本）
     */
    private Vector getBlockNormal(Block block) {
        // 简化版本，假设子弹从上方击中
        return new Vector(0, 1, 0);
    }
    
    /**
     * 渲染粒子效果
     */
    private void renderParticles(Location location) {
        // 如果子弹已经标记为死亡，不再渲染粒子
        if (isDead) {
            return;
        }
        
        BulletConfig.ParticlePresetConfig preset = config.getParticlePreset();
        if (preset != null) {
            if ("spiral".equalsIgnoreCase(preset.getType())) {
                int density = Math.max(1, preset.getDensity());
                Location from = lastLocation != null ? lastLocation : location;
                Vector dir = velocity.clone().normalize();
                // 选择一个不平行的向量作为辅助轴
                Vector up0 = Math.abs(dir.getY()) < 0.99 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
                Vector right = dir.clone().crossProduct(up0).normalize();
                Vector up = right.clone().crossProduct(dir).normalize();
                for (int d = 0; d < density; d++) {
                    double t = (double) d / density;
                    Location interp = from.clone().add(location.clone().subtract(from).multiply(t));
                    double time = (spiralTick + t) * preset.getRotateSpeed();
                    for (int i = 0; i < preset.getCount(); i++) {
                        double angle = time + (2 * Math.PI / preset.getCount()) * i;
                        Vector offset = right.clone().multiply(Math.cos(angle) * preset.getRadius())
                                        .add(up.clone().multiply(Math.sin(angle) * preset.getRadius()));
                        Location particleLoc = interp.clone().add(offset);
                        location.getWorld().spawnParticle(preset.getParticle(), particleLoc, 1, 0, 0, 0, 0, null, true);
                    }
                }
                spiralTick++;
            } else if ("trail".equalsIgnoreCase(preset.getType())) {
                // 拖尾粒子
                if (lastTrailLocation == null) {
                    lastTrailLocation = location.clone();
                }
                double interval = Math.max(0.01, preset.getInterval());
                double dist = lastTrailLocation.distance(location);
                if (dist >= interval) {
                    int steps = (int) (dist / interval);
                    Vector dir = location.toVector().subtract(lastTrailLocation.toVector()).normalize();
                    for (int i = 1; i <= steps; i++) {
                        Location trailLoc = lastTrailLocation.clone().add(dir.clone().multiply(i * interval));
                        location.getWorld().spawnParticle(preset.getParticle(), trailLoc, preset.getCount(), 0, 0, 0, 0, null, true);
                    }
                    lastTrailLocation = location.clone();
                }
            }
        } else if (config.getParticle() != null) {
            location.getWorld().spawnParticle(config.getParticle(), location, 1, 0, 0, 0, 0, null, true);
        }
    }
    
    /**
     * 渲染激光束效果
     * 在起点和终点之间创建密集的粒子线
     */
    private void renderLaserBeam(Location from, Location to) {
        // 获取起点和终点之间的向量
        Vector direction = to.clone().subtract(from).toVector();
        double distance = direction.length();
        direction.normalize();
        
        // 确定使用哪种粒子
        Particle particleType;
        int particleCount;
        double particleSpeed;
        
        // 优先使用粒子预设中的粒子
        if (config.getParticlePreset() != null) {
            particleType = config.getParticlePreset().getParticle();
            particleCount = Math.max(1, config.getParticlePreset().getCount());
            particleSpeed = 0.01;
        } else if (config.getParticle() != null) {
            particleType = config.getParticle();
            particleCount = 1;
            particleSpeed = 0.01;
        } else {
            // 默认粒子
            particleType = Particle.END_ROD;
            particleCount = 1;
            particleSpeed = 0.01;
        }
        
        // 计算需要多少粒子才能创建连续的光束效果
        // 每0.2格放置一个粒子，确保视觉上连续
        int steps = Math.max(5, (int)(distance * 5)); // 至少5个点，每0.2格一个粒子
        
        // 创建粒子线
        for (int i = 0; i < steps; i++) {
            double ratio = (double) i / (steps - 1);
            Location particleLoc = from.clone().add(direction.clone().multiply(ratio * distance));
            
            // 在每个点生成粒子
            from.getWorld().spawnParticle(
                particleType,
                particleLoc,
                particleCount,
                0.02, 0.02, 0.02, // 小范围随机偏移，使光束看起来更自然
                particleSpeed,
                null,
                true // 强制所有玩家都能看到
            );
            
            // 如果有粒子预设且是spiral类型，添加环绕效果使激光看起来更酷
            if (config.getParticlePreset() != null && "spiral".equalsIgnoreCase(config.getParticlePreset().getType())) {
                // 选择一个不平行的向量作为辅助轴
                Vector up0 = Math.abs(direction.getY()) < 0.99 ? new Vector(0, 1, 0) : new Vector(1, 0, 0);
                Vector right = direction.clone().crossProduct(up0).normalize();
                Vector up = right.clone().crossProduct(direction).normalize();
                
                double radius = config.getParticlePreset().getRadius() * 0.5; // 减小半径使激光看起来更集中
                int spiralPoints = Math.max(2, config.getParticlePreset().getCount());
                
                for (int j = 0; j < spiralPoints; j++) {
                    double angle = (ratio * 10) + (2 * Math.PI / spiralPoints) * j;
                    Vector offset = right.clone().multiply(Math.cos(angle) * radius)
                                   .add(up.clone().multiply(Math.sin(angle) * radius));
                    Location spiralLoc = particleLoc.clone().add(offset);
                    
                    from.getWorld().spawnParticle(
                        particleType,
                        spiralLoc,
                        1,
                        0, 0, 0,
                        0,
                        null,
                        true
                    );
                }
            }
        }
    }
    
    /**
     * 销毁子弹
     */
    public void destroy() {
        if (isDead) {
            return;
        }
        isDead = true;

        // 先获取当前位置用于效果
        Location effectLocation = getCurrentLocation();

        // 优先移除FallingBlock实体，确保不会生成方块
        if (fallingBlockEntity != null && !fallingBlockEntity.isDead()) {
            // 强制设置为不生成方块，以防万一
            fallingBlockEntity.setDropItem(false);
            // 增加安全性：确保实体将很快自动移除，防止意外生成方块
            fallingBlockEntity.setTicksLived(2000);
            fallingBlockEntity.remove();
        }

        // 移除其他实体
        if (arrowEntity != null && !arrowEntity.isDead()) {
            arrowEntity.remove();
        }
        if (itemDisplayEntity != null && !itemDisplayEntity.isDead()) {
            itemDisplayEntity.remove();
        }
        if (bulletEntity != null && !bulletEntity.isDead()) {
            bulletEntity.remove();
        }

        if (effectLocation == null) {
            return;
        }

        // 爆炸效果
        if (config.getExplosion() != null) {
            createExplosion(effectLocation);
        }
    }
    
    /**
     * 爆炸效果（保留自定义粒子/伤害）
     */
    private void createExplosion(Location location) {
        BulletConfig.ExplosionConfig explosion = config.getExplosion();
        if (explosion == null || !explosion.isEnabled()) return;

        // 播放自定义爆炸音效
        if (explosion.getSound() != null && !explosion.getSound().isEmpty()) {
            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(explosion.getSound().toUpperCase());
                
                // 使用自定义的音量和音调
                float volume = explosion.getSoundVolume();
                float pitch = explosion.getSoundPitch();
                
                // 增加爆炸音效的可听范围
                // 1. 在爆炸位置播放音效 - 只播放一次
                location.getWorld().playSound(location, sound, volume, pitch);
                
                // 2. 在爆炸范围内的多个点播放音效，增加可听范围 - 限制数量
                double radius = explosion.getRadius();
                // 在爆炸范围内随机选择几个点播放音效，减少点数量
                int extraSoundPoints = Math.min(2, (int)(radius));
                for (int i = 0; i < extraSoundPoints; i++) {
                    double angle = Math.random() * Math.PI * 2;
                    double distance = Math.random() * radius * 0.7;
                    double x = Math.cos(angle) * distance;
                    double z = Math.sin(angle) * distance;
                    Location soundLoc = location.clone().add(x, 0, z);
                    location.getWorld().playSound(soundLoc, sound, volume * 0.6f, pitch);
                }
                
                // 3. 为所有附近玩家播放音效 - 使用更合理的范围
                double hearingRange = Math.min(radius * 2.5, 30); // 限制最大听力范围为30格
                for (Player player : Bukkit.getOnlinePlayers()) {
                    if (player.getWorld().equals(location.getWorld()) && 
                        player.getLocation().distance(location) <= hearingRange && 
                        player.getLocation().distance(location) > radius) { // 只为超出爆炸范围的玩家播放
                        // 根据距离调整音量
                        float distanceVolume = (float) (volume * (1 - player.getLocation().distance(location) / hearingRange));
                        if (distanceVolume > 0.1f) {
                            player.playSound(player.getLocation(), sound, distanceVolume, pitch);
                        }
                    }
                }
            } catch (IllegalArgumentException e) {
                // sound无效则不播放
                plugin.getLogger().warning("无效的爆炸音效: " + explosion.getSound() + "，不播放音效");
            }
        }

        // 爆炸粒子效果
        if (explosion.getParticle() != null) {
            // 增加粒子效果的可见范围，设置为true表示所有玩家都能看到
            location.getWorld().spawnParticle(
                explosion.getParticle(),
                location,
                explosion.getParticleCount(),
                explosion.getParticleSpread(),
                explosion.getParticleSpread(),
                explosion.getParticleSpread(),
                0.1,
                null,
                true
            );
        }

        // 额外自定义爆炸伤害
        for (Entity entity : location.getWorld().getNearbyEntities(location, explosion.getRadius(), explosion.getRadius(), explosion.getRadius())) {
            if (entity instanceof LivingEntity && entity != shooter && entity != bulletEntity && entity != fallingBlockEntity && entity != arrowEntity) {
                double distance = entity.getLocation().distance(location);
                if (distance <= explosion.getRadius()) {
                    double damage = explosion.getDamage() * (1 - distance / explosion.getRadius());
                    ((LivingEntity) entity).damage(damage, shooter);
                }
            }
        }
    }
    
    /**
     * 检查子弹是否已销毁
     */
    public boolean isDead() {
        return isDead || 
               (bulletEntity != null && bulletEntity.isDead()) || 
               (fallingBlockEntity != null && fallingBlockEntity.isDead()) ||
               (arrowEntity != null && arrowEntity.isDead()) ||
               (itemDisplayEntity != null && itemDisplayEntity.isDead());
    }
    
    /**
     * 获取子弹位置
     */
    public Location getLocation() {
        Location currentLocation = getCurrentLocation();
        return currentLocation != null ? currentLocation.clone() : null;
    }
    
    /**
     * 获取子弹配置
     */
    public BulletConfig getConfig() {
        return config;
    }
    
    /**
     * 获取射击者
     */
    public Player getShooter() {
        return shooter;
    }

    /**
     * 播放射击音效
     */
    private void playShootSound(Location location) {
        BulletConfig.ShootSoundConfig soundConfig = config.getShootSound();
        if (soundConfig != null && soundConfig.isEnabled()) {
            try {
                org.bukkit.Sound sound = org.bukkit.Sound.valueOf(soundConfig.getSound().toUpperCase());
                location.getWorld().playSound(location, sound, soundConfig.getVolume(), soundConfig.getPitch());
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("无效的射击音效: " + soundConfig.getSound() + "，不播放音效");
            }
        }
    }

    /**
     * 执行飞行时命令（支持每条命令独立interval）
     */
    private void executeOnFlyCommands(Location location) {
        if (isDead) return;
        if (flyCommandCmds == null || flyCommandCmds.isEmpty()) return;
        for (int i = 0; i < flyCommandCmds.size(); i++) {
            int tick = flyCommandTicks.get(i) + 1;
            flyCommandTicks.set(i, tick);
            int interval = flyCommandIntervals.get(i);
            int max = flyCommandMax.get(i);
            int fired = flyCommandFired.get(i);
            if (max >= 0 && fired >= max) continue;
            if (interval > 0 && tick % interval == 0) {
                commandExecutor.executeCommands(java.util.Collections.singletonList(flyCommandCmds.get(i)), location, null, shooter, config.getName());
                flyCommandFired.set(i, fired + 1);
            }
        }
    }
    
    /**
     * 执行落地时命令
     */
    private void executeOnLandCommands(Location location) {
        if (location == null) return;
        
        commandExecutor.executeOnLandCommands(config.getName(), location, shooter);
    }
    
    /**
     * 执行命中时命令
     */
    private void executeOnHitCommands(Location location, LivingEntity target) {
        if (location == null || target == null) return;
        
        commandExecutor.executeOnHitCommands(config.getName(), location, target, shooter);
    }

    // 1. 在类内添加辅助方法
    private boolean isLeafBlock(Material material) {
        String name = material.name();
        return name.contains("LEAVES") || name.equals("AZALEA_LEAVES") || name.equals("MANGROVE_LEAVES");
    }
    
    /**
     * 检查实体是否在路径上
     * @param start 路径起点
     * @param end 路径终点
     * @param point 实体位置
     * @param maxDistance 最大距离
     * @return 是否在路径上
     */
    private boolean isEntityInPath(Vector start, Vector end, Vector point, double maxDistance) {
        // 计算方向向量
        Vector direction = end.clone().subtract(start).normalize();
        
        // 计算实体到起点的向量
        Vector toPoint = point.clone().subtract(start);
        
        // 计算投影长度
        double projLength = toPoint.dot(direction);
        
        // 如果投影长度为负，说明实体在起点之前
        if (projLength < 0) {
            return start.distance(point) <= maxDistance;
        }
        
        // 如果投影长度大于路径长度，说明实体在终点之后
        double pathLength = end.distance(start);
        if (projLength > pathLength) {
            return end.distance(point) <= maxDistance;
        }
        
        // 计算实体到路径的最短距离
        Vector projection = start.clone().add(direction.clone().multiply(projLength));
        double distance = projection.distance(point);
        
        // 如果距离小于阈值，说明实体在路径上
        return distance <= maxDistance;
    }
} 