package com.buddynsoul.monitor;

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
    BottomNavigationView bottomNavigation;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);
        loadFragment(new PedometerFragment());
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
                            loadFragment(new PedometerFragment());
                            return true;
                        case R.id.navigation_weather:
                            loadFragment(new WeatherFragment());
                            return true;
//                        case R.id.navigation_sleep:
//                            openFragment(NotificationFragment.newInstance("", ""));
//                            return true;
                        case R.id.navigation_settings:
                            loadFragment(new SettingsFragment());
                            return true;
                    }
                    return false;
                }
            };
}