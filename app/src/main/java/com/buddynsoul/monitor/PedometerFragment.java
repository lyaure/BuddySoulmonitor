package com.buddynsoul.monitor;

import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.core.view.GestureDetectorCompat;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.functions.Consumer;
//import io.reactivex.schedulers.Schedulers;
import okhttp3.Cookie;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.Manifest;
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

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;

import org.json.JSONException;
import org.json.JSONObject;

import static android.content.Context.MODE_PRIVATE;

public class PedometerFragment extends Fragment implements SensorEventListener {
    private int todayOffset, total_start, goal, since_boot, total_days;
    private TextView steps;
    final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 42;
    private ProgressBar progSteps;
    private GraphChartView graph;
    private HorizontalScrollView hs;
    double tmp;

    //    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    public PedometerFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_pedometer, container, false);
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                boolean isGranted = false;
                ActivityCompat.requestPermissions(getActivity(),
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
                if (ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(getActivity(), "Turn on Activity Recognition to enable steps counter", Toast.LENGTH_LONG).show();
                }
                else {
                    isGranted = true;
                }

            }
        }


        if (Build.VERSION.SDK_INT >= 26) {
            getActivity().startForegroundService(new Intent(getActivity(), StepCounterListener.class));
        } else {
            getActivity().startService(new Intent(getActivity(), StepCounterListener.class));
        }

//        detector = new GestureDetectorCompat(this, this);
        steps = v.findViewById(R.id.stepTxtv_ID);
        progSteps = v.findViewById(R.id.stepProgress_ID);

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);
        int  goal = sp.getInt("goal", 10000);

        progSteps.setMax(goal);

        Database db = new Database(getActivity());
//        final int sleepingTimeDb = (int)db.getSleepingTime(Util.getToday());
//
//        Button sleepingTimeBtn = findViewById(R.id.sleepingTimeBtn_ID);
//        sleepingTimeBtn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                TextView sleepingTimeTxtv = findViewById(R.id.sleepingTimeTxtv_ID);
//                final int sleepingTime = (int)StepCounterListener.calculateSleepingTime();
//                Log.d("DebugStepCounter", "SleepingTime2: " + sleepingTime);
//                int sleepingHours = sleepingTime / 3600;
//                int sleepingMinutes = (sleepingTime % 3600) / 60;
//                int sleepingSeconds = sleepingTime % 60;
//
//                String sleepingTimeStr = sleepingHours + " Hour(s) " + sleepingMinutes + " Minute(s) " + sleepingSeconds + " Second(s)";
//                sleepingTimeTxtv.setText(sleepingTimeStr);
//
//
//                int sleepingDbHours = sleepingTimeDb / 3600;
//                int sleepingDbMinutes = (sleepingTimeDb % 3600) / 60;
//                int sleepingDbSeconds = sleepingTimeDb % 60;
//
//                TextView sleepingTimeDb = findViewById(R.id.sleepingTimeDbTxtv_ID);
//                String sleepingTimeDbStr = sleepingDbHours + " Hour(s) " + sleepingDbMinutes + " Minute(s) " + sleepingDbSeconds + " Second(s)";
//                sleepingTimeDb.setText(sleepingTimeDbStr);
//            }
//        });

        hs = (HorizontalScrollView) v.findViewById(R.id.horizontal_scrollview_ID);
        hs.setHorizontalScrollBarEnabled(false);

        DisplayMetrics dm = new DisplayMetrics();

        getActivity().getWindowManager().getDefaultDisplay().getMetrics(dm);
        int width = dm.widthPixels, height = dm.heightPixels;
        graph = (GraphChartView)v.findViewById(R.id.graph_ID);
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
                        tmp = hs.getScrollX();
                    }
                });
            }
        });

        Button changeGraph = (Button)v.findViewById(R.id.change_graph_btn_ID);
        changeGraph.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                graph.changeGraph();
                hs.fullScroll(View.FOCUS_LEFT);
            }
        });

        // Button sendDataBtn = findViewById(R.id.sendDataBtn_ID);
        // sendDataBtn.setOnClickListener(new View.OnClickListener() {
        //        @Override
        //        public void onClick(View v) {
        //            try {
        //                sendData();
        //            } catch (JSONException e) {
        //                e.printStackTrace();
        //            }
        //        }
        //     }
        // );


//        ImageButton weather_btn = (ImageButton)v.findViewById(R.id.weather_btn_ID);
//        weather_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(PedometerActivity.this, WeatherActivity.class);
//                startActivity(i);
//            }
//        });
//
//        ImageButton sleep_btn = (ImageButton)findViewById(R.id.sleep_btn_ID);
//        sleep_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        ImageButton settings_btn = (ImageButton)findViewById(R.id.settings_btn_ID);
//        settings_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(PedometerActivity.this, SettingsActivity.class);
//                i.putExtra("from", "pedometer");
//                startActivity(i);
//            }
//        });

        return v;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
//        Log.d("DebugOnSensor", "change");
//        if (sensorEvent.values[0] > Integer.MAX_VALUE || sensorEvent.values[0] == 0) {
//            return;
//        }
//        Database db = Database.getInstance(this);
//
//        if (todayOffset == Integer.MIN_VALUE) {
//            // no values for today
//            // we dont know when the reboot was, so set todays steps to 0 by
//            // initializing them with -STEPS_SINCE_BOOT
//            todayOffset = -(int) sensorEvent.values[0];
//            //db.insertNewDay(Util.getToday(), (int) sensorEvent.values[0]);
//            db.insertNewDay(Util.getToday(), todayOffset);
//
//
//        }
//        since_boot = (int) sensorEvent.values[0];
//        int a = todayOffset + (int)sensorEvent.values[0];
//        steps.setText("" + a);
//
//        db.close();

        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
                return;
            }
            if (todayOffset == Integer.MIN_VALUE) {
                // no values for today
                // we dont know when the reboot was, so set todays steps to 0 by
                // initializing them with -STEPS_SINCE_BOOT
                todayOffset = -(int) event.values[0];
//                todayOffset = (int) event.values[0];
                //Database db = Database.getInstance(this);
                Database db = new Database(getActivity());
                db.insertNewDay(Util.getToday(), (int) event.values[0]);
                //db.close();
            }
            since_boot = (int) event.values[0];
            //updatePie();
            int steps_today = Math.max(todayOffset + since_boot, 0);
            if(Build.VERSION.SDK_INT >= 24)
                progSteps.setProgress(steps_today, true);
            else
                progSteps.setProgress(steps_today);
            steps.setText(String.valueOf(steps_today) + "\nsteps");
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();



       // this.getActionBar().setDisplayHomeAsUpEnabled(false);

        //Database db = Database.getInstance(this);
        Database db = new Database(getActivity());

        //if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);

//        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
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

//        sensor = sm.getDefaultSensor(Sensor.TYPE_LIGHT);
//        if (sensor == null) {
//            new AlertDialog.Builder(this).setTitle(R.string.no_sensor)
//                    .setMessage(R.string.no_light_sensor_explain)
//                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
//                        @Override
//                        public void onDismiss(final DialogInterface dialogInterface) {
//                            finish();
//                        }
//                    }).setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
//                @Override
//                public void onClick(final DialogInterface dialogInterface, int i) {
//                    dialogInterface.dismiss();
//                }
//            }).create().show();
//        } else {
//            sm.registerListener(this, sensor, SensorManager.SENSOR_DELAY_UI, 0);
//        }

        since_boot -= pauseDifference;

        total_start = db.getTotalWithoutToday();
        total_days = db.getDays();

        //db.close();

        //stepsDistanceChanged();
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
        //Database db = Database.getInstance(this);
        Database db = new Database(getActivity());
        db.saveCurrentSteps(since_boot);
        //db.close();
    }

    private void sendData() throws JSONException {

        SharedPreferences sp = this.getActivity().getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");
//        refreshToken = refreshToken.substring(1, refreshToken.length()-1);




        // Init Service
//        Retrofit retrofitClient = RetrofitClient.getInstance();
//        iMyService = retrofitClient.create(IMyService.class);

        String dataToSend = preprocessData();

//        compositeDisposable.add(iMyService.sendData(refreshToken, dataToSend)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String response) throws Exception {
//                        if (!response.equals("\"Wrong password\"")) {
//                            SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sp.edit();
//                            editor.putBoolean("sendToServer", true);
//                            editor.commit();
//
//                        }
//                        else {
//                            Toast.makeText(PedometerActivity.this, ""+response, Toast.LENGTH_SHORT).show();
//                        }
//
//                    }
//                }));

        Call<String> todoCall = iMyService.sendData(refreshToken, dataToSend);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
//                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
//                SharedPreferences.Editor editor = sp.edit();
//                editor.putBoolean("sendToServer", true);
//                editor.commit();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private String preprocessData() throws JSONException {

        SharedPreferences sp = this.getActivity().getSharedPreferences("user", MODE_PRIVATE);

        String timestamps = "" + Util.getToday();
        Database db = new Database(this.getActivity());
        String steps = ""+ db.getSteps(Util.getToday());
        String sleepingTime = "" + db.getSleepingTime(Util.getToday());
        String morning_location = "" + db.getLocation(Util.getToday(), "morning_location");
        String night_location = "" + db.getLocation(Util.getToday(), "night_location");


        JSONObject json = new JSONObject();
        json.put("timestamps", timestamps);
        json.put("steps", steps);
        json.put("sleeping_time", sleepingTime);
        json.put("morning_location", morning_location);
        json.put("night_location", night_location);

        return json.toString();
    }

}
