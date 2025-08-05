package com.eseabsolute.magicbullet.utils;

import org.bukkit.entity.Entity;
import org.bukkit.util.RayTraceResult;

public class EntityRayTraceResult {
    private final Entity entity;
    private final RayTraceResult rayTraceResult;

    public EntityRayTraceResult(Entity entity, RayTraceResult result) {
        this.entity = entity;
        this.rayTraceResult = result;
    }

    public Entity getEntity() {
        return entity;
    }

    public RayTraceResult getRayTraceResult() {
        return rayTraceResult;
    }
}
