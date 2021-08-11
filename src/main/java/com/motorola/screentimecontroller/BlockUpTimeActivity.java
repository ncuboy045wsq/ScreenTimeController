package com.motorola.screentimecontroller;

import android.app.Activity;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.TimePicker;
import android.widget.Toast;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.ScreenBlockUpTime;

//import motorola.core_services.screentimecontroller.MotoExtendManager;

public class BlockUpTimeActivity extends Activity {

    TimePickerFragment mTimePickerFragment;
    private long mStartTime = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_block_up_time);

        findViewById(R.id.bt_setBlockUpTime).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showTimePicker();
                Toast.makeText(BlockUpTimeActivity.this, "请输入起始时间", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void showTimePicker() {

        Fragment timePickerFragment = getFragmentManager().findFragmentByTag("pick_time");
        if (timePickerFragment != null) {
            getFragmentManager().beginTransaction().remove(timePickerFragment).commitAllowingStateLoss();
        }

        mTimePickerFragment = new TimePickerFragment();
        mTimePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {

                // set the time
                if (mStartTime == 0) {
                    mStartTime = TimePickerFragment.getTimeInMillis(hourOfDay, minute);
                    showTimePicker();
                    Toast.makeText(BlockUpTimeActivity.this, "请输入结束时间", Toast.LENGTH_SHORT).show();
                } else {
                    ScreenBlockUpTime screenBlockUpTime = new ScreenBlockUpTime();
                    long endTime = TimePickerFragment.getTimeInMillis(hourOfDay, minute);
                    if (mStartTime > endTime) {
                        long tmp = mStartTime;
                        mStartTime = endTime;
                        endTime = tmp;
                    }
                    screenBlockUpTime.setStartTime(mStartTime);
                    screenBlockUpTime.setEndTime(endTime);
                    mStartTime = 0;
                    try {

                        long addCount = MotoExtendManager.getInstance(BlockUpTimeActivity.this).addScreenBlockUpTime(screenBlockUpTime);

                        if (addCount > 0) {
                            Toast.makeText(BlockUpTimeActivity.this, "Set success " + addCount, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(BlockUpTimeActivity.this, "Set Failed " + addCount, Toast.LENGTH_SHORT).show();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(BlockUpTimeActivity.this, "Set Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            }
        });
        mTimePickerFragment.show(getFragmentManager(), "time_pickerer");
    }

}