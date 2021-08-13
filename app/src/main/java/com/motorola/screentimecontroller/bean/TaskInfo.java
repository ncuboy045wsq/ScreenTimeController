package com.motorola.screentimecontroller.bean;

import android.graphics.drawable.Drawable;

public class TaskInfo {
    private boolean isServer;
    private int blockType;
    private String packageName;
    private String appName;
    private Integer uid;
    private Drawable icon;
    private long maxUsage;

    public boolean isServer() {
        return isServer;
    }

    public void setServer(boolean server) {
        isServer = server;
    }

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

    public String getAppName() {
        return appName;
    }

    public void setAppName(String appName) {
        this.appName = appName;
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

    public long getMaxUsage() {
        return maxUsage;
    }

    public void setMaxUsage(long maxUsage) {
        this.maxUsage = maxUsage;
    }
}