package com.motorola.screentimecontroller;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;

import com.motorola.android.provider.MotorolaSettings;
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

    private Button mBtnToggleAppScreenControl;

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
        
        findViewById(R.id.bt_stopList).setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, BlockUpTimeListActivity.class);
            startActivity(intent);
        });

        mBtnToggleAppScreenControl = (Button) findViewById(R.id.btn_toggle_switch);
        mBtnToggleAppScreenControl.setText(getDisplayTextOfSwitch());
        mBtnToggleAppScreenControl.setOnClickListener(v -> {
            int value = MotorolaSettings.System.getInt(getContentResolver(), MotorolaSettings.System.APP_SCREEN_CONTROL, 0);
            int newValue = value == 1 ? 0 : 1;
            boolean result = MotorolaSettings.System.putInt(getContentResolver(), MotorolaSettings.System.APP_SCREEN_CONTROL, newValue);
            if (result) {
                mBtnToggleAppScreenControl.setText(getDisplayTextOfSwitch());
            }
        });


        loadTaskUsageInfo();
    }

    private String getDisplayTextOfSwitch() {
        int value = MotorolaSettings.System.getInt(getContentResolver(), MotorolaSettings.System.APP_SCREEN_CONTROL, 0);
        return value == 1 ? "已打开" : "已关闭";
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
        long averageUsage = TimeUtils.getDailyUsage(calendar, mTaskUsageInfosWeek);
        mTvDailyAverageInfo.setText(getResources().getString(R.string.daily_average_regex,
                (averageUsage / TimeUtils.ONE_HOUR) + "",
                ((averageUsage % TimeUtils.ONE_HOUR) / TimeUtils.ONE_MINUTE) + ""));
    }


    private void updateTaskUsageByDay(Calendar calendar) {

        String screenUsageStr = TimeUtils.getTaskUsageByDayDesc(calendar, mTaskUsageInfosWeek);

        mTvScreenUsageByDay.setText(screenUsageStr);
    }

}
