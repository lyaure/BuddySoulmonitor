package com.buddynsoul.monitor;

import android.app.Dialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.TimePickerDialog;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.Calendar;

public class TimePickerFragment extends DialogFragment {
    String from;

    public TimePickerFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        // Use the current time as the default values for the picker
        final Calendar calendar = Calendar.getInstance();
        int hour = calendar.get(Calendar.HOUR_OF_DAY);
        int minute = calendar.get(Calendar.MINUTE);

        Bundle bundle = this.getArguments();
        if(bundle != null)
            from = bundle.getString("from", "null");

        // Create a new instance of TimePickerDialog and return it
        return  new TimePickerDialog(getActivity(), new TimePickerDialog.OnTimeSetListener() {
            @Override
            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
                calendar.set(Calendar.MINUTE, minute);
                String am_pm = calendar.get(Calendar.AM_PM) == Calendar.AM ? "am" : "pm";

                if(hourOfDay > 12)
                    hourOfDay -= 12;

                if(hourOfDay == 0 && am_pm.equals("am"))
                    hourOfDay = 0;

                if(hourOfDay == 0 && am_pm.equals("pm"))
                    hourOfDay = 12;

                if(!from.equals("null")){
                    TextView time;
                    SharedPreferences sp = getActivity().getSharedPreferences("prefTime", getActivity().MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    if(from.equals("fromTime")) {
                        time = (TextView)getActivity().findViewById(R.id.fromTime_ID);
//                        time.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + " " + am_pm);
                        editor.putInt("fromHour", hourOfDay);
                        editor.putInt("fromMinute", minute);
                        editor.putString("am_pm_from", am_pm);
                    }
                    else {
                        time = (TextView)getActivity().findViewById(R.id.toTime_ID);
//                        time.setText(hourOfDay + ":" + minute + " " + am_pm);
                        editor.putInt("toHour", hourOfDay);
                        editor.putInt("toMinute", minute);
                        editor.putString("am_pm_to", am_pm);
                    }

                    time.setText(String.format("%02d", hourOfDay) + ":" + String.format("%02d", minute) + " " + am_pm);

                    editor.commit();
                }
            }
        }, hour, minute, false);
    }
}