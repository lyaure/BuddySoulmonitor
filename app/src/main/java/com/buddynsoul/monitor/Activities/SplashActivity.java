package com.buddynsoul.monitor.Activities;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;

import com.buddynsoul.monitor.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                boolean remember = sp.getBoolean("logged", false);

                if (remember) {
                        Intent i = new Intent(SplashActivity.this, MonitorActivity.class);
                        startActivity(i);
                        finish();
                }
                else {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                    finish();
                }
            }
        }, 3000);

    }
}
