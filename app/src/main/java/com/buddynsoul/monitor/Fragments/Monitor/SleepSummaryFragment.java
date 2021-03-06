package com.buddynsoul.monitor.Fragments.Monitor;

import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.buddynsoul.monitor.GraphChartView;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;

import java.util.Calendar;
import java.util.Locale;

public class SleepSummaryFragment extends Fragment {
    private View v;
    private TextView date, asleep, wokeUp, deepSleep, lightSleep, duration, average;
    private static GraphChartView graph;

    public SleepSummaryFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_sleep_summary, container, false);

        // init views
        date = (TextView)v.findViewById(R.id.date_txtv_ID);
        date.setVisibility(View.INVISIBLE);
        asleep = (TextView)v.findViewById(R.id.asleep_txtv_ID);
        wokeUp = (TextView)v.findViewById(R.id.wokeUpe_txtv_ID);
        deepSleep = (TextView)v.findViewById(R.id.deepSleep_txtv_ID);
        lightSleep = (TextView)v.findViewById(R.id.lightSleep_txtv_ID);
        duration = (TextView)v.findViewById(R.id.duration_txtv_ID);
        average = (TextView)v.findViewById(R.id.average_txtv_ID);

        // update the graph
        update(graph);

        return v;
    }

    // convert date to String
    private String getTime(long date){
        if(date == 0)
            return "No data for this day";

        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(date);

        String time = String.format("%02d", c.get(Calendar.HOUR_OF_DAY)) + ":" + String.format("%02d", c.get(Calendar.MINUTE));
        time += c.get(Calendar.AM_PM) == Calendar.AM ? " am" : " pm";

        return time;
    }

    // convert time to String
    // time in millisec
    private String getSleepingTime(int time){
        int hours = (int)time / 3600;
        int minutes = ((int)time % 3600) / 60;

        return hours + "h" + String.format("%02d", minutes);
    }

    // updates views in onCreate and after scroll position changed
    public void update(GraphChartView graph){
        Database db  = Database.getInstance(getActivity());
        long time;
        int count = db.getSleepingTimeDatesCount() >= 7 ? 7 : db.getSleepingTimeDatesCount();

        if(count > 0){
            date.setVisibility(View.VISIBLE);

            Calendar c = Calendar.getInstance();
            c.setTimeInMillis(graph.getDatePosition());

            String d = c.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()) + ", "
                    + c.get(Calendar.DAY_OF_MONTH) + " "
                    + c.getDisplayName(Calendar.MONTH, Calendar.LONG, Locale.getDefault()) + " "
                    + c.get(Calendar.YEAR);

            date.setText(d);

            // recover data from SQLite local database

            time = db.getWokeUp(graph.getDatePosition());
            if(time >= 0)
                wokeUp.setText(getTime(time));


            time = db.getAsleep(graph.getDatePosition());
            if(time >= 0)
                asleep.setText(getTime(time));

            int dur = db.getSleepDuration(graph.getDatePosition());

            if(dur >= 0)
                duration.setText(getSleepingTime((int)dur));

            int deep = db.getDeepSleep(graph.getDatePosition());
            if(deep >= 0)
                deepSleep.setText(getSleepingTime(deep));

            int light = db.getLightSleep(graph.getDatePosition());
            if(light >= 0)
                lightSleep.setText(getSleepingTime(light));

            int avrg = db.getSleepingTimes(Util.getSpecificDate(7), Util.getYesterday()) / count;
            average.setText(getSleepingTime(avrg));
        }

    }

    public void setGraph(GraphChartView g){
        this.graph = g;
    }
}
