package com.motorola.screentimecontroller.database.config;

import motorola.core_services.screentimecontroller.database.config.BaseColumn;

public class TaskUsageInfoTable extends BaseColumn {
    // 表名
    public static final String TABLE_NAME = "screen_usage_info";
    // 应用包名
    public static String PACKAGE_NAME = "package_name";
    // 应用uid
    public static String UID = "uid";
    // 起始时间(同 currentTimeMillis())
    public static String START_TIME = "start_time";
    // 使用时长
    public static String END_TIME = "end_time";
}
