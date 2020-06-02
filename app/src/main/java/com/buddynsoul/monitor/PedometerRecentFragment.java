package com.buddynsoul.monitor;

import android.animation.ObjectAnimator;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.buddynsoul.monitor.Utils.Util;

import androidx.fragment.app.Fragment;

public class PedometerRecentFragment extends Fragment {
    final double stepToKilometre = 0.000762;


    public PedometerRecentFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pedometer_recent, container, false);

        TextView stepsAverage_txtv = (TextView)v.findViewById(R.id.stepsAverage_ID);
        TextView distanceAverage_txtv = (TextView)v.findViewById(R.id.distanceAverage_ID);
        ProgressBar stepsAverage_bar = (ProgressBar)v.findViewById(R.id.stepsAverage_progressBar_ID);
        ProgressBar distanceAverage_bar = (ProgressBar)v.findViewById(R.id.distanceAverage_progressBar_ID);

        TextView maxSteps_txtv = (TextView)v.findViewById(R.id.maxSteps_ID);
        TextView distanceMaxSteps_txtv = (TextView)v.findViewById(R.id.distanceMaxSteps_ID);
        ProgressBar maxSteps_bar = (ProgressBar)v.findViewById(R.id.maxSteps_progressBar_ID);
        ProgressBar distanceMaxSteps_bar = (ProgressBar)v.findViewById(R.id.distanceMaxSteps_progressBar_ID);

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);
        int  goal = sp.getInt("goal", 10000);

//        Database db = new Database(getActivity());
        Database db = Database.getInstance(getActivity());
        int maxSteps = db.getRecord();
        String maxSteps_txt = "Max steps:\n" + maxSteps;
        maxSteps_txtv.setText(maxSteps_txt);

        maxSteps_bar.setMax(goal);
        ObjectAnimator.ofInt(maxSteps_bar, "progress", maxSteps)
                .setDuration(3000)
                .start();

        final int maxDistance = (int) (goal * stepToKilometre);

        double distanceMaxSteps = maxSteps * stepToKilometre;
        distanceMaxSteps = Math.round(distanceMaxSteps * 1000d) / 1000d;
        String distanceMaxSteps_txt = "Max distance:\n" + distanceMaxSteps + " (km)";
        distanceMaxSteps_txtv.setText(distanceMaxSteps_txt);

        distanceMaxSteps_bar.setMax(maxDistance);
        ObjectAnimator.ofInt(distanceMaxSteps_bar, "progress", (int) distanceMaxSteps)
                .setDuration(3000)
                .start();

        int stepsAverage = db.getSteps(Util.getSpecificDate(7), Util.getYesterday()) / 7;
        String averageSteps_txt = "Average steps:\n" + stepsAverage;
        stepsAverage_txtv.setText(averageSteps_txt);

        stepsAverage_bar.setMax(goal);
        ObjectAnimator.ofInt(stepsAverage_bar, "progress", stepsAverage)
                .setDuration(3000)
                .start();

        double distanceAverage = stepsAverage * stepToKilometre;
        distanceAverage = Math.round(distanceAverage * 1000d) / 1000d;
        String stepsAverage_txt = "Average distance:\n" + distanceAverage + " (km)";
        distanceAverage_txtv.setText(stepsAverage_txt);

        distanceAverage_bar.setMax(maxDistance);
        ObjectAnimator.ofInt(distanceAverage_bar, "progress", (int) (distanceAverage))
                .setDuration(3000)
                .start();

        return v;
    }
}
