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


public class SleepStatsFragment extends Fragment {
    View v;
    BarChart asleepChart, wokeUpChart, duration;
    public SleepStatsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_sleep_stats, container, false);

        asleepChart = (BarChart)v.findViewById(R.id.asleep_barchart_ID);
        wokeUpChart = (BarChart)v.findViewById(R.id.wokeup_barchart_ID);
        duration = (BarChart)v.findViewById(R.id.duration_barchart_ID);

        drawAsleepChart();
        drawWokeupChart();
        drawDurationChart();

        return v;
    }

    private void drawAsleepChart(){
        ArrayList users = new ArrayList();

        users.add(new BarEntry(50f, 0));
        users.add(new BarEntry(60f, 1));
        users.add(new BarEntry(110f, 2));
        users.add(new BarEntry(90f, 3));
        users.add(new BarEntry(75f, 4));
        users.add(new BarEntry(35f, 5));
        users.add(new BarEntry(25f, 6));


        ArrayList hour = new ArrayList();

        hour.add("20:00");
        hour.add("21:00");
        hour.add("22:00");
        hour.add("23:00");
        hour.add("00:00");
        hour.add("01:00");
        hour.add("02:00");



        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(users, "Users");
        asleepChart.animateY(5000);
        BarData data = new BarData(hour, bardataset);

        bardataset.setColors(colors);
        asleepChart.setData(data);

    }


    private void drawWokeupChart(){


        ArrayList users = new ArrayList();

        users.add(new BarEntry(50f, 0));
        users.add(new BarEntry(60f, 1));
        users.add(new BarEntry(110f, 2));
        users.add(new BarEntry(90f, 3));
        users.add(new BarEntry(75f, 4));
        users.add(new BarEntry(35f, 5));
        users.add(new BarEntry(25f, 6));


        ArrayList hour = new ArrayList();

        hour.add("04:00");
        hour.add("05:00");
        hour.add("06:00");
        hour.add("07:00");
        hour.add("08:00");
        hour.add("09:00");
        hour.add("10:00");



        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(users, "Users");
        wokeUpChart.animateY(5000);
        BarData data = new BarData(hour, bardataset);

        bardataset.setColors(colors);
        wokeUpChart.setData(data);

    }

    private void drawDurationChart(){


        ArrayList users = new ArrayList();

        users.add(new BarEntry(25f, 0));
        users.add(new BarEntry(145f, 1));
        users.add(new BarEntry(90f, 2));
        users.add(new BarEntry(145f, 3));
        users.add(new BarEntry(50f, 4));
        users.add(new BarEntry(30f, 5));


        ArrayList hour = new ArrayList();

        hour.add("05:00");
        hour.add("06:00");
        hour.add("07:00");
        hour.add("08:00");
        hour.add("09:00");
        hour.add("10:00");



        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(users, "Users");
        duration.animateY(5000);
        BarData data = new BarData(hour, bardataset);

        bardataset.setColors(colors);
        duration.setData(data);

    }
}
