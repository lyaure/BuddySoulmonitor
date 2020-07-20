package com.buddynsoul.monitor.Activities;

import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import com.buddynsoul.monitor.Fragments.Monitor.PedometerFragment;
import com.buddynsoul.monitor.Fragments.Monitor.ProfileFragment;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Fragments.Monitor.SleepingTimeFragment;
import com.buddynsoul.monitor.Fragments.Monitor.TodayGoalsFragment;
import com.buddynsoul.monitor.Fragments.Monitor.WeatherFragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MonitorActivity extends FragmentActivity {
    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private int fragmentID;
    private boolean adminButtonPressed, logoutButtonPressed;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        adminButtonPressed = false;
        logoutButtonPressed = false;

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
                        case R.id.navigation_sleep:
                            fragment = new SleepingTimeFragment();
                            loadFragment(fragment);
                            return true;
                        case R.id.navigation_weather:
                            fragment = new WeatherFragment();
                            loadFragment(fragment);
                            return true;
                        case R.id.navigation_todayGoals:
                            fragment = new TodayGoalsFragment();
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

    public void setAdminButtonPressed(){
        this.adminButtonPressed = true;
    }

    public void setLogoutButtonPressed() {
        this.logoutButtonPressed = true;
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

    @Override
    protected void onStop(){
        super.onStop();

        if(fragmentID != R.layout.fragment_profile && this.adminButtonPressed == false && this.logoutButtonPressed == false){
            Intent intent = new Intent(Intent.ACTION_MAIN);
            intent.addCategory(Intent.CATEGORY_HOME);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        }
    }
}