package com.motorola.screentimecontroller;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.view.View;
import android.widget.Button;

public class TaskUsageInfoActivity extends FragmentActivity {

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
                mVpActivityInformation.setCurrentItem(TaskUsageInfoWeekFragment.TYPE.WEEK);
                btWeekTab.setBackgroundResource(R.drawable.shape_button_selected_bacground);
                btDailyTab.setBackgroundResource(R.drawable.shape_button_normal_bacground);
            }
        });

        btDailyTab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mVpActivityInformation.setCurrentItem(TaskUsageInfoWeekFragment.TYPE.DAILY);
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
            TaskUsageInfoWeekFragment taskUsageInfoFragment = new TaskUsageInfoWeekFragment();
            Bundle bundle = new Bundle();
            if (i == 0) {
                bundle.putInt(TaskUsageInfoWeekFragment.KEY_TYPE, TaskUsageInfoWeekFragment.TYPE.WEEK);
            } else {
                bundle.putInt(TaskUsageInfoWeekFragment.KEY_TYPE, TaskUsageInfoWeekFragment.TYPE.DAILY);
            }
            taskUsageInfoFragment.setArguments(bundle);
            return taskUsageInfoFragment;
        }

        @Override
        public int getCount() {
            return 2;
        }
    }
}
