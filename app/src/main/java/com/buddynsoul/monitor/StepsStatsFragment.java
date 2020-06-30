package com.buddynsoul.monitor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;

import java.util.ArrayList;


public class StepsStatsFragment extends Fragment {
    View v;
    BarChart stepsChart;
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
        ArrayList users = new ArrayList();

        users.add(new BarEntry(30f, 0));
        users.add(new BarEntry(60f, 1));
        users.add(new BarEntry(110f, 2));
        users.add(new BarEntry(90f, 3));
        users.add(new BarEntry(75f, 4));
        users.add(new BarEntry(185f, 5));
        users.add(new BarEntry(100f, 6));
        users.add(new BarEntry(90f, 7));
        users.add(new BarEntry(130f, 8));
        users.add(new BarEntry(35f, 9));
        users.add(new BarEntry(25f, 10));


        ArrayList hour = new ArrayList();

        hour.add("0");
        hour.add("1000");
        hour.add("2000");
        hour.add("3000");
        hour.add("4000");
        hour.add("5000");
        hour.add("6000");
        hour.add("7000");
        hour.add("8000");
        hour.add("9000");
        hour.add("10000");



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

        BarDataSet bardataset = new BarDataSet(users, "Users");
        stepsChart.animateY(5000);
        BarData data = new BarData(hour, bardataset);

        bardataset.setColors(colors);
        stepsChart.setData(data);

    }
}
