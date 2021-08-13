package com.motorola.screentimecontroller;

import android.app.Activity;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import com.motorola.screentimecontroller.bean.TaskInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.ScreenBlockUpTime;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;

//import motorola.core_services.screentimecontroller.MotoExtendManager;

public class BlockUpTimeSettingActivity extends Activity {

    private long mStartTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_block_up_time);

        findViewById(R.id.bt_setBlockUpTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker("pick_time");
                Toast.makeText(BlockUpTimeSettingActivity.this, "请输入起始时间", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePicker(String tag) {

        Fragment timePickerFragmentTag = getFragmentManager().findFragmentByTag(tag);
        if (timePickerFragmentTag != null) {
            getFragmentManager().beginTransaction().remove(timePickerFragmentTag).commitAllowingStateLoss();
        }

        TimePickerFragment timePickerFragment = new TimePickerFragment();
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                // set the time
                if (mStartTime == 0) {
                    mStartTime = TimePickerFragment.getTimeInMillis(hourOfDay, minute);
                    showTimePicker("time_picker");
                    Toast.makeText(BlockUpTimeSettingActivity.this, "请输入结束时间", Toast.LENGTH_SHORT).show();
                } else {
                    ScreenBlockUpTime screenBlockUpTime = new ScreenBlockUpTime();
                    long endTime = TimePickerFragment.getTimeInMillis(hourOfDay, minute);
                    if (mStartTime > endTime) {
                        mStartTime = 0;
                        Toast.makeText(BlockUpTimeSettingActivity.this, "起始时间不能小于结束时间", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    screenBlockUpTime.setStartTime(mStartTime);
                    screenBlockUpTime.setEndTime(endTime);
                    mStartTime = 0;
                    try {
                        long addCount = MotoExtendManager.getInstance(BlockUpTimeSettingActivity.this).addScreenBlockUpTime(screenBlockUpTime);

                        if (addCount > 0) {
                            Toast.makeText(BlockUpTimeSettingActivity.this, "Set success " + addCount, Toast.LENGTH_SHORT).show();
                        } else if (addCount == -1001) {
                            Toast.makeText(BlockUpTimeSettingActivity.this, "Set Failed: Time is exist.", Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BlockUpTimeSettingActivity.this, "Set Failed: unexpected error." + addCount, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        Toast.makeText(BlockUpTimeSettingActivity.this, "Set Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        timePickerFragment.show(getFragmentManager(), "time_pickerer");
    }

}