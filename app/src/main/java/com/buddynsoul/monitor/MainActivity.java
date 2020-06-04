package com.buddynsoul.monitor;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends FragmentActivity {
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        fragment = new PedometerFragment();
        loadFragment(fragment);

        //WeatherUtils.cityNameAndKeyFromLocation(MainActivity.this, MainActivity.this);

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
                        case R.id.navigation_settings:
                            fragment = new SettingsFragment();

                            Fragment tmp = getSupportFragmentManager().findFragmentById(R.id.container_ID);
                            Bundle bundle = new Bundle();

                            if(tmp instanceof PedometerFragment)
                                bundle.putString("from", "pedometer");
                            else
                                if(tmp instanceof WeatherFragment)
                                    bundle.putString("from", "weather");
                                else
                                    bundle.putString("from", "sleep");

                            fragment.setArguments(bundle);
                            loadFragment(fragment);
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}