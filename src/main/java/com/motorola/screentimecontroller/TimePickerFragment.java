package com.motorola.screentimecontroller;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.TimePickerDialog;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.widget.TimePicker;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment implements TimePickerDialog.OnTimeSetListener {

    private TimePickerDialog.OnTimeSetListener mOnTimeSetListener;

    public TimePickerFragment() {
    }

    public static long getTimeInMillis(int hourOfDay, int minute) {
        return hourOfDay * 60 * 60 * 1000 + minute * 60 * 1000;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);

        // Create a new instance of TimePickerDialog and return it
        TimePickerDialog timePickerDialog = new TimePickerDialog(getActivity(), this, hour, minute,
                DateFormat.is24HourFormat(getActivity()));
        return timePickerDialog;
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
        // Do something with the time chosen by the user
        if (this.mOnTimeSetListener != null) {
            this.mOnTimeSetListener.onTimeSet(view, hourOfDay, minute);
        }
    }

    public TimePickerDialog.OnTimeSetListener getOnTimeSetListener() {
        return this.mOnTimeSetListener;
    }

    public void setOnTimeSetListener(TimePickerDialog.OnTimeSetListener onTimeSetListener) {
        this.mOnTimeSetListener = onTimeSetListener;
    }
}