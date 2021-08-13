package com.motorola.screentimecontroller;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.motorola.screentimecontroller.bean.ErrorCode;
import com.motorola.screentimecontroller.bean.TaskInfo;
import com.motorola.screentimecontroller.utils.SystemUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;

/**
 * 设置应用白名单
 */
public class TaskWhiteListActivity extends Activity {

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();

    private WhiteListAdapter mWhiteListAdapter;
    private TaskListAdapter mTaskListAdapter;

    private Handler myHandler = new Handler(Looper.getMainLooper()) {
        @Override
        public void handleMessage(@NonNull Message msg) {
            super.handleMessage(msg);
            if (msg.obj != null) {
                Map<Integer, List<TaskInfo>> taskInfos = (Map<Integer, List<TaskInfo>>) msg.obj;
                mWhiteListAdapter.setData(taskInfos.get(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_ALWAYS_ALLOW));
                mTaskListAdapter.setData(taskInfos.get(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE));
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_task_white_list);

        ListView lvWhiteList = findViewById(R.id.lv_whiteList);
        ListView lvTaskList = findViewById(R.id.lv_taskList);

        mWhiteListAdapter = new WhiteListAdapter();
        mTaskListAdapter = new TaskListAdapter();

        lvWhiteList.setAdapter(mWhiteListAdapter);
        lvTaskList.setAdapter(mTaskListAdapter);

        mExecutor.execute(new FutureTask<Map<Integer, List<TaskInfo>>>(new MyCaller()) {
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
    }

    private class MyCaller implements Callable<Map<Integer, List<TaskInfo>>> {
        @Override
        public Map<Integer, List<TaskInfo>> call() throws Exception {

            PackageManager packageManager = getApplicationContext().getPackageManager();
            List<PackageInfo> packageInfos = null;
            packageInfos = packageManager.getInstalledPackages(0);

            List<Bundle> taskBlockUpInfoBundles = null;
            try {
                taskBlockUpInfoBundles = MotoExtendManager.getInstance(TaskWhiteListActivity.this).getTaskBlockUpInfo();
            } catch (Exception e) {
                return new HashMap<>();
            }

            List<TaskBlockUpInfo> taskBlockUpInfos = new ArrayList<>();
            if (taskBlockUpInfoBundles != null) {
                for (Bundle bundle : taskBlockUpInfoBundles) {
                    taskBlockUpInfos.add(new TaskBlockUpInfo(bundle));
                }
            }

            Map<Integer, List<TaskInfo>> installPackages = new HashMap<>();
            List<TaskInfo> maxUsageTaskBlockUpInfos = new ArrayList<>();
            List<TaskInfo> alwaysAllowTaskBlockUpInfos = new ArrayList<>();

            installPackages.put(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE, maxUsageTaskBlockUpInfos);
            installPackages.put(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_ALWAYS_ALLOW, alwaysAllowTaskBlockUpInfos);

            for (int i = 0; packageInfos != null && i < packageInfos.size(); i++) {

                PackageInfo packageInfo = packageInfos.get(i);
                if (packageInfo == null || packageInfo.applicationInfo == null
                        || SystemUtils.isSystemApp(packageInfo.applicationInfo)
                        || "com.motorola.screentimecontroller".equals(packageInfo.packageName)) {
                    continue;
                }
                TaskInfo taskInfo = new TaskInfo();

                taskInfo.setAppName(packageInfo.applicationInfo.loadLabel(packageManager).toString());
                taskInfo.setPackageName(packageInfo.applicationInfo.packageName);
                taskInfo.setUid(packageInfo.applicationInfo.uid);
                taskInfo.setIcon(packageInfo.applicationInfo.loadIcon(packageManager));

                for (TaskBlockUpInfo taskBlockUpInfo : taskBlockUpInfos) {
                    if (taskBlockUpInfo.getPackageName() != null && taskBlockUpInfo.getPackageName().equals(taskInfo.getPackageName())
                            && taskBlockUpInfo.getUid() != null && +taskBlockUpInfo.getUid() == taskInfo.getUid()) {
                        taskInfo.setServer(true);
                        taskInfo.setMaxUsage(taskBlockUpInfo.getMaxUsage());
                        taskInfo.setBlockType(taskBlockUpInfo.getBlockType());
                        break;
                    }
                }
                if (taskInfo.getBlockType() == TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_ALWAYS_ALLOW) {
                    alwaysAllowTaskBlockUpInfos.add(taskInfo);
                } else {
                    maxUsageTaskBlockUpInfos.add(taskInfo);
                }
            }
            return installPackages;
        }
    }

    private class WhiteListAdapter extends BaseAdapter {

        private List<TaskInfo> mWhiteList;

        public void setData(List<TaskInfo> taskInfoList) {
            this.mWhiteList = taskInfoList;
            notifyDataSetChanged();
        }

        @Override
        public int getCount() {
            return mWhiteList == null ? 0 : mWhiteList.size();
        }

        @Override
        public Object getItem(int position) {
            return mWhiteList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_white_list_item, parent, false);
            }

            TaskInfo taskInfo = mWhiteList.get(position);

            Button btAddToWhiteList = convertView.findViewById(R.id.bt_addToWhiteList);
            TextView tvPacakgeName = convertView.findViewById(R.id.tv_pacakgeName);

            tvPacakgeName.setText(taskInfo.getAppName());

            btAddToWhiteList.setText(R.string.remove_always_allow);
            btAddToWhiteList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    TaskInfo taskInfo = mWhiteList.get(position);
                    TaskBlockUpInfo taskBlockUpInfo = new TaskBlockUpInfo();
                    taskBlockUpInfo.setPackageName(taskInfo.getPackageName());
                    taskBlockUpInfo.setUid(taskInfo.getUid());
                    taskBlockUpInfo.setMaxUsage(taskInfo.getMaxUsage());
                    if (taskBlockUpInfo.getMaxUsage() > 0) {
                        taskBlockUpInfo.setBlockType(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE);
                    } else {
                        taskBlockUpInfo.setBlockType(0);
                    }

                    long updateCount = MotoExtendManager.getInstance(getApplicationContext()).updateTaskBlockUpInfo(taskBlockUpInfo);

                    if (updateCount > 0) {
                        if (mWhiteList.remove(taskInfo)) {
                            Toast.makeText(TaskWhiteListActivity.this, "remove success 111 " + position, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskWhiteListActivity.this, "remove failed 111 " + position, Toast.LENGTH_SHORT).show();
                        }

                        notifyDataSetChanged();
                        mTaskListAdapter.mTaskInfoList.add(0, taskInfo);
                        mTaskListAdapter.notifyDataSetChanged();
                    } else {
                        Toast.makeText(TaskWhiteListActivity.this, "删除失败: " + updateCount, Toast.LENGTH_SHORT).show();
                    }
                }
            });

            return convertView;
        }
    }

    private class TaskListAdapter extends BaseAdapter {

        private List<TaskInfo> mTaskInfoList;

        @Override
        public int getCount() {
            return mTaskInfoList == null ? 0 : mTaskInfoList.size();
        }

        @Override
        public Object getItem(int position) {
            return mTaskInfoList.get(position);
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_task_white_list_item, parent, false);
            }

            TaskInfo taskInfo = mTaskInfoList.get(position);

            Button btAddToWhiteList = convertView.findViewById(R.id.bt_addToWhiteList);
            TextView tvPacakgeName = convertView.findViewById(R.id.tv_pacakgeName);

            tvPacakgeName.setText(taskInfo.getAppName());

            btAddToWhiteList.setText(R.string.add_always_allow);
            btAddToWhiteList.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {

                    TaskInfo taskInfo = mTaskInfoList.get(position);
                    TaskBlockUpInfo taskBlockUpInfo = new TaskBlockUpInfo();
                    taskBlockUpInfo.setPackageName(taskInfo.getPackageName());
                    taskBlockUpInfo.setUid(taskInfo.getUid());
                    taskBlockUpInfo.setMaxUsage(taskInfo.getMaxUsage());
                    taskBlockUpInfo.setBlockType(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_ALWAYS_ALLOW);


                    long updateCount = 0;
                    try {
                        if (taskInfo.isServer()) {
                            updateCount = MotoExtendManager.getInstance(getApplicationContext()).updateTaskBlockUpInfo(taskBlockUpInfo);
                        } else {
                            updateCount = MotoExtendManager.getInstance(getApplicationContext()).addTaskBlockUpInfo(taskBlockUpInfo);
                        }
                    } catch (Exception e) {
                    }
                    if (updateCount > 0) {
                        if (mTaskInfoList.remove(taskInfo)) {
                            Toast.makeText(TaskWhiteListActivity.this, "Remove task success " + updateCount, Toast.LENGTH_SHORT).show();
                        } else {
                            Toast.makeText(TaskWhiteListActivity.this, "Remove task failed " + updateCount, Toast.LENGTH_SHORT).show();
                        }
                        notifyDataSetChanged();
                        mWhiteListAdapter.mWhiteList.add(0, taskInfo);
                        mWhiteListAdapter.notifyDataSetChanged();
                    } else if (updateCount == ErrorCode.DATA_CONFLICT) {
                        Toast.makeText(TaskWhiteListActivity.this, "添加白名单失败: 重复设置了.", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TaskWhiteListActivity.this, "添加白名单失败 " + updateCount, Toast.LENGTH_SHORT).show();
                    }

                }
            });

            return convertView;
        }

        public void setData(List<TaskInfo> taskInfos) {
            this.mTaskInfoList = taskInfos;
            notifyDataSetChanged();
        }
    }

}