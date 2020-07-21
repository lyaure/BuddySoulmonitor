package com.buddynsoul.monitor.Activities;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import com.buddynsoul.monitor.Objects.UserStat;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Fragments.Admins.StatisticsFragment;
import com.buddynsoul.monitor.Fragments.Admins.UsersFragment;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class AdminActivity extends FragmentActivity {

    private BottomNavigationView bottomNavigation;
    private Fragment fragment;
    private ArrayList<UserStat[]> data = new ArrayList<>();
    private ArrayList<UserStat> finalStats = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_admin);

        // bottom nav
        bottomNavigation = findViewById(R.id.bottom_navigation);
        bottomNavigation.setOnNavigationItemSelectedListener(navigationItemSelectedListener);

        fragment = new UsersFragment();
        loadFragment(fragment);

        // recover all user's data for the graphs
        getAllUserData();
    }

    // load fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.admin_container_ID, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    private BottomNavigationView.OnNavigationItemSelectedListener navigationItemSelectedListener =
            new BottomNavigationView.OnNavigationItemSelectedListener() {
                @Override public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                    switch (item.getItemId()) {
                        case R.id.navigation_users:
                            fragment = new UsersFragment();
                            loadFragment(fragment);
                            return true;

                        case R.id.navigation_statistics:
                            fragment = new StatisticsFragment();
                            loadFragment(fragment);
                            return true;
                    }
                    return false;
                }
            };

    @Override
    public void onBackPressed(){
        new AlertDialog.Builder(this)
                .setTitle("Exit")
                .setMessage("Are you sure want to exit?")
                .setPositiveButton("Exit", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // return to home
                        Intent intent = new Intent(Intent.ACTION_MAIN);
                        intent.addCategory(Intent.CATEGORY_HOME);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    }
                })
                .setNegativeButton("Go to my monitor", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        // go to monitor
                        Intent intent = new Intent(AdminActivity.this, MonitorActivity.class);
                        startActivity(intent);
                        finish();
                    }
                })
                .show();
    }

    public ArrayList<UserStat> getFinalStats() {
        return finalStats;
    }

    private void getAllUserData() {
        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);
        Call<JsonElement> todoCall = iMyService.getAllUserData(refreshToken);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                JsonArray dataArray = response.body().getAsJsonArray();

                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject user = dataArray.get(i).getAsJsonObject();
                    String userEmail = user.get("email").getAsString();

                    JsonArray userData = user.get("data").getAsJsonArray();
                    UserStat[] userStats = new UserStat[userData.size()];

                    for (int j = 0; j < userData.size(); j++) {
                        JsonObject data = userData.get(j).getAsJsonObject();
                        int steps = data.get("steps").getAsInt();
                        long asleep = data.get("asleep_time").getAsLong();
                        long wokeUp = data.get("woke_up_time").getAsLong();
                        long deepSleep = data.get("deep_sleep").getAsLong();

                        userStats[j] = new UserStat(steps, asleep, wokeUp);
                    }

                    data.add(userStats);
                }
                calculateStats();
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(getApplicationContext(), "Failed to get all users data", Toast.LENGTH_SHORT).show();
            }
        });
    }

    // add averages of each user for the graphs
    private void calculateStats(){
        for(UserStat[] stats : data){
            int steps = 0, stepsCount = 0, sleepCount = 0;
            long asleep = 0;
            long wokeUp = 0;

            for(UserStat user : stats){
                if(user.getSteps() > 0){
                    steps += user.getSteps();
                    stepsCount ++;
                }
                if(user.getAsleepTime() > 0){
                    asleep += user.getAsleepTime();
                    wokeUp =+ user.getAsleepTime();
                    sleepCount ++;
                }
            }

            int tmpSteps;
            long tmpAsleep, tmpWokeUp;

            if(stepsCount == 0) {
                tmpSteps = -1;
            }
            else {
                tmpSteps = steps/stepsCount;
            }

            if(sleepCount == 0) {
                tmpAsleep = -1;
                tmpWokeUp = -1;
            }
            else {
                tmpAsleep = asleep/sleepCount;
                tmpWokeUp = wokeUp/sleepCount;
            }
            // finalStats contain for each user a UserStat object of the user averages
            finalStats.add(new UserStat((tmpSteps), (tmpAsleep), (tmpWokeUp)));
        }
    }

    @Override
    protected void onStop(){
        super.onStop();

        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

}
