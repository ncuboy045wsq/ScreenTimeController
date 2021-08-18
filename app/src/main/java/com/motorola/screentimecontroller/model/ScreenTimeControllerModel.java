package com.motorola.screentimecontroller.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.telecom.Call;

import androidx.annotation.NonNull;

import com.motorola.screentimecontroller.database.dao.TaskUsageInfoDao;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import motorola.core_services.screentimecontroller.TimeUtil;
import motorola.core_services.screentimecontroller.bean.TaskUsageInfo;

public class ScreenTimeControllerModel {

    private static final int ON_INSERT = 0;
    private static final int ON_QUERY = 1;
    private static final int ON_QUERY_ALL = 2;
    private static final int ON_UPDATE = 3;
    private static final int ON_DELETE = 4;

    private final ExecutorService mExecutor;
    private final TaskUsageInfoDao mTaskUsageInfoDao;

    private static class Result {

        private OnResult onResult;
        private Object mObj;

        public Result(OnResult callback, Object object) {
            this.onResult = callback;
            this.mObj = object;
        }
    }

    public interface OnResult {
        public void onResult(Object obj);
    }

    private Handler mHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            Result result = (Result) msg.obj;
            if (result != null && result.onResult != null) {
                result.onResult.onResult(result.mObj);
            }
        }
    };

    public ScreenTimeControllerModel(Context context) {
        this.mExecutor = Executors.newCachedThreadPool();
        this.mTaskUsageInfoDao = new TaskUsageInfoDao(context.getContentResolver());
    }

    private void sendMessage(int what, Result result) {
        Message msg = mHandler.obtainMessage();
        msg.what = what;
        msg.obj = result;
        msg.sendToTarget();
    }

    public void addTaskUsageInfo(OnResult onResult, TaskUsageInfo taskUsageInfo) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_INSERT, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.insert(taskUsageInfo)));
            }
        }, taskUsageInfo));
    }

    public void queryTaskUsageInfo(OnResult onResult, TaskUsageInfo taskUsageInfo) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_QUERY, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.query(taskUsageInfo)));
            }
        }, taskUsageInfo));
    }

    public void queryTaskUsageInfo(OnResult onResult) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_QUERY_ALL, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.query()));
            }
        }, null));
    }

    public void queryTaskUsageInfo(OnResult onResult, long startTime, long endTime) {
        queryTaskUsageInfo(onResult, Calendar.getInstance().getFirstDayOfWeek(), startTime, endTime);
    }

    public void queryTaskUsageInfo(OnResult onResult, String pkgName, int uid) {

        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.HOUR, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);

        long firstMillisOfDay = calendar.getTimeInMillis();

        queryTaskUsageInfo(onResult, pkgName, uid, firstMillisOfDay, firstMillisOfDay + TimeUtils.ONE_DAY);
    }

    public void queryTaskUsageInfo(OnResult onResult, String pkgName, int uid, long startTime, long endTime) {

        List<TaskUsageInfo> taskUsageInfoList = ScreenTimeControllerModel.this.mTaskUsageInfoDao.query(pkgName, uid, startTime, endTime);

        if (taskUsageInfoList == null) {
            sendMessage(ON_QUERY_ALL, new Result(onResult, 0));
            return;
        }

        long totalTime = 0;
        for (TaskUsageInfo taskUsageInfo : taskUsageInfoList) {
            totalTime += taskUsageInfo.getDuration();
            if (taskUsageInfo.getStartTime() < startTime) {
                totalTime -= (startTime - taskUsageInfo.getStartTime());
            }
            if (taskUsageInfo.getEndTime() > endTime) {
                totalTime -= (taskUsageInfo.getEndTime() - endTime);
            }
        }

        sendMessage(ON_QUERY_ALL, new Result(onResult, totalTime));
    }

    /**
     * @param onResult       结果回调, 将会获取到 Map<Integer, Map<Integer, Long>>
     * @param firstDayOfWeek 日期设置
     * @param startTime      起始时间
     * @param endTime        结束时间
     */
    public void queryTaskUsageInfo(OnResult onResult, int firstDayOfWeek, long startTime, long endTime) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {

                Calendar calendar = Calendar.getInstance();
                calendar.setFirstDayOfWeek(firstDayOfWeek);

                if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
                    calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek()));
                } else {
                    calendar.add(Calendar.DAY_OF_MONTH, -(calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() + Calendar.SATURDAY));
                }

                calendar.set(Calendar.HOUR_OF_DAY, 0);
                calendar.set(Calendar.MINUTE, 0);
                calendar.set(Calendar.MILLISECOND, 0);

                long firstMillisOfWeek = calendar.getTimeInMillis();
                long lastMillisOfWeek = firstMillisOfWeek + TimeUtils.ONE_DAY * 7;

                List<TaskUsageInfo> taskUsageInfoList = ScreenTimeControllerModel.this.mTaskUsageInfoDao.query(startTime, endTime);
                Map<Integer, Map<Integer, Long>> screenTimeUsage = new HashMap<>();
                for (int i = 0; i < taskUsageInfoList.size(); i++) {

                    long taskStartTime = taskUsageInfoList.get(i).getStartTime();
                    long taskDuration = taskUsageInfoList.get(i).getEndTime() - taskUsageInfoList.get(i).getStartTime();

                    if (taskStartTime < firstMillisOfWeek) {
                        taskStartTime = firstMillisOfWeek;
                        taskDuration -= (firstMillisOfWeek - taskStartTime);
                    }

                    if (taskStartTime + taskDuration >= lastMillisOfWeek) {
                        taskDuration -= (taskStartTime + taskDuration - lastMillisOfWeek);
                    }

                    long startTimeInDay = (taskStartTime - firstMillisOfWeek) % TimeUtils.ONE_DAY;
                    int currentDayOfWeek = (int) ((taskStartTime - firstMillisOfWeek) / TimeUtils.ONE_DAY);

                    Map<Integer, Long> totalUsageByHour = screenTimeUsage.get(currentDayOfWeek);
                    if (totalUsageByHour == null) {
                        totalUsageByHour = new HashMap<>();
                        screenTimeUsage.put(currentDayOfWeek, totalUsageByHour);
                    }

                    if (startTimeInDay + taskDuration <= TimeUtils.ONE_DAY) {
                        // 日内任务
                        addUsageByHour(startTimeInDay, taskDuration, totalUsageByHour);
                        taskDuration = 0;
                    } else {
                        // 跨日任务
                        addUsageByHour(startTimeInDay, TimeUtils.ONE_DAY - startTimeInDay, totalUsageByHour);
                        taskDuration -= (TimeUtils.ONE_DAY - startTimeInDay);
                        currentDayOfWeek++;
                        while (currentDayOfWeek < Calendar.SATURDAY && taskDuration > 0) {
                            startTimeInDay = 0;
                            totalUsageByHour = screenTimeUsage.get(currentDayOfWeek);
                            if (totalUsageByHour == null) {
                                totalUsageByHour = new HashMap<>();
                                screenTimeUsage.put(currentDayOfWeek, totalUsageByHour);
                            }

                            if (taskDuration <= TimeUtils.ONE_DAY) {
                                addUsageByHour(startTimeInDay, taskDuration, totalUsageByHour);
                                taskDuration = 0;
                            } else {
                                addUsageByHour(startTimeInDay, TimeUtils.ONE_DAY, totalUsageByHour);
                                taskDuration -= (TimeUtils.ONE_DAY - startTimeInDay);
                                currentDayOfWeek++;
                            }
                        }
                    }

                }

                sendMessage(ON_QUERY_ALL, new Result(onResult, screenTimeUsage));
            }
        }, null));
    }

    private void addUsageByHour(long startTimeInDay, long taskDuration, Map<Integer, Long> totalUsageByHour) {

        long oneHourMillis = 60 * 60 * 1000;

        int hourStart = (int) (startTimeInDay / oneHourMillis);

        while (hourStart < 24 && taskDuration > 0) {
            Long currentHourUsage = totalUsageByHour.get(hourStart);
            if (currentHourUsage == null) {
                currentHourUsage = 0l;
            }

            if (currentHourUsage + taskDuration <= oneHourMillis) {
                // 小时内任务
                currentHourUsage += taskDuration;
                taskDuration = 0;
                totalUsageByHour.put(hourStart, currentHourUsage);
            } else {
                taskDuration -= oneHourMillis - currentHourUsage;
                currentHourUsage += oneHourMillis - currentHourUsage;
                totalUsageByHour.put(hourStart, currentHourUsage);
                hourStart++;
            }


        }
        return;
    }

    /**
     * @param onResult
     */
    public void queryTaskUsageInfoByDaily(OnResult onResult) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_QUERY_ALL, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.query()));
            }
        }, null));
    }

    public void update(OnResult onResult, TaskUsageInfo taskUsageInfo) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_UPDATE, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.update(taskUsageInfo)));
            }
        }, null));
    }

    public void delete(OnResult onResult, TaskUsageInfo taskUsageInfo) {
        this.mExecutor.execute(new FutureTask<TaskUsageInfo>(new Runnable() {
            @Override
            public void run() {
                sendMessage(ON_DELETE, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.delete(taskUsageInfo)));
            }
        }, null));
    }

    public void queryTaskUsageInfoUpdateTime(OnResult onResult) {
        this.mExecutor.execute(new FutureTask<Long>(new Callable<Long>() {
            @Override
            public Long call() throws Exception {
                sendMessage(ON_DELETE, new Result(onResult, ScreenTimeControllerModel.this.mTaskUsageInfoDao.queryTaskUsageInfoUpdateTime()));
                return 1l;
            }
        }));
    }

}