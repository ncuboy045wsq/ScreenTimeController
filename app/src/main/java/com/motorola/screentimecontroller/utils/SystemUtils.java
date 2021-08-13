package com.motorola.screentimecontroller.utils;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.text.TextUtils;

import java.util.List;

public class SystemUtils {

    public static boolean isSystemApp(ApplicationInfo appInfo) {
        try {
            return appInfo != null && (isSystemAppReal(appInfo) || isUpdatedSystemApp(appInfo));
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean isSystemAppReal(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM) != 0;
    }

    private static boolean isUpdatedSystemApp(ApplicationInfo applicationInfo) {
        return (applicationInfo.flags & ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0;
    }
}
