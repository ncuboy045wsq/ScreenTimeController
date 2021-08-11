package com.motorola.screentimecontroller.model;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;

import com.motorola.screentimecontroller.database.dao.TaskUsageInfoDao;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

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

    public interface OnResult<T> {
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

}