package com.buddynsoul.monitor;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.buddynsoul.monitor.Utils.WeatherUtils;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends FragmentActivity {
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private int fragmentID;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        fragment = new PedometerFragment();
        loadFragment(fragment);

    }
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_ID, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_pedometer:
                            fragment = new PedometerFragment();
                            loadFragment(fragment);
                            return true;
                        case R.id.navigation_weather:
                            fragment = new WeatherFragment();
                            loadFragment(fragment);
                            return true;
                        case R.id.navigation_sleep:
                            fragment = new SleepingTimeFragment();
                            loadFragment(fragment);
                            return true;
                        case R.id.navigation_profile:
                            fragment = new ProfileFragment();
                            loadFragment(fragment);
                            return true;
                    }
                    return false;
                }
            };

    public void setFragmentID(int id){
        this.fragmentID = id;
    }

    @Override
    public void onBackPressed(){
        if(fragmentID == R.layout.fragment_setting || fragmentID == R.layout.fragment_contact_us){
            fragment = new ProfileFragment();
            loadFragment(fragment);
        }
        else {
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}