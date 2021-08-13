package com.motorola.screentimecontroller;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import androidx.annotation.Nullable;

import com.motorola.screentimecontroller.database.dao.TaskUsageInfoDao;

import java.util.ArrayList;
import java.util.List;

import motorola.core_services.screentimecontroller.IScreenTimeInfoAidl;
import motorola.core_services.screentimecontroller.bean.TaskUsageInfo;

public class ScreenTimeControllerService extends Service {

    private ScreenTimeControllerBinder screenTimeControllerBinder = new ScreenTimeControllerBinder();
    private TaskUsageInfoDao mTaskUsageInfoDao;

    public ScreenTimeControllerService() {
    }

    public class ScreenTimeControllerBinder extends IScreenTimeInfoAidl.Stub {

        @Override
        public long onPageFinish(String packageName, String uid, long startTime, long duration) throws RemoteException {
            Log.e("lk_test", getClass().getSimpleName() + ".onPageFinish: 001v1 packageName " + packageName + " uid " + uid + " startTime " + startTime + " duration " + duration);
            TaskUsageInfo taskUsageInfo = new TaskUsageInfo();
            taskUsageInfo.setPackageName(packageName);
            taskUsageInfo.setUid(Integer.parseInt(uid));
            taskUsageInfo.setStartTime(startTime);
            taskUsageInfo.setEndTime(startTime + duration);
            Uri uri = null;

            try {
                uri = mTaskUsageInfoDao.insert(taskUsageInfo);
            } catch (Exception e) {
                Log.e("lk_test", getClass().getSimpleName() + ".onPageFinish: mTaskUsageInfoDao " + mTaskUsageInfoDao + "  002 " + e.getMessage());
            }

            int insertCount = 0;
            if (uri != null) {
                try {
                    insertCount = Integer.parseInt(uri.getLastPathSegment());
                } catch (Exception e) {
                    Log.e("lk_test", getClass().getSimpleName() + ".onPageFinish: 003 " + e.getMessage());
                    insertCount = 0;
                }
            }

//            Toast.makeText(ScreenTimeControllerService.this, "Insert " + taskUsageInfo.getInfo() + " count " + insertCount, Toast.LENGTH_SHORT).show();

            Log.e("lk_test", getClass().getSimpleName() + ".onPageFinish: 004 packageName " + packageName + " uid " + uid + " startTime " + startTime + " duration " + duration + " insertCount " + insertCount);

            return insertCount;
        }

        @Override
        public List<Bundle> getTaskUsageInfo() throws RemoteException {

            Log.e("lk_test", ScreenTimeControllerService.this.getClass().getSimpleName() + ".ScreenTimeControllerService.getTaskUsageInfo:: run...");

            // 获取当天所有 task 使用的时长信息
            List<TaskUsageInfo> taskUsageInfoList = mTaskUsageInfoDao.query();
            List<Bundle> taskUsageInfoBundleList = new ArrayList<>();
            for (int i = 0; taskUsageInfoList != null && i < taskUsageInfoList.size(); i++) {
                taskUsageInfoBundleList.add(taskUsageInfoList.get(i).toBundle());
            }
            return taskUsageInfoBundleList;
        }
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return screenTimeControllerBinder;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        Log.e("lk_test", getClass().getSimpleName() + ".onCreate run");
        mTaskUsageInfoDao = new TaskUsageInfoDao(getContentResolver());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.e("lk_test", getClass().getSimpleName() + ".onStartCommand run");
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}