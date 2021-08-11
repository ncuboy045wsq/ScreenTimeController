package com.motorola.screentimecontroller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MainActivity extends Activity {
    private ScreenTimeControllerModel mScreenTimeControllerModel;
    private ScreenTimeControllerService.ScreenTimeControllerBinder screenTimeControllerBinder;

    /**
     * 这一周的平均使用时长
     */
    private TextView mTvDailyAverageInfo;
    /**
     * 显示每天使用时长
     */
    private TextView mTvScreenUsageByDay;

    /**
     * 本周任务使用时长
     */
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosWeek;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        mScreenTimeControllerModel = new ScreenTimeControllerModel(this);

        mTvScreenUsageByDay = findViewById(R.id.tv_screenUsageByDay);

        TextView tvFloatValueChangeLabel = findViewById(R.id.tv_floatValueChangeLabel);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher_background);
        drawable.setBounds(0, 0, 15, 15);
        tvFloatValueChangeLabel.setCompoundDrawables(drawable, null, null, null);

        mTvDailyAverageInfo = findViewById(R.id.tv_dailyAverageInfo);

        LinearLayout llViewAllActivities = findViewById(R.id.ll_viewAllActivities);
        llViewAllActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TaskUsageInfoActivity.class);
                startActivity(intent);
            }
        });

        Button btStopTime = findViewById(R.id.bt_stopTime);
        btStopTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置停用时间
                Intent intent = new Intent(MainActivity.this, BlockUpTimeSettingActivity.class);
                startActivity(intent);
            }
        });

        Button btAppTimeLimit = findViewById(R.id.bt_appTimeLimit);
        btAppTimeLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置 app 最大使用时长
                Intent intent = new Intent(MainActivity.this, TaskBlockUpInfoActivity.class);
                startActivity(intent);
            }
        });

        Button btAlwayAllows = findViewById(R.id.bt_alwayAllows);
        btAlwayAllows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置始终允许
                Intent intent = new Intent(MainActivity.this, TaskWhiteListActivity.class);
                startActivity(intent);
            }
        });

        loadTaskUsageInfo();
    }

    private void loadTaskUsageInfo() {

        final Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - (calendar.get(Calendar.DAY_OF_WEEK) + 7));
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        final long firstMillisOfWeek = calendar.getTimeInMillis();
        final long lastMillisOfWeek = firstMillisOfWeek + TimeUtils.ONE_DAY * 7;

        mScreenTimeControllerModel.queryTaskUsageInfo(new ScreenTimeControllerModel.OnResult() {

            @Override
            public void onResult(Object object) {

                if (object != null) {
                    mTaskUsageInfosWeek = (Map<Integer, Map<Integer, Long>>) object;
                } else {
                    mTaskUsageInfosWeek = new HashMap<>();
                }

                updateTaskUsageByDay(calendar);
                updateDailyAverage(calendar);
            }
        }, calendar, firstMillisOfWeek, lastMillisOfWeek);
    }

    private void updateDailyAverage(Calendar calendar) {

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

        for (int i = 0; i < Calendar.SATURDAY; i++) {
            Set<Integer> weekDayKeys = mTaskUsageInfosWeek.keySet();
            for (Integer weekDayKey : weekDayKeys) {
                Map<Integer, Long> usageByHour = mTaskUsageInfosWeek.get(weekDayKey);
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
        }

        mTvDailyAverageInfo.setText(getResources().getString(R.string.daily_average_regex,
                (totalUsage / TimeUtils.ONE_HOUR) + "",
                ((totalUsage % TimeUtils.ONE_HOUR) / TimeUtils.ONE_MINUTE) + ""));
    }

    private void updateTaskUsageByDay(Calendar calendar) {
        StringBuilder screenUsageBuilder = new StringBuilder();

        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 1; i <= Calendar.SATURDAY; i++) {
            Map<Integer, Long> usageDailyByHour = mTaskUsageInfosWeek.get(i);
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

        mTvScreenUsageByDay.setText(screenUsageBuilder);
    }

//    private boolean isTimeMatch(long firstMillisOfWeek, long lastMillisOfWeek, TaskUsageInfo taskUsageInfo) {
//        if (firstMillisOfWeek <= taskUsageInfo.getStartTime() && taskUsageInfo.getStartTime() < lastMillisOfWeek) {
//            return true;
//        } else if (firstMillisOfWeek < taskUsageInfo.getEndTime() && taskUsageInfo.getEndTime() <= lastMillisOfWeek) {
//            return true;
//        } else if (taskUsageInfo.getStartTime() < firstMillisOfWeek && taskUsageInfo.getEndTime() > lastMillisOfWeek) {
//            return true;
//        }
//
//        return false;
//    }

    private String getUsageDescription(int firstDayOfWeek, int i, Long useAge) {

        int currentDayOfWeek = 0;

        if (firstDayOfWeek + i <= Calendar.SATURDAY) {
            currentDayOfWeek = firstDayOfWeek + i;
        } else {
            currentDayOfWeek = firstDayOfWeek + i - Calendar.SATURDAY;
        }

        switch (currentDayOfWeek) {
            case Calendar.MONDAY:
                return "Monday " + getUsageDescription(useAge);
            case Calendar.TUESDAY:
                return "TUESDAY " + getUsageDescription(useAge);
            case Calendar.WEDNESDAY:
                return "WEDNESDAY " + getUsageDescription(useAge);
            case Calendar.THURSDAY:
                return "THURSDAY " + getUsageDescription(useAge);
            case Calendar.FRIDAY:
                return "FRIDAY " + getUsageDescription(useAge);
            case Calendar.SATURDAY:
                return "SATURDAY " + getUsageDescription(useAge);
            case Calendar.SUNDAY:
                return "SUNDAY " + getUsageDescription(useAge);
            default:
                return "";
        }
    }

    private String getUsageDescription(Long usage) {
        return usage == null ? "" : (getHour(usage) + " : " + getMinute(usage) + " : " + getSeconds(usage));
    }

    private String getSeconds(long usage) {
        long minuteMillis = usage % (60 * 60 * 1000);
        return minuteMillis % (60 * 1000) + " S";
    }

    private String getMinute(long usage) {
        return usage % (60 * 60 * 1000) / (60 * 1000) + " M";
    }

    private String getHour(long usage) {
        return usage / (60 * 60 * 1000) + " H";
    }
}
