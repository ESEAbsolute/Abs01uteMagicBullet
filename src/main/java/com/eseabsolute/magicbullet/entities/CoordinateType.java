package com.eseabsolute.magicbullet.entities;

import org.bukkit.Location;
import org.bukkit.util.Vector;

import java.util.Arrays;
import java.util.List;

public enum CoordinateType {
    RELATIVE {
        @Override
        public Location calculateLocation(Location location, Vector offset) {
            return location.clone().add(offset.getX(), offset.getY(), offset.getZ());
        }

        @Override
        public Vector toRelativeOffset(Location direction, Vector vector) {
            return vector;
        }
    },
    LOCAL {
        @Override
        public Location calculateLocation(Location location, Vector offset) {
            Vector relativeOffset = toRelativeOffset(location, offset);
            return location.clone().add(relativeOffset.getX(), relativeOffset.getY(), relativeOffset.getZ());
        }

        @Override
        public Vector toRelativeOffset(Location direction, Vector vector) {
            float yaw = (float) Math.toRadians(direction.getYaw());
            float pitch = (float) Math.toRadians(-direction.getPitch());

            double cosYaw = Math.cos(yaw);
            double sinYaw = Math.sin(yaw);
            double cosPitch = Math.cos(pitch);
            double sinPitch = Math.sin(pitch);

            double forwardX = -sinYaw * cosPitch;
            double forwardY = sinPitch;
            double forwardZ = cosYaw * cosPitch;

            double rightX = cosYaw;
            double rightY = 0;
            double rightZ = sinYaw;

            double upX = sinYaw * sinPitch;
            double upY = cosPitch;
            double upZ = -cosYaw * sinPitch;

            double worldDirX = rightX * vector.getX() + upX * vector.getY() + forwardX * vector.getZ();
            double worldDirY = rightY * vector.getX() + upY * vector.getY() + forwardY * vector.getZ();
            double worldDirZ = rightZ * vector.getX() + upZ * vector.getY() + forwardZ * vector.getZ();

            return new Vector(worldDirX, worldDirY, worldDirZ);
        }
    };

    public abstract Location calculateLocation(Location location, Vector offset);
    public abstract Vector toRelativeOffset(Location direction, Vector vector);

    public static List<String> getAllTypes() {
        return Arrays.stream(BulletType.values())
                .map(Enum::name)
                .toList();
    }
}
