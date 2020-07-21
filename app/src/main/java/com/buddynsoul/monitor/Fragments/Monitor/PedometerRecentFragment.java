package com.buddynsoul.monitor.Fragments.Monitor;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.buddynsoul.monitor.Dialogs.NumberPickerDialog;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;

import androidx.fragment.app.Fragment;

public class PedometerRecentFragment extends Fragment {
    final double stepToKilometre = 0.000762;
    View v;
    TextView today;

    public PedometerRecentFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_pedometer_recent, container, false);

        // init views
        TextView stepsAverage_txtv = (TextView)v.findViewById(R.id.stepsAverage_ID);
        TextView distanceAverage_txtv = (TextView)v.findViewById(R.id.distanceAverage_ID);
        ProgressBar stepsAverage_bar = (ProgressBar)v.findViewById(R.id.stepsAverage_progressBar_ID);
        ProgressBar distanceAverage_bar = (ProgressBar)v.findViewById(R.id.distanceAverage_progressBar_ID);

        TextView maxSteps_txtv = (TextView)v.findViewById(R.id.maxSteps_ID);
        TextView distanceMaxSteps_txtv = (TextView)v.findViewById(R.id.distanceMaxSteps_ID);
        ProgressBar maxSteps_bar = (ProgressBar)v.findViewById(R.id.maxSteps_progressBar_ID);
        ProgressBar distanceMaxSteps_bar = (ProgressBar)v.findViewById(R.id.distanceMaxSteps_progressBar_ID);

        TextView yesterday = (TextView)v.findViewById(R.id.yesterday_steps_ID);
        today = (TextView)v.findViewById(R.id.today_goal_ID);

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);
        int  goal = sp.getInt("goal", 10000);

        Database db = Database.getInstance(getActivity());
        int maxSteps = db.getRecord();
        String maxSteps_txt = "Max steps:\n" + maxSteps;
        maxSteps_txtv.setText(maxSteps_txt);

        // animate progress bar 1
        maxSteps_bar.setMax(goal);
        ObjectAnimator.ofInt(maxSteps_bar, "progress", maxSteps)
                .setDuration(3000)
                .start();

        final int maxDistance = (int) (goal * stepToKilometre);

        double distanceMaxSteps = maxSteps * stepToKilometre;
        distanceMaxSteps = Math.round(distanceMaxSteps * 1000d) / 1000d;
        String distanceMaxSteps_txt = "Max distance:\n" + distanceMaxSteps + " (km)";
        distanceMaxSteps_txtv.setText(distanceMaxSteps_txt);

        // animate progress bar 2
        distanceMaxSteps_bar.setMax(maxDistance);
        ObjectAnimator.ofInt(distanceMaxSteps_bar, "progress", (int) distanceMaxSteps)
                .setDuration(3000)
                .start();

        int stepsAverage = db.getSteps(Util.getSpecificDate(7), Util.getYesterday()) / 7;
        String averageSteps_txt = "Average steps:\n" + stepsAverage;
        stepsAverage_txtv.setText(averageSteps_txt);

        // animate progress bar 3
        stepsAverage_bar.setMax(goal);
        ObjectAnimator.ofInt(stepsAverage_bar, "progress", stepsAverage)
                .setDuration(3000)
                .start();

        double distanceAverage = stepsAverage * stepToKilometre;
        distanceAverage = Math.round(distanceAverage * 1000d) / 1000d;
        String stepsAverage_txt = "Average\ndistance:\n" + distanceAverage + " (km)";
        distanceAverage_txtv.setText(stepsAverage_txt);

        // animate progress bar 4
        distanceAverage_bar.setMax(maxDistance);
        ObjectAnimator.ofInt(distanceAverage_bar, "progress", (int) (distanceAverage))
                .setDuration(3000)
                .start();

        int yesterdaySteps = db.getSteps(Util.getYesterday());
        if(yesterdaySteps != Integer.MIN_VALUE)
            yesterday.setText("Yesterday I walked " + yesterdaySteps + " steps");

        else
            yesterday.setText("No data for yesterday's steps");

        int todayGoal = db.getStepGoal(Util.getToday());
        if(todayGoal == Integer.MIN_VALUE || todayGoal == -1)
            today.setText(String.valueOf(goal));
        else
            today.setText(String.valueOf(todayGoal));


        // add new daily goal
        today.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                NumberPickerDialog dialogFragment = new NumberPickerDialog();

                // to recover from which fragment we came
                Bundle bundle = new Bundle();
                bundle.putString("from", "pedometer");
                dialogFragment.setArguments(bundle);

                dialogFragment.show(getActivity().getSupportFragmentManager(), "steps");

                db.insertStepGoal(Integer.valueOf(today.getText().toString()));
            }
        });

        return v;
    }
}
