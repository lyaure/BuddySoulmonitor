package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;
import androidx.core.view.GestureDetectorCompat;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.widget.TextView;

public class PedometerActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_pedometer);

        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(new Intent(this, StepCounterListener.class));
        } else {
            startService(new Intent(this, StepCounterListener.class));
        }

        detector = new GestureDetectorCompat(this, this);
        TextView steps = findViewById(R.id.steps_ID);
        Database db = Database.getInstance(this);

        int a = db.getSteps(Util.getToday());
        steps.setText("" + a);
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
}
