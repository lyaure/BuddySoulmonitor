package com.buddynsoul.monitor.Fragments.Monitor;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.Manifest;
import android.animation.ObjectAnimator;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.HorizontalScrollView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.GraphChartView;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.MonitorService;
import com.buddynsoul.monitor.Utils.Util;
import com.buddynsoul.monitor.Utils.WeatherUtils;

import java.util.ArrayList;
import java.util.List;

import static android.content.Context.MODE_PRIVATE;

public class PedometerCurrentFragment extends Fragment implements SensorEventListener {
    private final int PERMISSION_REQUEST_CODE = 10;
    private final int PEDOMETER = 0;
    private int todayOffset, total_start, goal, since_boot, total_days;
    private TextView steps;
    private TextView distance_txtv;
    private ProgressBar progSteps;
    private GraphChartView graph;
    private HorizontalScrollView hs;
    final double stepToKilometre = 0.000762;

    IMyService iMyService;

    public PedometerCurrentFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pedometer_current, container, false);
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        // Request all the permissions
        requestPermissions();

        // get the location and the weather data
        WeatherUtils.cityNameAndKeyFromLocation(getActivity(), getActivity());


        if (Build.VERSION.SDK_INT >= 26) {
            getActivity().startForegroundService(new Intent(getActivity(), MonitorService.class));
        } else {
            getActivity().startService(new Intent(getActivity(), MonitorService.class));
        }

        steps = v.findViewById(R.id.stepTxtv_ID);
        distance_txtv = v.findViewById(R.id.distanceTxtv_ID);
        progSteps = v.findViewById(R.id.stepProgress_ID);
        progSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeVisibility(steps);
                changeVisibility(distance_txtv);
            }
        });

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);

        int  goal = sp.getInt("goal", 10000);

        progSteps.setMax(goal);

        hs = (HorizontalScrollView) v.findViewById(R.id.pedometer_horizontal_scrollview_ID);
        hs.setHorizontalScrollBarEnabled(false);

        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels, height = dm.heightPixels;
        graph = (GraphChartView)v.findViewById(R.id.pedometer_graph_ID);
        graph.setType(PEDOMETER);
        graph.setScreenDimensions(width, height);
        graph.setGoal(goal);

        hs.post(new Runnable() {
            @Override
            public void run() {
                ViewTreeObserver observer = hs.getViewTreeObserver();
                observer.addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
                        graph.setScrollPosition(hs.getScrollX());
                    }
                });
            }
        });

        Button changeGraph = (Button)v.findViewById(R.id.pedometer_change_graph_btn_ID);
        changeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.changeGraph();
                hs.fullScroll(View.FOCUS_LEFT);
            }
        });

        if (!Util.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
        }

        return v;
    }

    private void changeVisibility(View v){
        if(v.getVisibility() == View.VISIBLE)
            v.setVisibility(View.INVISIBLE);
        else
            v.setVisibility(View.VISIBLE);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
                return;
            }
            if (todayOffset == Integer.MIN_VALUE) {
                // no values for today
                // we don't know when the reboot was, so set todays steps to 0 by
                // initializing them with -STEPS_SINCE_BOOT
                todayOffset = -(int) event.values[0];
                Database db = Database.getInstance(getActivity());
                db.insertNewDay(Util.getToday(), (int) event.values[0]);
            }
            since_boot = (int) event.values[0];
            int steps_today = Math.max(todayOffset + since_boot, 0);
            if(Build.VERSION.SDK_INT >= 24) {
                ObjectAnimator.ofInt(progSteps, "progress", steps_today)
                        .setDuration(3000)
                        .start();
            }

            else {
                progSteps.setProgress(steps_today);
            }
            steps.setText(String.valueOf(steps_today + "\nsteps"));
            double distance = steps_today * stepToKilometre;
            distance = Math.round(distance * 1000d) / 1000d;
            distance_txtv.setText(String.valueOf(distance + "\nKm"));
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();

        Database db = Database.getInstance(getActivity());
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);

        since_boot = db.getCurrentSteps();

        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) {
            new AlertDialog.Builder(getActivity()).setTitle(R.string.no_sensor)
                    .setMessage(R.string.no_step_sensor_explain)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            getActivity().finish();
                        }
                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(final DialogInterface dialogInterface, int i) {
                    dialogInterface.dismiss();
                }
            }).create().show();
        } else {
            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();
    }

    @Override
    public void onPause() {
        super.onPause();
        try {
            SensorManager sm =
                    (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            //if (BuildConfig.DEBUG) Logger.log(e);
        }
        Database db = Database.getInstance(getActivity());
        db.saveCurrentSteps(since_boot);
    }

    private void requestPermissions() {

        List<String> appPermissions = new ArrayList<>();

        if (Build.VERSION.SDK_INT >= 29) {
            appPermissions.add(Manifest.permission.ACTIVITY_RECOGNITION);
        }
        appPermissions.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        appPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION);

        // Check which permissions are granted
        List<String> listPermissionsNeeded = new ArrayList<>();
        for(String perm: appPermissions)
        {
            if (ContextCompat.checkSelfPermission(getActivity(), perm) != PackageManager.PERMISSION_GRANTED) {
                listPermissionsNeeded.add(perm);
            }
        }

        // Ask for non-granted permissions
        if(!listPermissionsNeeded.isEmpty()) {
            String[] listPermissions = listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]);
            ActivityCompat.requestPermissions(getActivity(), listPermissions, PERMISSION_REQUEST_CODE);
        }
    }

}
