package com.eseabsolute.magicbullet.models;

/**
 * 绑定配置类
 * 存储物品绑定的子弹配置信息
 * 
 * @author EseAbsolute
 * @version 1.0.0
 */
public class BindingConfig {
    
    private final String bulletName;
    private final String direction;
    private final double speed;
    private final String trigger;
    
    public BindingConfig(String bulletName, String direction, double speed, String trigger) {
        this.bulletName = bulletName;
        this.direction = direction;
        this.speed = speed;
        this.trigger = trigger;
    }
    
    // Getters
    public String getBulletName() {
        return bulletName;
    }
    
    public String getDirection() {
        return direction;
    }
    
    public double getSpeed() {
        return speed;
    }
    
    public String getTrigger() {
        return trigger;
    }
    
    @Override
    public String toString() {
        return "BindingConfig{" +
                "bulletName='" + bulletName + '\'' +
                ", direction='" + direction + '\'' +
                ", speed=" + speed +
                ", trigger='" + trigger + '\'' +
                '}';
    }
} 