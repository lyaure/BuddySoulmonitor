package com.buddynsoul.monitor;

import android.graphics.Color;
import android.os.Bundle;
import android.view.MenuItem;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.buddynsoul.monitor.PedometerFragment;
import com.buddynsoul.monitor.WeatherFragment;
import com.buddynsoul.monitor.CitySearchFragment;
import com.buddynsoul.monitor.SettingsFragment;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {
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
    }
    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
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
//                        case R.id.navigation_sleep:
//                            openFragment(NotificationFragment.newInstance("", ""));
//                            return true;
                        case R.id.navigation_settings:
                            fragment = new SettingsFragment();

                            Fragment tmp = getSupportFragmentManager().findFragmentById(R.id.container);
                            Bundle bundle = new Bundle();

                            if(tmp instanceof PedometerFragment)
                                bundle.putString("from", "pedometer");
                            else
                                bundle.putString("from", "weather");

                            fragment.setArguments(bundle);
                            loadFragment(fragment);
                            return true;
                    }
                    return false;
                }
            };
}