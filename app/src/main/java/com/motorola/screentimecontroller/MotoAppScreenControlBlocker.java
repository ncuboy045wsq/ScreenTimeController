package com.motorola.screentimecontroller;


import android.app.Activity;
import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.hardware.biometrics.BiometricPrompt;
import android.os.Build;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.RequiresApi;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;


public class MotoAppScreenControlBlocker extends Activity {
    private static final boolean DEBUG = true;
    private static final String TAG = "MotoAppScreenBlocker";
    private static final int REQUEST_CODE_KEYGUARD = 1;
    private Handler mHandler;

    private EditText mEtInput;
    private TaskBlockUpInfo mBlockUpInfo = null;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_blocker);
        final CharSequence appName = getIntent().getCharSequenceExtra("screen_control_app");
        mBlockUpInfo = getIntent().getParcelableExtra("screen_control_policy");

        TextView textView = (TextView) findViewById(R.id.tv_blocker_display);
        Button button = (Button) findViewById(R.id.btn_blocker_addtime);
        mHandler = new Handler(getMainLooper());

        mEtInput = (EditText) findViewById(R.id.et_blocker_input);

        textView.setText(appName + " has been out of time, please add more time");

        button.setOnClickListener(v -> {
            if (DEBUG) {
                Log.d(TAG, "showKeyguardCredentialOfMainUser@1");
            }
            try {
                String time = mEtInput.getText().toString();
                int timeInt = Integer.parseInt(time);

                if (timeInt >= 24 * 60) {
                    Toast.makeText(this, "can't more than 24 hours", Toast.LENGTH_SHORT).show();
                    return;
                }
            } catch (NumberFormatException e) {
                Toast.makeText(this, "add fail", Toast.LENGTH_SHORT).show();
            }
            if (MotoExtendManager.getInstance(this).isMainUser(this)) {
                addMaxTime();
            } else {
                MotoExtendManager.getInstance(this).showKeyguardCredentialOfMainUser(this, "I am title", mHandler, () -> {
                    if (DEBUG) {
                        addMaxTime();
                    }
                }, () -> {
                    Toast.makeText(this, "验证失败", Toast.LENGTH_SHORT).show();
                });
            }
        });

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_DATE_CHANGED);
        registerReceiver(mReceiver, filter);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_KEYGUARD && resultCode == RESULT_OK) {
            if (DEBUG) {
                Log.d(TAG, "onActivityResult");
            }
            addMaxTime();
        }
    }

    private void addMaxTime() {
        try {
            String time = mEtInput.getText().toString();
            int timeInt = Integer.parseInt(time);

            if (timeInt >= 24 * 60) {
                Toast.makeText(this, "can't more than 24 hours", Toast.LENGTH_SHORT).show();
                return;
            }
            mBlockUpInfo.setMaxUsage(mBlockUpInfo.getMaxUsage() + timeInt * 60 * 1000);
            MotoExtendManager.getInstance(this).updateTaskBlockUpInfo(mBlockUpInfo);
            finish();
        } catch (NumberFormatException e) {
            Toast.makeText(this, "add fail", Toast.LENGTH_SHORT).show();
        }
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
