package com.motorola.screentimecontroller;

import android.app.Service;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.IBinder;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.widget.Toast;

import com.motorola.screentimecontroller.database.dao.TaskUsageInfoDao;

import java.util.ArrayList;
import java.util.List;

import motorola.core_services.screentimecontroller.IScreenTimeInfoAidl;
import motorola.core_services.screentimecontroller.bean.TaskUsageInfo;

public class ScreenTimeControllerService extends Service {

    private ScreenTimeControllerBinder screenTimeControllerBinder = new ScreenTimeControllerBinder();
    private TaskUsageInfoDao mTaskUsageInfoDao;

    public class ScreenTimeControllerBinder extends IScreenTimeInfoAidl.Stub {

        @Override
        public long onPageFinish(String packageName, String uid, long startTime, long duration) throws RemoteException {
            TaskUsageInfo taskUsageInfo = new TaskUsageInfo();
            taskUsageInfo.setPackageName(packageName);
            taskUsageInfo.setUid(uid);
            taskUsageInfo.setStartTime(startTime);
            taskUsageInfo.setDuration(duration);
            Uri uri = mTaskUsageInfoDao.insert(taskUsageInfo);

            int insertCount = 0;
            if (uri != null) {
                try {
                    insertCount = Integer.parseInt(uri.getLastPathSegment());
                } catch (Exception e) {
                    insertCount = 0;
                }
            }

            Toast.makeText(ScreenTimeControllerService.this, "Insert " + taskUsageInfo.getInfo() + " count " + insertCount, Toast.LENGTH_LONG).show();

            return 0;
        }

        @Override
        public List<Bundle> getTaskUsageInfo() throws RemoteException {
            try {
                Toast.makeText(ScreenTimeControllerService.this, "ScreenTimeControllerService.getTaskUsageInfo:: run...", Toast.LENGTH_SHORT).show();
            } catch (Exception e) {
                e.printStackTrace();
            }
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
    public int onStartCommand(Intent intent, int flags, int startId) {
        mTaskUsageInfoDao = new TaskUsageInfoDao(getContentResolver());
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}