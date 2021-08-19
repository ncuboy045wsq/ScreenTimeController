package com.motorola.screentimecontroller;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
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
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.motorola.screentimecontroller.bean.ErrorCode;
import com.motorola.screentimecontroller.bean.TaskInfo;
import com.motorola.screentimecontroller.utils.SystemUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.FutureTask;

import motorola.core_services.misc.MotoExtendManager;
import motorola.core_services.screentimecontroller.TimeUtil;
import motorola.core_services.screentimecontroller.bean.TaskBlockUpInfo;

/**
 * 设置应用最大使用时长
 */
public class TaskBlockUpInfoActivity extends Activity {

    private final ExecutorService mExecutor = Executors.newCachedThreadPool();
    private ListView mLlViewAllTasks;
    private AvailableTaskAdapter mAvailableTaskAdapter;

    /**
     * 所有ui控件的控制器
     */
    private LinearLayout mLlContentContainer;

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

    private class MyCaller implements Callable<List<TaskInfo>> {

        PackageManager mPackageManager;

        public MyCaller(PackageManager packageManager) {
            this.mPackageManager = packageManager;
        }

        @Override
        public List<TaskInfo> call() throws Exception {

            final List<PackageInfo> packageInfos = mPackageManager.getInstalledPackages(0);
            List<Bundle> taskBlockUpInfoBundles = MotoExtendManager.getInstance(TaskBlockUpInfoActivity.this).getTaskBlockUpInfo();
            List<TaskBlockUpInfo> taskBlockUpInfos = new ArrayList<>();
            if (taskBlockUpInfoBundles != null) {
                for (Bundle bundle : taskBlockUpInfoBundles) {
                    taskBlockUpInfos.add(new TaskBlockUpInfo(bundle));
                }
            }

            List<TaskInfo> installPackages = new ArrayList<>();
            for (int i = 0; packageInfos != null && i < packageInfos.size(); i++) {
                PackageInfo packageInfo = packageInfos.get(i);
                if (SystemUtils.isSystemApp(packageInfo.applicationInfo)
                        || "com.motorola.screentimecontroller".equals(packageInfo.packageName)) {
                    continue;
                }
                TaskInfo taskInfo = new TaskInfo();
                taskInfo.setAppName(packageInfo.applicationInfo.loadLabel(mPackageManager).toString());
                taskInfo.setPackageName(packageInfo.applicationInfo.packageName);
                taskInfo.setUid(packageInfo.applicationInfo.uid);
                taskInfo.setIcon(packageInfo.applicationInfo.loadIcon(mPackageManager));
                for (TaskBlockUpInfo taskBlockUpInfo : taskBlockUpInfos) {
                    if (taskBlockUpInfo.getPackageName() != null && taskBlockUpInfo.getPackageName().equals(taskInfo.getPackageName())
                            && taskBlockUpInfo.getUid() != null && taskBlockUpInfo.getUid().equals(taskInfo.getUid())) {
//                            && taskBlockUpInfo.getUserId() != null && taskBlockUpInfo.getUserId().equals(taskInfo.getUserId())) {
                        taskInfo.setMaxUsage(taskBlockUpInfo.getMaxUsage());
                        taskInfo.setBlockType(taskBlockUpInfo.getBlockType());
                        taskInfo.setServer(true);
                    }
                }
                installPackages.add(taskInfo);
            }
            return installPackages;
        }
    }


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.layout_task_block_up_info);

        mLlContentContainer = findViewById(R.id.ll_contentContainer);
        mLlContentContainer.setVisibility(View.GONE);

        mExecutor.execute(new FutureTask<List<TaskInfo>>(new MyCaller(getApplicationContext().getPackageManager())) {
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
                showTimePicker("pick_time", taskInfo);
                Toast.makeText(TaskBlockUpInfoActivity.this, "请输入限制的时长", Toast.LENGTH_SHORT).show();
            }
        });

        showPasswordDialog();
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());

    private void showPasswordDialog() {

//        AlertDialog.Builder builder = new AlertDialog.Builder(this);
//        builder.setMessage(R.string.enter_password)
//                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//
//                    }
//                })
//                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
//                    public void onClick(DialogInterface dialog, int id) {
//                        finish();
//                    }
//                });
//        // Create the AlertDialog object and return it
//        builder.create().show();

        MotoExtendManager.getInstance(TaskBlockUpInfoActivity.this)
                .showKeyguardCredentialOfMainUser(
                        TaskBlockUpInfoActivity.this,
                        getString(R.string.enter_password),
                        mHandler,
                        () -> {
                            // Auth
                            mLlContentContainer.setVisibility(View.VISIBLE);
                        },
                        () -> {});
    }

    private void showTimePicker(String tag, TaskInfo taskInfo) {

        Fragment timePickerFragmentTag = getFragmentManager().findFragmentByTag(tag);
        if (timePickerFragmentTag != null) {
            getFragmentManager().beginTransaction().remove(timePickerFragmentTag).commitAllowingStateLoss();
        }

        TimePickerFragment timePickerFragment = new TimePickerFragment();
        Bundle args = new Bundle();
        args.putBoolean(TimePickerFragment.KEY_24_HOUR_FORMAT, true);
        timePickerFragment.setArguments(args);
        timePickerFragment.setOnTimeSetListener(new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                TaskBlockUpInfo taskBlockUpInfo = new TaskBlockUpInfo();
                taskBlockUpInfo.setPackageName(taskInfo.getPackageName());
                taskBlockUpInfo.setUid(taskInfo.getUid());
                taskBlockUpInfo.setMaxUsage(TimePickerFragment.getTimeInMillis(hourOfDay, minute));
                if (taskInfo.getBlockType() != TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_ALWAYS_ALLOW) {
                    taskBlockUpInfo.setBlockType(TaskBlockUpInfo.BLOCK_TYPE.TYPE_MAX_USAGE);
                }
                try {
                    Long result = 0l;
                    if (taskInfo.isServer()) {
                        result = MotoExtendManager.getInstance(TaskBlockUpInfoActivity.this).updateTaskBlockUpInfo(taskBlockUpInfo);
                    } else {
                        result = MotoExtendManager.getInstance(TaskBlockUpInfoActivity.this).addTaskBlockUpInfo(taskBlockUpInfo);
                    }

                    if (result > 0) {
                        Toast.makeText(TaskBlockUpInfoActivity.this, "设置成功", Toast.LENGTH_SHORT).show();
                        taskInfo.setMaxUsage(taskBlockUpInfo.getMaxUsage());
                        mAvailableTaskAdapter.notifyDataSetChanged();
                    } else if (result == ErrorCode.DATA_CONFLICT) {
                        Toast.makeText(TaskBlockUpInfoActivity.this, "设置失败: 存在重复的设置", Toast.LENGTH_SHORT).show();
                    } else {
                        Toast.makeText(TaskBlockUpInfoActivity.this, "设置失败", Toast.LENGTH_SHORT).show();
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
        timePickerFragment.show(getFragmentManager(), "time_pickerer");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
        }
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
            TextView tvAppLimit = convertView.findViewById(R.id.tv_appLimit);

            ivIcon.setImageDrawable(taskInfo.getIcon());
            tvPackageName.setText(taskInfo.getAppName());
            if (taskInfo.getMaxUsage() > 0) {
                tvAppLimit.setText(TimeUtil.getTimeFormat(taskInfo.getMaxUsage()));
            } else {
                tvAppLimit.setText(null);
            }

            return convertView;
        }
    }
}