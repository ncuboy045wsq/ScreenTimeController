package com.motorola.screentimecontroller.utils;

import java.text.DecimalFormat;
import java.util.Map;
import java.util.Set;

public class TaskUsageUtil {
    public static long getTotalUsage(Map<Integer, Map<Integer, Long>> weekUsageInfo) {
        long totalUsage = 0;

        if (weekUsageInfo == null) {
            return totalUsage;
        }

        Set<Integer> weekdayKeys = weekUsageInfo.keySet();
        for (Integer weekdayKey : weekdayKeys) {
            Map<Integer, Long> usageInfoByHour = weekUsageInfo.get(weekdayKey);
            if (usageInfoByHour == null) {
                continue;
            }
            Set<Integer> usageInfoByHourKeys = usageInfoByHour.keySet();
            for (Integer usageInfoByHourKey : usageInfoByHourKeys) {
                Long usageByHour = usageInfoByHour.get(usageInfoByHourKey);
                if (usageByHour == null) {
                    continue;
                }

                totalUsage += usageByHour;
            }
        }

        return totalUsage;
    }

    public static String getWeekInscreaseFormat(long totalUsageWeek, long totalUsageLastWeek) {
        DecimalFormat decimalFormat = new DecimalFormat(".00"); //构造方法的字符格式这里如果小数不足2位,会以0补足.
        return decimalFormat.format((totalUsageWeek - totalUsageLastWeek) * 1.0f / totalUsageLastWeek);
    }
}
