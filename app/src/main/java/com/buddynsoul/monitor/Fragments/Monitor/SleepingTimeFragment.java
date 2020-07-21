package com.buddynsoul.monitor.Fragments.Monitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;

import com.buddynsoul.monitor.Adapters.ViewPagerAdapter;
import com.buddynsoul.monitor.GraphChartView;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

public class SleepingTimeFragment extends Fragment {
    private final int SLEEP = 1;
    private View v;
    private GraphChartView graph;
    private HorizontalScrollView hs;
    private Database db;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;
    private SleepSummaryFragment summary;

    public SleepingTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_sleeping_time, container, false);

        db = Database.getInstance(getContext());


        viewPager = (ViewPager) v.findViewById(R.id.sleep_viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        DotsIndicator dotsIndicator = (DotsIndicator) v.findViewById(R.id.sleep_dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        summary = new SleepSummaryFragment();
        SleepGoalFragment goals = new SleepGoalFragment();

        adapter.addFrag(summary, "summary");
        adapter.addFrag(goals, "goal");
        adapter.notifyDataSetChanged();


        hs = (HorizontalScrollView) v.findViewById(R.id.sleepingTime_horizontal_scrollview_ID);
        hs.setHorizontalScrollBarEnabled(false);

        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels, height = dm.heightPixels;
        graph = (GraphChartView) v.findViewById(R.id.sleepingTime_graph_ID);
        graph.setType(SLEEP);
        graph.setScreenDimensions(width, height);

        hs.post(new Runnable() {
            @Override
            public void run() {
                ViewTreeObserver observer = hs.getViewTreeObserver();
                observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        graph.setScrollPosition(hs.getScrollX());
                        summary.update(graph);
                    }
                });
            }
        });

        Button changeGraph = (Button) v.findViewById(R.id.sleepingTime_change_graph_btn_ID);
        changeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.changeGraph();
                hs.fullScroll(View.FOCUS_LEFT);
            }
        });

        SharedPreferences sp = getActivity().getSharedPreferences("goalAchieved", getActivity().MODE_PRIVATE);

        if (!sp.contains("showSleepGoalAchieved")) {
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("showSleepGoalAchieved", Util.getToday());
            editor.commit();
        }

        int yesterday = db.getSleepGoal(Util.getYesterday());

        if (sp.getLong("showSleepGoalAchieved", Util.getToday()) != Util.getToday()
                && db.getSleepDuration(Util.getYesterday()) >= yesterday
                && yesterday != Integer.MIN_VALUE && yesterday != -1) {
            new AlertDialog.Builder(getActivity())
                    .setTitle("GOOD JOB!!")
                    .setMessage("Congratulation, you achieved your goal this night!\nKepp going!")
                    .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {

                        }
                    })
                    .setIcon(R.drawable.icon)
                    .show();
            SharedPreferences.Editor editor = sp.edit();
            editor.putLong("showSleepGoalAchieved", Util.getToday());
            editor.commit();
        }

        return v;
    }

    @Override
    public void onResume(){
        super.onResume();
        summary.setGraph(graph);
    }

}
