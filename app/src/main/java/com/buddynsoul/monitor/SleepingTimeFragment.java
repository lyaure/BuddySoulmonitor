package com.buddynsoul.monitor;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class SleepingTimeFragment extends Fragment {
    View v;
    public SleepingTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_sleeping_time, container, false);

        TextView data = (TextView)v.findViewById(R.id.stat_data_ID);

        SharedPreferences sp = getActivity().getSharedPreferences("tempData", getActivity().MODE_PRIVATE);
        long statData = sp.getLong("stat", -1);
        if(statData != -1)
            data.setText(String.valueOf(statData));

        TextView sleeping_time_txtv = (TextView)v.findViewById(R.id.sleeping_time_ID);

        int hour = 0, min = 0, sec = 0;

        int sleepingTime = sp.getInt("sleepingTime", -1);
        if (sleepingTime != -1)
            hour = sleepingTime / 3600;
            min = (sleepingTime  % 3600 ) / 60;
            sec = ((sleepingTime % 86400 ) % 3600 ) % 60;
        sleeping_time_txtv.setText(String.format("%s\n\n%d hour %d min %d sec", String.valueOf(sleepingTime), hour, min, sec));

        return v;
    }
}
