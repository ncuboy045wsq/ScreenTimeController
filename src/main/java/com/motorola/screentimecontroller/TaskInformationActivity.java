package com.motorola.screentimecontroller;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

import java.util.List;

import motorola.core_services.screentimecontroller.bean.ScreenBlockUpTime;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;

public class TaskInformationActivity extends FragmentActivity {

    ViewPager mVpActivityInformation;
    ActivityInformationPageAdapter mActivityInformationPageAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_activity_information);

        Button btWeekTab = findViewById(R.id.bt_weekTab);
        Button btDailyTab = findViewById(R.id.bt_dailyTab);

        btWeekTab.setBackgroundResource(R.drawable.shape_button_selected_bacground);
        btDailyTab.setBackgroundResource(R.drawable.shape_button_normal_bacground);

        btWeekTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                long blockStartTimeDelay = 30 * 1000;
                ScreenBlockUpTime screenBlockUpTime = new ScreenBlockUpTime();
                screenBlockUpTime.setStartTime(System.currentTimeMillis() + blockStartTimeDelay);
                screenBlockUpTime.setEndTime(screenBlockUpTime.getStartTime() + blockStartTimeDelay);
//                MotoExtendManager.getInstance(TaskInformationActivity.this).addScreenBlockUpTime(screenBlockUpTime);
                mVpActivityInformation.setCurrentItem(ActivityInformationFragment.TYPE.WEEK);

                btWeekTab.setBackgroundResource(R.drawable.shape_button_selected_bacground);
                btDailyTab.setBackgroundResource(R.drawable.shape_button_normal_bacground);
            }
        });

        btDailyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PackageManager pm = getPackageManager();
                List<PackageInfo> packageInfoList = pm.getInstalledPackages(0);
                PackageInfo packageInfo = null;
                for (int i = 0; packageInfoList != null && i < packageInfoList.size(); i++) {
                    packageInfo = packageInfoList.get(i);
                    if ("com.google.android.calculator".equals(packageInfo.applicationInfo.packageName)) {
                        break;
                    } else {
                        packageInfo = null;
                    }
                }
                if (packageInfo != null) {
                    TaskBlockUpInfo taskBlockUpInfo = new TaskBlockUpInfo();
                    taskBlockUpInfo.setPackageName(packageInfo.packageName);
                    taskBlockUpInfo.setUid(packageInfo.applicationInfo.uid + "");
                    taskBlockUpInfo.setMaxUsage(30 * 1000);
                    taskBlockUpInfo.setBlockType(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE);
                    // MotoExtendManager.getInstance(TaskInformationActivity.this).addTaskBlockUpInfo(taskBlockUpInfo);
                }

                mVpActivityInformation.setCurrentItem(ActivityInformationFragment.TYPE.DAILY);
                btWeekTab.setBackgroundResource(R.drawable.shape_button_normal_bacground);
                btDailyTab.setBackgroundResource(R.drawable.shape_button_selected_bacground);
            }
        });

        mActivityInformationPageAdapter = new ActivityInformationPageAdapter(getSupportFragmentManager());
        mVpActivityInformation = findViewById(R.id.vp_activityInformation);
        mVpActivityInformation.setAdapter(mActivityInformationPageAdapter);
    }

    private class ActivityInformationPageAdapter extends FragmentPagerAdapter {

        public ActivityInformationPageAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int i) {
            ActivityInformationFragment activityInformationFragment = new ActivityInformationFragment();
            Bundle bundle = new Bundle();
            if (i == 0) {
                bundle.putInt(ActivityInformationFragment.KEY_TYPE, ActivityInformationFragment.TYPE.WEEK);
            } else {
                bundle.putInt(ActivityInformationFragment.KEY_TYPE, ActivityInformationFragment.TYPE.DAILY);
            }
            activityInformationFragment.setArguments(bundle);
            return activityInformationFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
