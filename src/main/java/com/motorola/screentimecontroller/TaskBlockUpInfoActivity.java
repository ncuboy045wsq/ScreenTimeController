package com.motorola.screentimecontroller;

import android.app.Activity;
import android.app.TimePickerDialog;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;

public class TaskBlockUpInfoActivity extends Activity {

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private ListView mLlViewAllTasks;
    private AvailableTaskAdapter mAvailableTaskAdapter;

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                mAvailableTaskAdapter.setData((List<TaskInfo>) msg.obj);
                mAvailableTaskAdapter.notifyDataSetChanged();
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_task_block_up_info);

        final PackageManager packageManager = getApplicationContext().getPackageManager();
        final List<PackageInfo> packageInfos = packageManager.getInstalledPackages(0);

        mExecutor.execute(new FutureTask<List<TaskInfo>>(new Callable<List<TaskInfo>>() {
            @Override
            public List<TaskInfo> call() throws Exception {
                List<TaskInfo> installPackages = new ArrayList<>();
                for (int i = 0; packageInfos != null && i < packageInfos.size(); i++) {
                    PackageInfo packageInfo = packageInfos.get(i);
                    TaskInfo taskInfo = new TaskInfo();
                    taskInfo.setPackageName(packageInfo.applicationInfo.packageName);
                    taskInfo.setUid(packageInfo.applicationInfo.uid + "");
                    taskInfo.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));
                    installPackages.add(taskInfo);
                }
                return installPackages;
            }
        }) {
            @Override
            protected void done() {
                super.done();
                Message msg = myHandler.obtainMessage();
                msg.what = 1000;
                try {
                    msg.obj = get();
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                msg.sendToTarget();
            }
        });
        mLlViewAllTasks = findViewById(R.id.ll_viewAllTasks);
        mAvailableTaskAdapter = new AvailableTaskAdapter();
        mLlViewAllTasks.setAdapter(mAvailableTaskAdapter);
        mLlViewAllTasks.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                TaskInfo taskInfo = mAvailableTaskAdapter.mTaskInfoList.get(position);
                TimePickerFragment timePickerFragment = new TimePickerFragment();
                timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        TaskBlockUpInfo taskBlockUpInfo = new TaskBlockUpInfo();
                        taskBlockUpInfo.setPackageName(taskInfo.getPackageName());
                        taskBlockUpInfo.setUid(taskInfo.getUid());
                        taskBlockUpInfo.setMaxUsage(TimePickerFragment.getTimeInMillis(hourOfDay, minute));
                        taskBlockUpInfo.setBlockType(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE);
                        MotoExtendManager.getInstance(TaskBlockUpInfoActivity.this).addTaskBlockUpInfo(taskBlockUpInfo);
                    }
                });
                Toast.makeText(TaskBlockUpInfoActivity.this, "请输入限制的时长", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private class AvailableTaskAdapter extends BaseAdapter {

        private final List<TaskInfo> mTaskInfoList = new ArrayList<>();

        public AvailableTaskAdapter() {
        }

        public void setData(List<TaskInfo> taskInfoList) {
            mTaskInfoList.clear();
            if (taskInfoList != null) {
                mTaskInfoList.addAll(taskInfoList);
            }
        }

        @Override

        public int getCount() {
            return mTaskInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTaskInfoList.get(position);
        }

        @Override
        public int getItemViewType(int position) {
            return super.getItemViewType(position);
        }

        @Override
        public int getViewTypeCount() {
            return super.getViewTypeCount();
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            TaskInfo taskInfo = mTaskInfoList.get(position);
            if (convertView == null) {
                LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
                convertView = layoutInflater.inflate(R.layout.item_task_info, parent, false);
            }

            ImageView ivIcon = convertView.findViewById(R.id.iv_icon);
            TextView tvPackageName = convertView.findViewById(R.id.tv_packageName);

            ivIcon.setImageDrawable(taskInfo.getIcon());
            tvPackageName.setText(taskInfo.getPackageName());

            return convertView;
        }
    }
}