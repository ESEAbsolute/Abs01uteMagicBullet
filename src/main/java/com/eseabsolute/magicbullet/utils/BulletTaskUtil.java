package com.eseabsolute.magicbullet.utils;

import com.eseabsolute.magicbullet.Abs01uteMagicBulletPlugin;
import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class BulletTaskUtil {
    public static void runBulletTask(Abs01uteMagicBulletPlugin plugin, Player anchor, Runnable runnable) {
        RegionScheduler scheduler = plugin.getServer().getRegionScheduler();

        scheduler.runAtFixedRate(plugin, anchor.getLocation(), task -> {
            try {
                runnable.run();
            } catch (Exception e) {
                plugin.getMessageUtils().log(Level.WARNING, "log.warning.bullet.task.execute", e.getMessage());
                e.printStackTrace();
            }

            // 如果子弹已经死亡，取消任务
            // 注意：runnable内部应该自行判断是否需要取消任务
        }, 1L, 1L);
    }
} 