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

import java.util.Map;

public class TaskUsageInfoDailyFragment extends Fragment {

    public static final String KEY_TYPE = "page_type";

    private ScreenTimeControllerModel mScreenTimeControllerModel;

    public class TYPE {
        public static final int WEEK = 0;
        public static final int DAILY = 1;
    }

    private boolean isWeekType() {
        return getArguments().getInt(KEY_TYPE) == TYPE.WEEK;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv_ = new TextView(container.getContext());
        tv_.setText("hello" + getArguments().getInt(KEY_TYPE));
        return tv_;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mScreenTimeControllerModel = new ScreenTimeControllerModel(getContext());
        mScreenTimeControllerModel.queryTaskUsageInfoByDaily(new ScreenTimeControllerModel.OnResult() {
            @Override
            public void onResult(Object obj) {
                Map<Integer, Map<Integer, Long>> result = (Map<Integer, Map<Integer, Long>>) obj;

            }
        });
    }
}
