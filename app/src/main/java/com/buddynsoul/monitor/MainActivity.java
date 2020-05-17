package com.buddynsoul.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

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

        cityNameAndKeyFromLocation();

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
                        case R.id.navigation_sleep:
                            fragment = new SleepingTimeFragment();
                            loadFragment(fragment);
                            return true;
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

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
        finish();
    }

    private Boolean cityNameAndKeyFromLocation() {

        final String API_KEY = getResources().getString(R.string.accuweather_api_key);

        IMyService iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);

        // get the last location
        String localisation = getLocation.getLastLocation(MainActivity.this, this);

        if (localisation.equals("")) {
            return false;
        }

        // send geoposition request to the api
        Call<JsonElement> todoCall = iMyService.geoposition(API_KEY, localisation);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //response.isSuccessful()
                if (response.code() == 200) {
                    String keyValue = ((JsonObject) response.body()).get("Key").toString();
                    keyValue = keyValue.substring(1, keyValue.length() - 1);

                    String cityName = ((JsonObject) response.body()).get("EnglishName").toString();
                    cityName = cityName.substring(1, cityName.length() - 1);

                    //Save to sharedPreference
                    SharedPreferences sp = MainActivity.this.getSharedPreferences("Weather", MainActivity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("keyValue", keyValue);
                    editor.putString("cityName", cityName);
                    editor.commit();
                }
                else {
                    Toast.makeText(MainActivity.this, "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(MainActivity.this, "change Api Key", Toast.LENGTH_LONG).show();
            }
        });
        return true;
    }
}