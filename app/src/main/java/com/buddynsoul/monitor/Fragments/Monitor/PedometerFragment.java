package com.buddynsoul.monitor.Fragments.Monitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buddynsoul.monitor.Adapters.ViewPagerAdapter;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class PedometerFragment extends Fragment {
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    public PedometerFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_pedometer, container, false);

        // multiple fragments in this fragment
        viewPager = (ViewPager) v.findViewById(R.id.pedometer_viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        // indicates number of fragments
        DotsIndicator dotsIndicator = (DotsIndicator)v.findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        PedometerCurrentFragment current = new PedometerCurrentFragment();
        PedometerRecentFragment recent = new PedometerRecentFragment();

        // add fragments
        adapter.addFrag(current, "current");
        adapter.addFrag(recent, "recent");

        adapter.notifyDataSetChanged();


        Database db = Database.getInstance(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("goalAchieved", getActivity().MODE_PRIVATE);

        if(!sp.contains("showStepGoalAchieved")){
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("showStepGoalAchieved", Util.getToday());
            editor.apply();
        }

        int yesterday = db.getStepGoal(Util.getYesterday());

        // congrats message for yesterday goal achieved
        if((sp.getLong("showStepGoalAchieved", Util.getToday()) != Util.getToday()
                && db.getSteps(Util.getYesterday()) >= yesterday
                && yesterday != Integer.MIN_VALUE && yesterday != -1)){
            new AlertDialog.Builder(getActivity())
                    .setTitle("GOOD JOB!!")
                    .setMessage("Congratulation, you achieved your daily goal yesterday!\nKepp going!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(R.drawable.icon)
                    .show();

            // save that we already show the message
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("showStepGoalAchieved", Util.getToday());
            editor.apply();
        }

        return v;
    }
}
