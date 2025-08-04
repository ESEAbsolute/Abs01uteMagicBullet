package com.eseabsolute.magicbullet.entities;

import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public enum BulletShape {
    SINGLE {
        @Override
        public List<Vector> generate(Vector relativeCoordinate) {
            return List.of(relativeCoordinate);
        }
    },
    CIRCLE {
        @Override
        public List<Vector> generate(Vector relativeCoordinate) {
            List<Vector> bullets = new ArrayList<>();
            bullets.add(relativeCoordinate);
            // logic
            return bullets;
        }
    },
    SQUARE {
        @Override
        public List<Vector> generate(Vector relativeCoordinate) {
            List<Vector> bullets = new ArrayList<>();
            bullets.add(relativeCoordinate);
            // logic
            return bullets;
        }
    },
    CUBE {
        @Override
        public List<Vector> generate(Vector relativeCoordinate) {
            List<Vector> bullets = new ArrayList<>();
            bullets.add(relativeCoordinate);
            // logic
            return bullets;
        }
    },
    SPHERE {
        @Override
        public List<Vector> generate(Vector relativeCoordinate) {
            List<Vector> bullets = new ArrayList<>();
            bullets.add(relativeCoordinate);
            // logic
            return bullets;
        }
    };

    public abstract List<Vector> generate(Vector relativeCoordinate);

    public static List<String> getAllTypes() {
        return Arrays.stream(BulletType.values())
                .map(Enum::name)
                .toList();
    }
}
