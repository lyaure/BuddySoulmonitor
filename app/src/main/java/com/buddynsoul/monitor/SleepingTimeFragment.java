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

        return v;
    }
}
