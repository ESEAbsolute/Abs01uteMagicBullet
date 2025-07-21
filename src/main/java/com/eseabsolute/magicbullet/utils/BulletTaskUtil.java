package com.eseabsolute.magicbullet.utils;

import io.papermc.paper.threadedregions.scheduler.RegionScheduler;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

public class BulletTaskUtil {
    /**
     * 只支持 Folia 的子弹调度
     */
    public static void runBulletTask(Plugin plugin, Player anchor, Runnable runnable) {
        RegionScheduler scheduler = plugin.getServer().getRegionScheduler();

        scheduler.runAtFixedRate(plugin, anchor.getLocation(), task -> {
            try {
                runnable.run();
            } catch (Exception e) {
                // 记录异常但不中断任务
                plugin.getLogger().warning("子弹任务执行出错: " + e.getMessage());
                e.printStackTrace();
            }

            // 如果子弹已经死亡，取消任务
            // 注意：runnable内部应该自行判断是否需要取消任务
        }, 1L, 1L);
    }
} 