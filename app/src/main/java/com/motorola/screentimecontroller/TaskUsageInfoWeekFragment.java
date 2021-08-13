package com.motorola.screentimecontroller;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.motorola.screentimecontroller.model.ScreenTimeControllerModel;
import com.motorola.screentimecontroller.utils.TaskUsageUtil;
import com.motorola.screentimecontroller.utils.TimeUtils;

import java.util.Calendar;
import java.util.Date;
import java.util.Map;

public class TaskUsageInfoWeekFragment extends Fragment {

    public static final String KEY_TYPE = "page_type";

    /**
     * 数据访问 Model
     */
    private ScreenTimeControllerModel mScreenTimeControllerModel;

    /**
     * 本周任务使用时长
     */
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosWeek;
    private Map<Integer, Map<Integer, Long>> mTaskUsageInfosLastWeek;

    /**
     * 更新时间
     */
    private TextView mTvUpdateTime;

    public class TYPE {
        public static final int WEEK = 0;
        public static final int DAILY = 1;
    }

    private TextView mTvCompareWithLastWeekTitle;
    private TextView mTvCompareWithLastWeek;

    private TextView mTvTotalScreenUsage;
    private TextView mTvDailyAverageInfo;

    private TextView mTvScreenUsageByDay;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.layout_fragment_task_usage_week, container, false);

        mTvCompareWithLastWeekTitle = view.findViewById(R.id.tv_compareWithLastWeekTitle);
        mTvCompareWithLastWeek = view.findViewById(R.id.tv_compareWithLastWeek);

        mTvTotalScreenUsage = view.findViewById(R.id.tv_totalScreenUsage);
        mTvDailyAverageInfo = view.findViewById(R.id.tv_dailyAverageInfo);
        mTvUpdateTime = view.findViewById(R.id.tv_updateTime);

        mTvScreenUsageByDay = view.findViewById(R.id.tv_screenUsageByDay);

        view.findViewById(R.id.tv_screenUsageByHour).setVisibility(View.GONE);

        return view;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenTimeControllerModel = new ScreenTimeControllerModel(getContext());
        loadData();
    }

    private boolean isWeekType() {
        return getArguments().getInt(KEY_TYPE) == TYPE.WEEK;
    }

    private void loadData() {
        final Calendar calendar = TimeUtils.getWeekFirstDayCalendar();
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
        mScreenTimeControllerModel.queryTaskUsageInfoUpdateTime(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                if (obj instanceof Long) {
                    Long updateTime = (Long) obj;
                    mTvUpdateTime.setText(getString(R.string.update_time_regex, new Date(updateTime).toLocaleString()));
                }
            }
        });
    }


    private void updateUsageInfo() {

        if (mTaskUsageInfosWeek == null || mTaskUsageInfosLastWeek == null) {
            return;
        }

        long totalUsageWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosWeek);
        long totalUsageLastWeek = TaskUsageUtil.getTotalUsage(mTaskUsageInfosLastWeek);

        if (totalUsageLastWeek == 0) {
            mTvCompareWithLastWeekTitle.setVisibility(View.GONE);
            mTvCompareWithLastWeek.setVisibility(View.GONE);
        } else {
            mTvCompareWithLastWeekTitle.setVisibility(View.VISIBLE);
            mTvCompareWithLastWeek.setVisibility(View.VISIBLE);
            mTvCompareWithLastWeek.setText(TaskUsageUtil.getWeekInscreaseFormat(totalUsageWeek, totalUsageLastWeek) + "");
        }

        mTvTotalScreenUsage.setText(TimeUtils.getUsageDescription(totalUsageWeek));
        mTvDailyAverageInfo.setText(TimeUtils.getUsageDescription(TimeUtils.getDailyUsage(TimeUtils.getWeekFirstDayCalendar(), mTaskUsageInfosWeek)));
        mTvScreenUsageByDay.setText(TimeUtils.getTaskUsageByDayDesc(TimeUtils.getWeekFirstDayCalendar(), mTaskUsageInfosWeek));

    }


}
