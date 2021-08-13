package com.motorola.screentimecontroller;


import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;


public class MotoAppScreenControlBlocker extends Activity {
    private static final boolean DEBUG = true;
    private static final String TAG = "MotoAppScreenBlocker";


    public static final String KEY_LIMITED_APP = "key_limited_app";
    public static final String KEY_LIMITED_BLOCK_INFO = "key_limited_blockinfo";
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocker);
        final CharSequence appName = getIntent().getCharSequenceExtra("screen_control_app");
        final TaskBlockUpInfo blockUpInfo = getIntent().getParcelableExtra("screen_control_policy");

        TextView textView = (TextView) findViewById(R.id.tv_blocker_display);
        Button button = (Button) findViewById(R.id.btn_blocker_addtime);

        final EditText input = (EditText) findViewById(R.id.et_blocker_input);

        textView.setText(appName + " has been out of time, please add more time");

        button.setOnClickListener(v -> {
            try {
                String time = input.getText().toString();
                int timeInt = Integer.parseInt(time);

                if (timeInt >= 24 * 60) {
                    Toast.makeText(this, "can't more than 24 hours", Toast.LENGTH_SHORT).show();
                    return;
                }
                blockUpInfo.setMaxUsage(blockUpInfo.getMaxUsage() + timeInt * 60 * 1000);
                MotoExtendManager.getInstance(this).updateTaskBlockUpInfo(blockUpInfo);
                finish();
            } catch (NumberFormatException e) {
                Toast.makeText(this, "add fail", Toast.LENGTH_SHORT).show();
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    private BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (Intent.ACTION_DATE_CHANGED.equals(intent.getAction())) {
                if (DEBUG) {
                    Log.d(TAG, "blocker finish itself due to date changed");
                }
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(mReceiver);
    }

    @Override
    public void onBackPressed() {
    }
}
