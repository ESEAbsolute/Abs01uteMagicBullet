package com.eseabsolute.magicbullet.entities.properties;

import java.util.Arrays;
import java.util.List;

public enum BulletType {
    PROJECTILE,
    LASER;

    public static List<String> getAllTypes() {
        return Arrays.stream(values())
                .map(Enum::name)
                .toList();
    }
}
