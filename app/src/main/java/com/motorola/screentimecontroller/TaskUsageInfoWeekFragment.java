package com.motorola.screentimecontroller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;
import com.motorola.screentimecontroller.utils.TaskUsageUtil;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.Calendar;
import java.util.Map;

public class TaskUsageInfoWeekFragment extends Fragment {

    public static final String KEY_TYPE = "page_type";

    /**
     * 本周任务使用时长
     */
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosWeek;
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosLastWeek;

    private ScreenTimeControllerModel mScreenTimeControllerModel;

    public class TYPE {
        public static final int WEEK = 0;
        public static final int DAILY = 1;
    }

    private boolean isWeekType() {
        return getArguments().getInt(KEY_TYPE) == TYPE.WEEK;
    }

    private TextView mTvCompareWithLastWeek;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.layout_fragment_task_usage_week, container, false);
        mTvCompareWithLastWeek = view.findViewById(R.id.tv_compareWithLastWeek);
        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenTimeControllerModel = new ScreenTimeControllerModel(getContext());
        loadData();
    }

    private void loadData() {
        final Calendar calendar = Calendar.getInstance();
        if (calendar.get(Calendar.DAY_OF_WEEK) - calendar.getFirstDayOfWeek() > 0) {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - calendar.get(Calendar.DAY_OF_WEEK));
        } else {
            calendar.add(Calendar.DAY_OF_MONTH, calendar.getFirstDayOfWeek() - (calendar.get(Calendar.DAY_OF_WEEK) + 7));
        }

        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        mScreenTimeControllerModel.queryTaskUsageInfo(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                mTaskUsageInfosWeek = (Map<Integer, Map<Integer, Long>>) obj;
                updateUsageInfo();
            }
        }, calendar.getTimeInMillis(), calendar.getTimeInMillis() + 7 * TimeUtils.ONE_DAY);

        mScreenTimeControllerModel.queryTaskUsageInfo(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                mTaskUsageInfosLastWeek = (Map<Integer, Map<Integer, Long>>) obj;
                updateUsageInfo();
            }
        }, calendar.getTimeInMillis() - 7 * TimeUtils.ONE_DAY, calendar.getTimeInMillis());
    }

    private void updateUsageInfo() {

        if (mTaskUsageInfosWeek == null || mTaskUsageInfosLastWeek == null) {
            return;
        }

        long totalUsageWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosWeek);
        long totalUsageLastWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosLastWeek);

        if (totalUsageLastWeek == 0) {
            mTvCompareWithLastWeek.setVisibility(View.VISIBLE);
        } else {
            mTvCompareWithLastWeek.setText(TaskUsageUtil.getWeekInscreaseFormat(totalUsageWeek, totalUsageLastWeek) + "");
        }
    }


}
