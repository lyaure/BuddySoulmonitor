package com.buddynsoul.monitor.Fragments.Admins;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.buddynsoul.monitor.Adapters.ViewPagerAdapter;
import com.buddynsoul.monitor.Objects.UserStat;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;


public class StatisticsFragment extends Fragment {
    private View v;
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
