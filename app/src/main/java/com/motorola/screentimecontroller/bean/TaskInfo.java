package com.motorola.screentimecontroller.bean;

import android.graphics.drawable.Drawable;

public class TaskInfo {
    private int blockType;
    private String packageName;
    private Integer uid;
    private Drawable icon;
    private Long maxUsage;

    public int getBlockType() {
        return blockType;
    }

    public void setBlockType(int blockType) {
        this.blockType = blockType;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Integer getUid() {
        return uid;
    }

    public void setUid(Integer uid) {
        this.uid = uid;
    }

    public Drawable getIcon() {
        return icon;
    }

    public void setIcon(Drawable icon) {
        this.icon = icon;
    }

    public Long getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(Long maxUsage) {
        this.maxUsage = maxUsage;
    }
}