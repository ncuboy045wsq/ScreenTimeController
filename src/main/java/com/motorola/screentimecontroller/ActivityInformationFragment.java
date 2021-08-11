package com.motorola.screentimecontroller;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ActivityInformationFragment extends Fragment {

    public static final String KEY_TYPE = "page_type";

    public class TYPE {
        public static final int WEEK = 0;
        public static final int DAILY = 1;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        TextView tv_ = new TextView(container.getContext());
        tv_.setText("hello" + getArguments().getInt(KEY_TYPE));
        return tv_;
    }
}
