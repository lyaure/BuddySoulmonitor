package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        if (Build.VERSION.SDK_INT >= 26) {
            this.startForegroundService(new Intent(SplashActivity.this, StepCounterListener.class));
        } else {
            startService(new Intent(SplashActivity.this, StepCounterListener.class));
        }

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent i = new Intent(SplashActivity.this, PedometerActivity.class);
                startActivity(i);
                finish();
            }
        }, 3000);

    }
}
