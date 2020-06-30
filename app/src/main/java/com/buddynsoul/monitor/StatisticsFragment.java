package com.buddynsoul.monitor;

import android.graphics.Color;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.utils.ColorTemplate;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import java.util.ArrayList;
import java.util.List;


public class StatisticsFragment extends Fragment {
    View v;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_statistics, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.pedometer_viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        DotsIndicator dotsIndicator = (DotsIndicator)v.findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        StepsStatsFragment steps = new StepsStatsFragment();
        SleepStatsFragment sleep = new SleepStatsFragment();

        adapter.addFrag(steps, "current");
        adapter.addFrag(sleep, "recent");
        adapter.notifyDataSetChanged();
        return v;
    }
}
