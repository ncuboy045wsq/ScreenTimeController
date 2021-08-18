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
import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.ArrayList;
import java.util.Calendar;
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
            TaskUsageInfo taskUsageInfo = new TaskUsageInfo();
            taskUsageInfo.setPackageName(packageName);
            taskUsageInfo.setUid(Integer.parseInt(uid));
            taskUsageInfo.setStartTime(startTime);
            taskUsageInfo.setEndTime(startTime + duration);
            Uri uri = null;

            try {
                uri = mTaskUsageInfoDao.insert(taskUsageInfo);
            } catch (Exception e) {
                e.printStackTrace();
            }

            int insertCount = 0;
            if (uri != null) {
                try {
                    insertCount = Integer.parseInt(uri.getLastPathSegment());
                } catch (Exception e) {
                    insertCount = 0;
                }
            }

            return insertCount;
        }

        @Override
        public List<Bundle> getTaskUsageInfo() throws RemoteException {

            // 获取当天所有 task 使用的时长信息
            List<TaskUsageInfo> taskUsageInfoList = mTaskUsageInfoDao.query();
            List<Bundle> taskUsageInfoBundleList = new ArrayList<>();
            for (int i = 0; taskUsageInfoList != null && i < taskUsageInfoList.size(); i++) {
                taskUsageInfoBundleList.add(taskUsageInfoList.get(i).toBundle());
            }
            return taskUsageInfoBundleList;
        }

        @Override
        public long getTaskUsageToday(String pkgName, int uid) throws RemoteException {

            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.HOUR, 0);
            calendar.set(Calendar.MINUTE, 0);
            calendar.set(Calendar.SECOND, 0);
            calendar.set(Calendar.MILLISECOND, 0);

            long firstMillisOfDay = calendar.getTimeInMillis();

            List<TaskUsageInfo> taskUsageInfoList = mTaskUsageInfoDao.query(pkgName, uid, firstMillisOfDay, firstMillisOfDay + TimeUtils.ONE_DAY);

            if (taskUsageInfoList == null) {
                return 0;
            }

            long totalTime = 0;
            for (TaskUsageInfo taskUsageInfo : taskUsageInfoList) {
                totalTime += taskUsageInfo.getDuration();
                if (taskUsageInfo.getStartTime() < firstMillisOfDay) {
                    totalTime -= (firstMillisOfDay - taskUsageInfo.getStartTime());
                }
                if (taskUsageInfo.getEndTime() > firstMillisOfDay + TimeUtils.ONE_DAY) {
                    totalTime -= (taskUsageInfo.getEndTime() - (firstMillisOfDay + TimeUtils.ONE_DAY));
                }
            }

            return totalTime;
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
        mTaskUsageInfoDao = new TaskUsageInfoDao(getContentResolver());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }


    @Override
    public void onDestroy() {
        super.onDestroy();
    }
}