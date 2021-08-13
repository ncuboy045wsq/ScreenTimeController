package com.motorola.screentimecontroller;

import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentPagerAdapter;
import androidx.viewpager.widget.ViewPager;

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
            Fragment taskUsageInfoFragment = null;
            Bundle bundle = new Bundle();
            if (i == 0) {
                taskUsageInfoFragment = new TaskUsageInfoWeekFragment();
                bundle.putInt(TaskUsageInfoWeekFragment.KEY_TYPE, TaskUsageInfoWeekFragment.TYPE.WEEK);
            } else {
                taskUsageInfoFragment = new TaskUsageInfoDailyFragment();
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
