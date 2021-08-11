package com.motorola.screentimecontroller;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import motorola.core_services.screentimecontroller.IScreenTimeInfoAidl;
import motorola.core_services.screentimecontroller.bean.TaskUsageInfo;

public class MainActivity extends Activity {
    private ScreenTimeControllerModel mScreenTimeControllerModel;
    private ScreenTimeControllerService.ScreenTimeControllerBinder screenTimeControllerBinder;

    /**
     * 显示每天使用时长
     */
    private TextView mTvScreenUsageByDay;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_main);

        mScreenTimeControllerModel = new ScreenTimeControllerModel(this);
        mScreenTimeControllerModel.queryTaskUsageInfo(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object object) {

                if (object == null) {
                    return;
                }

                Calendar calendar = Calendar.getInstance();
                if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - (calendar.get(Calendar.DAY_OF_WEEK) + 7));
                }

                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long oneDayMillis = 24 * 60 * 60 * 1000;
                long firstMillisOfWeek = calendar.getTimeInMillis();
                long lastMillisOfWeek = firstMillisOfWeek + oneDayMillis * 7;

                List<TaskUsageInfo> taskUsageInfoList = (List<TaskUsageInfo>) object;
                Map<Integer, Long> screenTimeUsage = new HashMap<>();
                for (int i = 0; i < taskUsageInfoList.size(); i++) {
                    if (firstMillisOfWeek <= taskUsageInfoList.get(i).getStartTime() && taskUsageInfoList.get(i).getStartTime() < lastMillisOfWeek) {
                        long startTimeInDay = (taskUsageInfoList.get(i).getStartTime() - firstMillisOfWeek) % oneDayMillis;
                        int currentDayOfWeek = (int) ((taskUsageInfoList.get(i).getStartTime() - firstMillisOfWeek) / oneDayMillis);

                        Long totalUsage = screenTimeUsage.get(currentDayOfWeek);
                        if (totalUsage == null) {
                            totalUsage = 0l;
                        }

                        long taskDuration = taskUsageInfoList.get(i).getDuration();

                        if (startTimeInDay + taskDuration <= oneDayMillis) {
                            // 日内任务
                            totalUsage += taskDuration;
                            screenTimeUsage.put(currentDayOfWeek, totalUsage);
                        } else {
                            while (taskDuration > 0 && currentDayOfWeek < Calendar.SATURDAY) {
                                totalUsage += (oneDayMillis - startTimeInDay);
                                screenTimeUsage.put(currentDayOfWeek, totalUsage);
                                taskDuration -= (oneDayMillis - startTimeInDay);
                                currentDayOfWeek++;
                                totalUsage = screenTimeUsage.get(currentDayOfWeek);
                            }
                        }

                    }
                }

                Iterator<Map.Entry<Integer, Long>> iterable = screenTimeUsage.entrySet().iterator();

                StringBuilder screenUsageBuilder = new StringBuilder();

                int firstDayOfWeek = calendar.getFirstDayOfWeek();
                for (int i = 1; i <= Calendar.SATURDAY; i++) {
                    Long useAge = screenTimeUsage.get(i);
                    screenUsageBuilder.append(getUsageDescription(firstDayOfWeek, i, useAge));
                    screenUsageBuilder.append("\n");
                }

                mTvScreenUsageByDay.setText(screenUsageBuilder);
            }
        });

        TextView tvFloatValueChangeLabel = findViewById(R.id.tv_floatValueChangeLabel);
        Drawable drawable = getResources().getDrawable(R.drawable.ic_launcher_background);
        drawable.setBounds(0, 0, 15, 15);
        tvFloatValueChangeLabel.setCompoundDrawables(drawable, null, null, null);

        mTvScreenUsageByDay = findViewById(R.id.tv_screenUsageByDay);

        TextView tvDailyAverageInfo = findViewById(R.id.tv_dailyAverageInfo);
        tvDailyAverageInfo.setText(getResources().getString(R.string.daily_average_regex, "13", "13"));

        LinearLayout llViewAllActivities = findViewById(R.id.ll_viewAllActivities);
        llViewAllActivities.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, TaskInformationActivity.class);
                startActivity(intent);
            }
        });

        Button btStopTime = findViewById(R.id.bt_stopTime);
        btStopTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置停用时间
                Intent intent = new Intent(MainActivity.this, BlockUpTimeActivity.class);
                startActivity(intent);
            }
        });

        Button btAppTimeLimit = findViewById(R.id.bt_appTimeLimit);
        btAppTimeLimit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置 app 最大使用时长
                Intent intent = new Intent();

                intent.setPackage("com.motorola.screentimecontroller");
                intent.setAction("com.motorola.screentimecontroller.ScreenTimeControllerService");

//                intent.setComponent(new ComponentName("com.motorola.screentimecontroller", "com.motorola.screentimecontroller.ScreenTimeControllerService1"));

//                intent.setClass(MainActivity.this, ScreenTimeControllerService.class); // ok

                ServiceConnection serviceConnection = new ServiceConnection() {
                    @Override
                    public void onServiceConnected(ComponentName name, IBinder service) {
                        Log.d("lk_test", MainActivity.this.getClass().getSimpleName() + ".onServiceConnected: run... ");
                        IScreenTimeInfoAidl screenTimeInfoAidl = IScreenTimeInfoAidl.Stub.asInterface(service);
                        Log.d("lk_test", MainActivity.this.getClass().getSimpleName() + ".onServiceConnected: convert... name " + name);
                    }

                    @Override
                    public void onServiceDisconnected(ComponentName name) {
                        Log.d("lk_test", MainActivity.this.getClass().getSimpleName() + ".onServiceDisconnected: run...");
                    }

                    @Override
                    public void onBindingDied(ComponentName name) {
                        Log.d("lk_test", MainActivity.this.getClass().getSimpleName() + ".onBindingDied: run...");
                    }

                    @Override
                    public void onNullBinding(ComponentName name) {
                        Log.d("lk_test", MainActivity.this.getClass().getSimpleName() + ".onNullBinding: run...");
                    }
                };

                bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE);
            }
        });

        Button btAlwayAllows = findViewById(R.id.bt_alwayAllows);
        btAlwayAllows.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 设置始终允许
            }
        });
    }

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
