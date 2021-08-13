package com.motorola.screentimecontroller;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;
import com.motorola.screentimecontroller.utils.TaskUsageUtil;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
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
     * 本周使用时长环比上周增长标题
     */
    private TextView mTvTaskUsageIncreasementLabel;
    /**
     * 本周使用时长环比上周增长
     */
    private TextView mTvTaskUsageIncreasement;
    /**
     * 最后更新时间
     */
    private TextView mTvUpdateTime;

    /**
     * 本周任务使用时长
     */
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosWeek;
    /**
     * 上周任务使用时长
     */
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosLastWeek;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        mScreenTimeControllerModel = new ScreenTimeControllerModel(this);

        mTvScreenUsageByDay = findViewById(R.id.tv_screenUsageByDay);

        mTvDailyAverageInfo = findViewById(R.id.tv_dailyAverageInfo);
        mTvTaskUsageIncreasementLabel = findViewById(R.id.tv_taskUsageIncreasementLabel);
        mTvTaskUsageIncreasement = findViewById(R.id.tv_taskUsageIncreasement);
        mTvUpdateTime = findViewById(R.id.tv_updateTime);

        findViewById(R.id.bt_viewAllActivities)
                .setOnClickListener(new View.OnClickListener() {
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

                updateDailyAverage(calendar);
                updateTaskUsageByDay(calendar);
                updateTaskUsageIncreasement();
            }
        }, firstMillisOfWeek, lastMillisOfWeek);

        mScreenTimeControllerModel.queryTaskUsageInfo(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                if (obj != null) {
                    mTaskUsageInfosLastWeek = (Map<Integer, Map<Integer, Long>>) obj;
                } else {
                    mTaskUsageInfosLastWeek = new HashMap<>();
                }
                updateTaskUsageIncreasement();
            }
        }, firstMillisOfWeek - 7 * TimeUtils.ONE_DAY, firstMillisOfWeek);

        mScreenTimeControllerModel.queryTaskUsageInfoUpdateTime(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                if (obj instanceof Long) {
                    Long updateTime = (Long) obj;
                    mTvUpdateTime.setText(getString(R.string.update_time_regex, new Date(updateTime).toLocaleString()));
                }
            }
        });
    }

    private void updateTaskUsageIncreasement() {

        if (mTaskUsageInfosWeek == null || mTaskUsageInfosLastWeek == null) {
            return;
        }

        long totalUsageWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosWeek);
        long totalUsageLastWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosLastWeek);
        Log.e("lk_test", getClass().getSimpleName() + ".updateTaskUsageIncreasement totalUsageWeek " + totalUsageWeek + " totalUsageLastWeek " + totalUsageLastWeek);
        if (totalUsageLastWeek == 0) {
            mTvTaskUsageIncreasement.setVisibility(View.GONE);
            mTvTaskUsageIncreasementLabel.setVisibility(View.GONE);
        } else {
            mTvTaskUsageIncreasement.setVisibility(View.VISIBLE);
            mTvTaskUsageIncreasementLabel.setVisibility(View.VISIBLE);
            mTvTaskUsageIncreasement.setText(TaskUsageUtil.getWeekInscreaseFormat(totalUsageWeek, totalUsageLastWeek));
        }
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

        Set<Integer> weekDayKeys = mTaskUsageInfosWeek.keySet();
        long weekUsage = 0;
        for (Integer weekDayKey : weekDayKeys) {
            Map<Integer, Long> usageByHour = mTaskUsageInfosWeek.get(weekDayKey);
            if (usageByHour != null) {
                Set<Integer> usageByHourKeys = usageByHour.keySet();
                for (Integer usageByHourKey : usageByHourKeys) {
                    Long usage = usageByHour.get(usageByHourKey);
                    if (usage != null) {
                        totalUsage += usage;
                        weekUsage += usage;
                    }
                }
            }

            Log.e("lk_test", "week " + weekDayKey + " usage " + weekUsage + " " + TimeUtils.getTimeHHmm(weekUsage));
        }

        long averageUsage = totalUsage / totalDay;
        mTvDailyAverageInfo.setText(getResources().getString(R.string.daily_average_regex,
                (totalUsage / TimeUtils.ONE_HOUR) + "",
                ((totalUsage % TimeUtils.ONE_HOUR) / TimeUtils.ONE_MINUTE) + ""));
    }

    private void updateTaskUsageByDay(Calendar calendar) {

        StringBuilder screenUsageBuilder = new StringBuilder();

        int firstDayOfWeek = calendar.getFirstDayOfWeek();
        for (int i = 0; i < Calendar.SATURDAY; i++) {
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

    private String getUsageDescription(int firstDayOfWeek, int index, Long useAge) {

        int currentDayOfWeek = 0;

        if (firstDayOfWeek + index <= Calendar.SATURDAY) {
            currentDayOfWeek = firstDayOfWeek + index;
        } else {
            currentDayOfWeek = firstDayOfWeek + index - Calendar.SATURDAY;
        }

        switch (currentDayOfWeek) {
            case Calendar.MONDAY:
                return "MONDAY        " + getUsageDescription(useAge);
            case Calendar.TUESDAY:
                return "TUESDAY       " + getUsageDescription(useAge);
            case Calendar.WEDNESDAY:
                return "WEDNESDAY " + getUsageDescription(useAge);
            case Calendar.THURSDAY:
                return "THURSDAY    " + getUsageDescription(useAge);
            case Calendar.FRIDAY:
                return "FRIDAY           " + getUsageDescription(useAge);
            case Calendar.SATURDAY:
                return "SATURDAY     " + getUsageDescription(useAge);
            case Calendar.SUNDAY:
                return "SUNDAY         " + getUsageDescription(useAge);
            default:
                return "";
        }
    }

    private String getUsageDescription(Long usage) {
        return usage == null ? "" : (getHour(usage) + " : " + getMinute(usage) + " : " + getSeconds(usage));
    }

    private String getSeconds(long usage) {
        long minuteMillis = usage % (60 * 60 * 1000);
        return minuteMillis % (60 * 1000) / 1000 + " S";
    }

    private String getMinute(long usage) {
        return usage % (60 * 60 * 1000) / (60 * 1000) + " M";
    }

    private String getHour(long usage) {
        return usage / (60 * 60 * 1000) + " H";
    }
}
