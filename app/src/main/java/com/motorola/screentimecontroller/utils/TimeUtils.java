package com.motorola.screentimecontroller.utils;

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
}
