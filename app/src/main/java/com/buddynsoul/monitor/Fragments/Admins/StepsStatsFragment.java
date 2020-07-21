package com.buddynsoul.monitor.Fragments.Admins;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buddynsoul.monitor.Activities.AdminActivity;
import com.buddynsoul.monitor.Objects.UserStat;
import com.buddynsoul.monitor.R;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;


public class StepsStatsFragment extends Fragment {
    private View v;
    private BarChart stepsChart;
    public StepsStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_steps_stats, container, false);

        stepsChart = (BarChart)v.findViewById(R.id.steps_barchart_ID);
        drawAsleepChart();

        return v;
    }

    private void drawAsleepChart(){
        AdminActivity activity = (AdminActivity)getActivity();
        ArrayList<UserStat> users = activity.getFinalStats();

        ArrayList steps = new ArrayList();

        steps.add("0");
        steps.add("1000");
        steps.add("2000");
        steps.add("3000");
        steps.add("4000");
        steps.add("5000");
        steps.add("6000");
        steps.add("7000");
        steps.add("8000");
        steps.add("9000");
        steps.add("10000");


        float[] results = new float[steps.size()];

        for(int i=0; i<users.size(); i++){
            if(users.get(i).getSteps() / 1000 >= 10)
                results[11]++;
            else
                results[users.get(i).getSteps() / 1000] ++;
        }

        ArrayList stats = new ArrayList();

        for(int i=0; i<results.length; i++)
            stats.add(new BarEntry(results[i], i));


//        stats.add(new BarEntry(30f, 0));
//        stats.add(new BarEntry(60f, 1));
//        stats.add(new BarEntry(110f, 2));
//        stats.add(new BarEntry(90f, 3));
//        stats.add(new BarEntry(75f, 4));
//        stats.add(new BarEntry(185f, 5));
//        stats.add(new BarEntry(100f, 6));
//        stats.add(new BarEntry(90f, 7));
//        stats.add(new BarEntry(130f, 8));
//        stats.add(new BarEntry(35f, 9));
//        stats.add(new BarEntry(25f, 10));


        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));

        BarDataSet bardataset = new BarDataSet(stats, "Users");
        stepsChart.animateY(5000);
        BarData data = new BarData(steps, bardataset);

        bardataset.setColors(colors);
        stepsChart.setData(data);

    }
}
