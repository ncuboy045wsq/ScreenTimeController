package com.motorola.screentimecontroller.utils;

import android.content.pm.PackageInfo;
import android.util.Log;

import java.util.Calendar;
import java.util.Map;
import java.util.Set;

public class TimeUtils {

    public static final long ONE_DAY = 24 * 60 * 60 * 1000;
    public static final long ONE_HOUR = 60 * 60 * 1000;
    public static final long ONE_MINUTE = 60 * 1000;


    /**
     * 返回格式化后的时间, 格式例子: 18:30
     *
     * @return
     */
    public static String getTimeHHmm(long time) {

        if (time > ONE_DAY) {
            time = time % ONE_DAY;
        }

        return time / ONE_HOUR + ":" + (time % ONE_HOUR) / ONE_MINUTE;
    }

    public static Calendar getWeekFirstDayCalendar() {
        final Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - (calendar.get(Calendar.DAY_OF_WEEK) + 7));
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        return calendar;
    }

    public static long getDailyUsage(Calendar calendar, Map<Integer, Map<Integer, Long>> taskUsageInfosWeek) {

        int totalDay = 0;
        long totalUsage = 0;
        if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
            totalDay = calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek();
        } else {
            totalDay = calendar.get(Calendar.DAY_OF_WEEK) + 7 - calendar.getFirstDayOfWeek();
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        Set<Integer> weekDayKeys = taskUsageInfosWeek.keySet();
        for (Integer weekDayKey : weekDayKeys) {
            Map<Integer, Long> usageByHour = taskUsageInfosWeek.get(weekDayKey);
            if (usageByHour != null) {
                Set<Integer> usageByHourKeys = usageByHour.keySet();
                for (Integer usageByHourKey : usageByHourKeys) {
                    Long usage = usageByHour.get(usageByHourKey);
                    if (usage != null) {
                        totalUsage += usage;
                    }
                }
            }
        }

        return totalDay > 0 ? totalUsage / totalDay : totalUsage;
    }

    private static String getUsageDescription(int firstDayOfWeek, int index, Long useAge) {

        int currentDayOfWeek = 0;

        if (firstDayOfWeek + index <= Calendar.SATURDAY) {
            currentDayOfWeek = firstDayOfWeek + index;
        } else {
            currentDayOfWeek = firstDayOfWeek + index - Calendar.SATURDAY;
        }

        switch (currentDayOfWeek) {
            case Calendar.MONDAY:
                return "MONDAY        " + TimeUtils.getUsageDescription(useAge);
            case Calendar.TUESDAY:
                return "TUESDAY       " + TimeUtils.getUsageDescription(useAge);
            case Calendar.WEDNESDAY:
                return "WEDNESDAY " + TimeUtils.getUsageDescription(useAge);
            case Calendar.THURSDAY:
                return "THURSDAY    " + TimeUtils.getUsageDescription(useAge);
            case Calendar.FRIDAY:
                return "FRIDAY           " + TimeUtils.getUsageDescription(useAge);
            case Calendar.SATURDAY:
                return "SATURDAY     " + TimeUtils.getUsageDescription(useAge);
            case Calendar.SUNDAY:
                return "SUNDAY         " + TimeUtils.getUsageDescription(useAge);
            default:
                return "";
        }
    }

    public static String getTaskUsageByDayDesc(Calendar calendar, Map<Integer, Map<Integer, Long>> taskUsageInfosWeek) {
        StringBuilder screenUsageBuilder = new StringBuilder();

        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < Calendar.SATURDAY; i++) {
            Map<Integer, Long> usageDailyByHour = taskUsageInfosWeek.get(i);
            long usageDaily = 0;
            if (usageDailyByHour != null) {
                Set<Integer> usageKeys = usageDailyByHour.keySet();
                for (Integer usageKey : usageKeys) {
                    Long usageByHour = usageDailyByHour.get(usageKey);
                    if (usageByHour != null) {
                        usageDaily += usageByHour;
                    }
                }
            }
            screenUsageBuilder.append(getUsageDescription(firstDayOfWeek, i, usageDaily));
            screenUsageBuilder.append("\n");
        }
        return screenUsageBuilder.toString();
    }

    public static String getTaskUsageByHourDesc(Calendar calendar, Map<Integer, Map<Integer, Long>> taskUsageInfosWeek) {

        StringBuilder screenUsageBuilder = new StringBuilder();

        int currentDayIndex = calendar.get(Calendar.DAY_OF_WEEK);
        if (currentDayIndex > calendar.getFirstDayOfWeek()) {
            currentDayIndex -= calendar.getFirstDayOfWeek();
        } else {
            currentDayIndex = currentDayIndex + 7 - calendar.getFirstDayOfWeek();
        }

        Map<Integer, Long> usageDailyByHour = taskUsageInfosWeek.get(currentDayIndex);
        long usageHours = 0;
        if (usageDailyByHour != null) {
            for (int i = 0; i < 24; i++) {
                Long usageByHour = usageDailyByHour.get(i);
                if (usageByHour != null) {
                    usageHours += usageByHour;
                }

                if ((i + 1) % 6 == 0) {
//                    getUsageDescription(calendar.getFirstDayOfWeek(), currentDayIndex, usageDaily);

                    switch ((i + 1) / 6) {
                        case 1:
                            screenUsageBuilder.append("0点-6点:      ");
                            break;
                        case 2:
                            screenUsageBuilder.append("6点-12点:    ");
                            break;
                        case 3:
                            screenUsageBuilder.append("12点-18点:  ");
                            break;
                        case 4:
                            screenUsageBuilder.append("18点-24点:  ");
                            break;
                        default:
                            break;
                    }

                    screenUsageBuilder.append(getUsageDescription(usageHours));
                    if (i != 23) {
                        screenUsageBuilder.append("\n");
                    }
                }
            }
        }


        return screenUsageBuilder.toString();
    }

    public static String getUsageDescription(Long usage) {
        return usage == null ? "" : (getHour(usage) + " : " + getMinute(usage) + " : " + getSeconds(usage));
    }

    private static String getSeconds(long usage) {
        long minuteMillis = usage % (60 * 60 * 1000);
        return minuteMillis % (60 * 1000) / 1000 + " S";
    }

    private static String getMinute(long usage) {
        return usage % (60 * 60 * 1000) / (60 * 1000) + " M";
    }

    private static String getHour(long usage) {
        return usage / (60 * 60 * 1000) + " H";
    }
}
