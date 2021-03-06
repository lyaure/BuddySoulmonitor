package com.buddynsoul.monitor.Fragments.Monitor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buddynsoul.monitor.Dialogs.NumberPickerDialog;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;


public class SleepGoalFragment extends Fragment {
    private View v;
    public SleepGoalFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_sleep_goal, container, false);

        Database db = Database.getInstance(getActivity());

        // init views
        TextView yesterday = (TextView)v.findViewById(R.id.sleep_yesterday_ID);
        TextView hours = (TextView)v.findViewById(R.id.sleep_hours_goal_ID);
        TextView minutes = (TextView)v.findViewById(R.id.sleep_min_goal_ID);

        int yesterdaySleep = db.getSleepDuration(Util.getYesterday());

        if(yesterdaySleep > 0)
            yesterday.setText("Yesterday I slept for\n" + getHours(yesterdaySleep) + " hours and " + getMinutes(yesterdaySleep) + " minutes");
        else
            yesterday.setText("No data for yesterday's sleep duration");

        int todayGoal = db.getSleepGoal(Util.getToday());
        if(todayGoal == Integer.MIN_VALUE || todayGoal == -1) {
            hours.setText("8");
            minutes.setText("00");
        }
        else{
            hours.setText(String.valueOf(getHours(todayGoal)));
            minutes.setText(String.valueOf(getMinutes(todayGoal)));
        }

        hours.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerDialog dialogFragment = new NumberPickerDialog();

                // to recover from where we cam
                Bundle bundle = new Bundle();
                bundle.putString("from", "sleepHours");
                dialogFragment.setArguments(bundle);

                dialogFragment.show(getActivity().getSupportFragmentManager(), "sleep");
            }
        });

        minutes.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerDialog dialogFragment = new NumberPickerDialog();

                // to recover from where we came
                Bundle bundle = new Bundle();
                bundle.putString("from", "sleepMinutes");
                dialogFragment.setArguments(bundle);

                dialogFragment.show(getActivity().getSupportFragmentManager(), "sleep");
            }
        });


        return v;
    }

    // get hours from millisec
    private int getHours(int time){
        return (int)time / 3600;
    }

    // get minutes from millisec
    private int getMinutes(int time){
        return ((int)time % 3600) / 60;
    }


}
