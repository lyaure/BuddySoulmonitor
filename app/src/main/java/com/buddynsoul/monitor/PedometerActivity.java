package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GestureDetectorCompat;

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
import android.view.GestureDetector;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

public class PedometerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener, SensorEventListener {
    private GestureDetectorCompat detector;
    private int todayOffset, total_start, goal, since_boot, total_days;
    private TextView steps;
    final int MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION = 42;
    private ProgressBar progSteps;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);


        if (Build.VERSION.SDK_INT >= 29) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permission is not granted
                boolean isGranted = false;
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACTIVITY_RECOGNITION},
                        MY_PERMISSIONS_REQUEST_ACTIVITY_RECOGNITION);
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION)
                        != PackageManager.PERMISSION_GRANTED) {
                    Toast.makeText(this, "Turn on Activity Recognition to enable steps counter", Toast.LENGTH_LONG).show();
                }
                else {
                    isGranted = true;
                }

            }
        }


        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(new Intent(this, StepCounterListener.class));
        } else {
            startService(new Intent(this, StepCounterListener.class));
        }

        detector = new GestureDetectorCompat(this, this);
        steps = findViewById(R.id.stepTxtv_ID);
        progSteps = findViewById(R.id.stepProgress_ID);

        SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
        String  goal = sp.getString("stepGoal", "10000");

        progSteps.setMax(Integer.parseInt(goal.trim()));



//        Database db = Database.getInstance(this);
//
//        int a = db.getSteps(Util.getToday());
//        steps.setText("" + a);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        // menu creation
        super.onCreateOptionsMenu(menu);
        MenuItem settings = menu.add("Settings");

        settings.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                Intent i = new Intent(PedometerActivity.this, SettingActivity.class);
                i.putExtra("activity", 1);
                startActivity(i); // open rules activity
                return true;
            }
        });

        return true;
    }


    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }

    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(e1.getX() > e2.getX()){
            Intent i = new Intent(PedometerActivity.this, WeatherActivity.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
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

        if (event.values[0] > Integer.MAX_VALUE || event.values[0] == 0) {
            return;
        }
        if (todayOffset == Integer.MIN_VALUE) {
            // no values for today
            // we dont know when the reboot was, so set todays steps to 0 by
            // initializing them with -STEPS_SINCE_BOOT
            todayOffset = -(int) event.values[0];
            //Database db = Database.getInstance(this);
            Database db = new Database(this);
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

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    public void onResume() {
        super.onResume();

       // this.getActionBar().setDisplayHomeAsUpEnabled(false);

        //Database db = Database.getInstance(this);
        Database db = new Database(this);

        //if (BuildConfig.DEBUG) db.logState();
        // read todays offset
        todayOffset = db.getSteps(Util.getToday());

        SharedPreferences prefs =
                this.getSharedPreferences("pedometer", Context.MODE_PRIVATE);

//        goal = prefs.getInt("goal", Fragment_Settings.DEFAULT_GOAL);
        since_boot = db.getCurrentSteps();

        int pauseDifference = since_boot - prefs.getInt("pauseCount", since_boot);

        // register a sensorlistener to live update the UI if a step is taken
        SensorManager sm = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor sensor = sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);
        if (sensor == null) {
            new AlertDialog.Builder(this).setTitle(R.string.no_sensor)
                    .setMessage(R.string.no_step_sensor_explain)
                    .setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(final DialogInterface dialogInterface) {
                            finish();
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
                    (SensorManager) this.getSystemService(Context.SENSOR_SERVICE);
            sm.unregisterListener(this);
        } catch (Exception e) {
            //if (BuildConfig.DEBUG) Logger.log(e);
        }
        //Database db = Database.getInstance(this);
        Database db = new Database(this);
        db.saveCurrentSteps(since_boot);
        //db.close();
    }

}
