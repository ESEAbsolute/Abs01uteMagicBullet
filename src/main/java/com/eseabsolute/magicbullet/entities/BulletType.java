package com.eseabsolute.magicbullet.entities;

import java.util.Arrays;
import java.util.List;

public enum BulletType {
    PROJECTILE,
    LASER;

    public static List<String> getAllTypes() {
        return Arrays.stream(BulletType.values())
                .map(Enum::name)
                .toList();
    }
}
