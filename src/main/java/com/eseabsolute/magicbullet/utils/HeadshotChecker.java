package com.eseabsolute.magicbullet.utils;

import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.*;

public class HeadshotChecker {
    public static boolean isHeadshot(LivingEntity target, double hitY) {
        if (!isHeadshotCandidate(target)) return false;
        return hitY >= getHeadThresholdY(target);
    }

    // https://jd.papermc.io/folia/1.21/org/bukkit/entity/LivingEntity.html
    // Update manually if needed.
    private static boolean isHeadshotCandidate(LivingEntity target) {
        return target instanceof AbstractSkeleton     // Skeleton, WitherSkeleton, Bogged ......
                || target instanceof AbstractVillager // Villager, WanderingTrader
                || target instanceof Blaze
                || target instanceof Boss             // EnderDragon, Wither
                || target instanceof Breeze
                || target instanceof Creaking
                || target instanceof Creeper
                || target instanceof Enderman
                || target instanceof Giant
                || target instanceof HumanEntity      // Player
                || target instanceof Illager          // Evoker, Illusioner, Pillager, Vindicator
                || target instanceof Golem            // IronGolem, Snowman
                || target instanceof PiglinAbstract   // Piglin, PiglinBrute
                || target instanceof Warden
                || target instanceof Witch
                || target instanceof Zombie;          // Drowned, Husk, PigZombie, ZombieVillager
    }

    private static double getHeadThresholdY(LivingEntity target) {
        if (target instanceof EnderDragon) {
            for (ComplexEntityPart part : ((EnderDragon) target).getParts()) {
                if (part.getName().equalsIgnoreCase("head")) {
                    return part.getBoundingBox().getMinY();
                }
            }
        }
        double eyeHeight = target.getEyeLocation().getY(); // Eye = Head / 2
        double boxUpperHeight = target.getBoundingBox().getMaxY();
        return eyeHeight - (boxUpperHeight - eyeHeight);
    }
}
