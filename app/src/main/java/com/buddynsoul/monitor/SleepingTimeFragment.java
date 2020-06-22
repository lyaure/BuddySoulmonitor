package com.buddynsoul.monitor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.TextView;

import com.buddynsoul.monitor.Utils.Util;

import java.util.Calendar;

public class SleepingTimeFragment extends Fragment {
    private final int SLEEP = 1;
    View v;
    private GraphChartView graph;
    private HorizontalScrollView hs;
    private TextView asleep, wokeUp, deepSleep, lightSleep, duration, average;
    private Database db;

    public SleepingTimeFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_sleeping_time, container, false);

        db = Database.getInstance(getContext());

        hs = (HorizontalScrollView) v.findViewById(R.id.sleepingTime_horizontal_scrollview_ID);
        hs.setHorizontalScrollBarEnabled(false);

        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels, height = dm.heightPixels;
        graph = (GraphChartView)v.findViewById(R.id.sleepingTime_graph_ID);
        graph.setType(SLEEP);
        graph.setScreenDimensions(width, height);


        asleep = (TextView)v.findViewById(R.id.asleep_txtv_ID);
        wokeUp = (TextView)v.findViewById(R.id.wokeUpe_txtv_ID);
        deepSleep = (TextView)v.findViewById(R.id.deepSleep_txtv_ID);
        lightSleep = (TextView)v.findViewById(R.id.lightSleep_txtv_ID);
        duration = (TextView)v.findViewById(R.id.duration_txtv_ID);
        average = (TextView)v.findViewById(R.id.average_txtv_ID);

        update();

        hs.post(new Runnable() {
            @Override
            public void run() {
                ViewTreeObserver observer = hs.getViewTreeObserver();
                observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        graph.setScrollPosition(hs.getScrollX());
//                        tmp = hs.getScrollX();
//                        asleep.setText(getTime(db.getAsleep(graph.getDatePosition())));
//                        wokeUp.setText(getTime(db.getWokeUp(graph.getDatePosition())));
//                        duration.setText(getSleepingTime(db.getSleepingTime(graph.getDatePosition())));
                        update();
                    }
                });
            }
        });

        Button changeGraph = (Button)v.findViewById(R.id.sleepingTime_change_graph_btn_ID);
        changeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.changeGraph();
                hs.fullScroll(View.FOCUS_LEFT);
            }
        });


//        TextView data = (TextView)v.findViewById(R.id.stat_data_ID);
//
//        SharedPreferences sp = getActivity().getSharedPreferences("tempData", getActivity().MODE_PRIVATE);
//        long statData = sp.getLong("stat", -1);
//        if(statData != -1)
//            data.setText(String.valueOf(statData));
//
//        TextView sleeping_time_txtv = (TextView)v.findViewById(R.id.sleeping_time_ID);
//
//        int hour = 0, min = 0, sec = 0;
//
//        int sleepingTime = sp.getInt("sleepingTime", -1);
//        if (sleepingTime != -1)
//            hour = sleepingTime / 3600;
//            min = (sleepingTime  % 3600 ) / 60;
//            sec = ((sleepingTime % 86400 ) % 3600 ) % 60;
//        sleeping_time_txtv.setText(String.format("%s\n\n%d hour %d min %d sec", String.valueOf(sleepingTime), hour, min, sec));

        return v;
    }

    private String getTime(long date){
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);

        String time = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", c.get(Calendar.MINUTE));
        time += c.get(Calendar.AM_PM) == Calendar.AM ? " am" : " pm";

        return time;
    }

    private String getSleepingTime(int time){
        int hours = (int)time / 3600;
        int minutes = ((int)time % 3600) / 60;

        return hours + "h" + String.format("%02d", minutes);
    }

    private void update(){
        long time;
        int count = db.getSleepingTimeDatesCount() >= 7 ? 7 : db.getSleepingTimeDatesCount();

        if(count > 0){
            time = db.getWokeUp(graph.getDatePosition());
            if(time >= 0)
                wokeUp.setText(getTime(time));


            time = db.getAsleep(graph.getDatePosition());
            if(time >= 0)
                asleep.setText(getTime(time));

            int dur = db.getSleepDuration(graph.getDatePosition());
            dur /= 1000; // millisec to sec

            if(dur >= 0)
                duration.setText(getSleepingTime((int)dur));

            int deep = db.getDeepSleep(graph.getDatePosition());
            if(deep >= 0)
                deepSleep.setText(getSleepingTime(deep));

            int light = db.getLightSleep(graph.getDatePosition());
            light /= 1000;
            if(light >= 0)
                lightSleep.setText(getSleepingTime(light));

            int avrg = db.getSleepingTimes(Util.getSpecificDate(7), Util.getYesterday()) / count;
            avrg /= 1000;
            average.setText(getSleepingTime(avrg));
        }

    }
}
