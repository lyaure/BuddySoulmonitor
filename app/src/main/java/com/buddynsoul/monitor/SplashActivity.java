package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

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
                boolean admin = sp.getBoolean("admin", false);
                Class activityToLaunch;

                if (remember) {
                    if (admin) {
                        new AlertDialog.Builder(SplashActivity.this)
                                .setTitle("Admin")
                                .setMessage("Do you want to access your admin account?")
                                .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(SplashActivity.this, AdminActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                })
                                .setNegativeButton("No", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                                        startActivity(i);
                                        finish();
                                    }
                                })
                                .show();
                    }
                    else {
                        Intent i = new Intent(SplashActivity.this, MainActivity.class);
                        startActivity(i);
                    }
                }
                else {
                    Intent i = new Intent(SplashActivity.this, LoginActivity.class);
                    startActivity(i);
                }
            }
        }, 3000);

    }
}
