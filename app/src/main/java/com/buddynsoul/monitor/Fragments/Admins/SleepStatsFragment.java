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
import java.util.Calendar;


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
        AdminActivity activity = (AdminActivity)getActivity();
        ArrayList<UserStat> users = activity.getFinalStats();

        ArrayList hours = new ArrayList();

        hours.add("20:00");
        hours.add("21:00");
        hours.add("22:00");
        hours.add("23:00");
        hours.add("00:00");
        hours.add("01:00");
        hours.add("02:00");

        float[] results = new float[hours.size()];

        for(int i=0; i<users.size(); i++){
            int hour = getHour(users.get(i).getAsleepTime());
            if(hour <= 2)
                results[hour + 12 - 20]++;
            else{
                if(hour > 2 && hour < 20)
                    results[results.length-1] ++;
                else
                    results[hour] ++;
            }
        }

        ArrayList stats = new ArrayList();

        for(int i=0; i<results.length; i++)
            stats.add(new BarEntry(results[i], i));
        /*
        ArrayList users = new ArrayList();

        users.add(new BarEntry(50f, 0));
        users.add(new BarEntry(60f, 1));
        users.add(new BarEntry(110f, 2));
        users.add(new BarEntry(90f, 3));
        users.add(new BarEntry(75f, 4));
        users.add(new BarEntry(35f, 5));
        users.add(new BarEntry(25f, 6));
*/


        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(stats, "Users");
        asleepChart.animateY(5000);
        BarData data = new BarData(hours, bardataset);

        bardataset.setColors(colors);
        asleepChart.setData(data);

    }


    private void drawWokeupChart(){
        AdminActivity activity = (AdminActivity)getActivity();
        ArrayList<UserStat> users = activity.getFinalStats();

        ArrayList hours = new ArrayList();

        hours.add("04:00");
        hours.add("05:00");
        hours.add("06:00");
        hours.add("07:00");
        hours.add("08:00");
        hours.add("09:00");
        hours.add("10:00");

        float[] results = new float[hours.size()];

        for(int i=0; i<users.size(); i++){
            int hour = getHour(users.get(i).getAsleepTime());
            if(hour > 10)
                results[results.length-1]++;
            else
                results[hour] ++;
        }

        ArrayList stats = new ArrayList();

        for(int i=0; i<results.length; i++)
            stats.add(new BarEntry(results[i], i));

        /*
        ArrayList users = new ArrayList();

        users.add(new BarEntry(50f, 0));
        users.add(new BarEntry(60f, 1));
        users.add(new BarEntry(110f, 2));
        users.add(new BarEntry(90f, 3));
        users.add(new BarEntry(75f, 4));
        users.add(new BarEntry(35f, 5));
        users.add(new BarEntry(25f, 6));
        */


        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(stats, "Users");
        wokeUpChart.animateY(5000);
        BarData data = new BarData(hours, bardataset);

        bardataset.setColors(colors);
        wokeUpChart.setData(data);

    }

    private void drawDurationChart(){
        AdminActivity activity = (AdminActivity)getActivity();
        ArrayList<UserStat> users = activity.getFinalStats();

        ArrayList hours = new ArrayList();

        hours.add("05:00");
        hours.add("06:00");
        hours.add("07:00");
        hours.add("08:00");
        hours.add("09:00");
        hours.add("10:00");

        float[] results = new float[hours.size()];

        for(int i=0; i<users.size(); i++){
            int hour = getHour(users.get(i).getAsleepTime());
            if(hour > 10)
                results[results.length-1]++;
            else
                results[hour] ++;
        }

        ArrayList stats = new ArrayList();

        for(int i=0; i<results.length; i++)
            stats.add(new BarEntry(results[i], i));

/*
        ArrayList users = new ArrayList();

        users.add(new BarEntry(25f, 0));
        users.add(new BarEntry(145f, 1));
        users.add(new BarEntry(90f, 2));
        users.add(new BarEntry(145f, 3));
        users.add(new BarEntry(50f, 4));
        users.add(new BarEntry(30f, 5));

*/

        ArrayList colors = new ArrayList();
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimaryDark));
        colors.add(getActivity().getResources().getColor(R.color.colorPrimary));
        colors.add(getActivity().getResources().getColor(R.color.colorGreen));
        colors.add(getActivity().getResources().getColor(R.color.colorOrange));


        BarDataSet bardataset = new BarDataSet(stats, "Users");
        duration.animateY(5000);
        BarData data = new BarData(hours, bardataset);

        bardataset.setColors(colors);
        duration.setData(data);

    }

    private int getHour(long date){
        if(date == 0)
            return 0;

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);

        int hour = c.get(Calendar.HOUR_OF_DAY);
        if(c.get(Calendar.AM_PM) == Calendar.PM)
            hour += 12;

        return hour;
    }

    private int dur(int time){
        int hours = (int)time / 3600;

        return hours;
    }
}
