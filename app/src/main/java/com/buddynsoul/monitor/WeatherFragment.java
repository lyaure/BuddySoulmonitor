package com.buddynsoul.monitor;

import androidx.core.view.GestureDetectorCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.io.IOException;
import java.lang.Math;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherFragment extends Fragment {
    private static final long MIN_TIME_FOR_UPDATE = 0;
    private static final float MIN_DIS_FOR_UPDATE = 0;
    final int PERMISSION_ID = 42;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String localisation;
    private GestureDetectorCompat detector;
    private URL builtUri;
    private String response = "", keyValue = null;
    private TextView city;
    private String metricValue;
    private View v;

    private String API_KEY;
    private IMyService iMyService;

    private ArrayList<ImageView> dIconForecast = new ArrayList<>();
    private ArrayList<TextView> dForecast = new ArrayList<>();
    private ArrayList<TextView> daysTextView = new ArrayList<>();
    private ArrayList<ImageView> hIconForecast = new ArrayList<>();
    private ArrayList<TextView> hForecast = new ArrayList<>();
    private ArrayList<TextView> hours = new ArrayList<>();


    public WeatherFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_weather, container, false);

        if (!Util.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
            return v;
        }

        iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);

        API_KEY = getResources().getString(R.string.accuweather_api_key);


        SharedPreferences prefs = getActivity().getSharedPreferences("Settings", getActivity().MODE_PRIVATE);
        metricValue = prefs.getString("metricValue", "true");

        for (int i = 1; i <= 5; i++) {

            String id_str = "dIconForecast" + i + "_ID";
            int id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
            ImageView tmpImgView = (ImageView) v.findViewById(id);
            dIconForecast.add(tmpImgView);

            id_str = "dForecast" + i + "_ID";
            id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
            TextView tmpTxtView = (TextView) v.findViewById(id);
            dForecast.add(tmpTxtView);

            if (i < 5) {
                int tmp_index = i+1;
                id_str = "day" + tmp_index + "_ID";
                id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
                tmpTxtView = (TextView) v.findViewById(id);
                daysTextView.add(tmpTxtView);

                id_str = "hIconForecast" + i + "_ID";
                id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
                tmpImgView = (ImageView) v.findViewById(id);
                hIconForecast.add(tmpImgView);

                id_str = "hForecast" + i + "_ID";
                id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
                tmpTxtView = (TextView) v.findViewById(id);
                hForecast.add(tmpTxtView);

                id_str = "hour" + i + "_ID";
                id = getResources().getIdentifier(id_str, "id", getActivity().getPackageName());
                tmpTxtView = (TextView) v.findViewById(id);
                hours.add(tmpTxtView);
            }
        }


//        Intent intent = getIntent();
        final String[] cityValues = null;
//        final String[] cityValues = intent.getStringArrayExtra("cityValues");
//        Toast.makeText(this, keyValue, Toast.LENGTH_SHORT).show();

        TextView changeCity = (TextView) v.findViewById(R.id.searchCity_ID);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment search = new CitySearchFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, search ); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
//                Intent i = new Intent(WeatherFragment.this, CitySearchActivity.class);
//                startActivity(i);
            }
        });

        ImageButton actualPosition = (ImageButton) v.findViewById(R.id.actualPositionBtn_ID);
        actualPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!cityNameAndKeyFromLocation()) {
                    return;
                }
            }
        });

        ImageButton AWLink = (ImageButton) v.findViewById(R.id.AWLink_imgButton);
        AWLink.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Uri uriUrl = Uri.parse("https://www.accuweather.com/");
                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
                startActivity(launchBrowser);
            }
        });


        city = (TextView) v.findViewById(R.id.cityName_ID);


//        if (android.os.Build.VERSION.SDK_INT > 9) {
//            StrictMode.ThreadPolicy policy = new
//                    StrictMode.ThreadPolicy.Builder().permitAll().build();
//            StrictMode.setThreadPolicy(policy);
//        }

        if (cityValues == null) {
            if (!cityNameAndKeyFromLocation()) {
                return v;
            }
        } else {
            city.setText(cityValues[0]);
            keyValue = cityValues[1];
        }


//        ImageButton pedometer_btn = (ImageButton)v.findViewById(R.id.pedometer_btn_ID);
//        pedometer_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
////                Intent i = new Intent(WeatherFragment.this, PedometerFragment.class);
////                startActivity(i);
//            }
//        });
//
//        ImageButton sleep_btn = (ImageButton)v.findViewById(R.id.sleep_btn_ID);
//        sleep_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//
//            }
//        });
//
//        ImageButton settings_btn = (ImageButton)findViewById(R.id.settings_btn_ID);
//        settings_btn.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent i = new Intent(WeatherFragment.this, SettingsFragment.class);
//                i.putExtra("from", "weather");
//                startActivity(i);
//            }
//        });

        return v;
    }



    public Boolean cityNameAndKeyFromLocation() {

        SharedPreferences sp = getActivity().getSharedPreferences("Weather", MainActivity.MODE_PRIVATE);

        keyValue = sp.getString("keyValue", "");

        if (keyValue.equals("")) {
            return false;
        }

        String cityName = sp.getString("cityName", "");
        city.setText(cityName);

        forecast();
        currentConditions();

        return true;
    }

    public void forecast() {
        // send forecast request to the api

        Call<JsonElement> todoCall = iMyService.forecast(keyValue, API_KEY, metricValue);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.code() == 200) {
                    JsonArray forecastArray = ((JsonObject) response.body()).getAsJsonArray("DailyForecasts");
                    int minTemp, maxTemp, currentDayIndex = -1;
                    for (int i = 0; i < forecastArray.size(); i++) {

                        JsonElement dailyForecast = forecastArray.get(i);

                        JsonObject icon = ((JsonObject) dailyForecast).getAsJsonObject("Day");
                        String iconName = "i" + icon.get("Icon");
                        int icon_id = getContext().getResources().getIdentifier(iconName, "drawable", getContext().getPackageName());
                        dIconForecast.get(i).setImageResource(icon_id);

                        JsonObject temperature = ((JsonObject) dailyForecast).getAsJsonObject("Temperature");

                        minTemp = (int) Math.round(Double.parseDouble(temperature.getAsJsonObject("Minimum").get("Value").toString()));
                        maxTemp = (int) Math.round(Double.parseDouble(temperature.getAsJsonObject("Maximum").get("Value").toString()));

                        String tmpTemp = minTemp + "°\n" + maxTemp + "°";
                        dForecast.get(i).setText(tmpTemp);

                        // get the forecast day
                        String[] day = {"Monday", "Tuesday", "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                        if (i == 0) {
                            Calendar calendar = Calendar.getInstance();
                            Date date = calendar.getTime();
                            String forecastDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

                            // get the current day index
                            for (int j = 0; j < day.length; j++) {
                                if (day[j].equals(forecastDay)) {
                                    currentDayIndex = j;
                                    break;
                                }
                            }
                        } else {
                            int tmpIndexId = i - 1;
                            int tmpIndexDay = (currentDayIndex + i) % 7;
                            daysTextView.get(tmpIndexId).setText(day[tmpIndexDay]);
                        }

                    }
                }
                else {
                    Toast.makeText(getActivity(), "change Api Key", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("FailureDebug", t.getMessage());
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    public void currentConditions() {
        // send current condition request to the api

        Call<JsonElement> todoCall = iMyService.currentconditions(keyValue, API_KEY);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    final String METRIC_VALUE = "Metric";
                    final String IMPERIAL_VALUE = "Imperial";

                    ImageView currentIconForecast = (ImageView) v.findViewById(R.id.currentIconForecast_ID);
                    TextView currentForecast = (TextView) v.findViewById(R.id.currentForecast_ID);
//
                    JsonArray currentConditionsJsonArray = (JsonArray) response.body();
                    String currentConditionsIconName = "i" + currentConditionsJsonArray.get(0).getAsJsonObject().get("WeatherIcon");
                    String weatherText = currentConditionsJsonArray.get(0).getAsJsonObject().get("WeatherText").toString();
                    weatherText = weatherText.substring(1, weatherText.length()-1);
                    int icon_id = getContext().getResources().getIdentifier(currentConditionsIconName, "drawable", getContext().getPackageName());
                    currentIconForecast.setImageResource(icon_id);
//
                    double currentConditionsTemp = Math.round(Double.parseDouble("" +
                            currentConditionsJsonArray.get(0).getAsJsonObject().get("Temperature")
                                    .getAsJsonObject().get("Metric").getAsJsonObject().get("Value")));
//
                    if (metricValue.equals("false")) {
                        currentConditionsTemp = ((9 / 5) * currentConditionsTemp) + 32;
                    }

                    String currentForecastText = currentConditionsTemp + "°\n" + weatherText;
                    currentForecast.setText(currentForecastText);
                }
                else {
                    Toast.makeText(getActivity(), "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        // send hourly forecast request to the api

        Call<JsonElement> todoCall2 = iMyService.hourlyforecast(keyValue, API_KEY, metricValue);
        todoCall2.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.code() == 200) {
                    JsonArray hourlyJsonArray = response.body().getAsJsonArray();

                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    calendar.setTime(date);
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);


                    for (int i = 0; i < 4; i++) {

                        int tmpIndex = (i + 1) * 2;
                        int tmpHour = (currentHour + tmpIndex) % 24;

                        String tmpHourStr = tmpHour + ":00";

                        hours.get(i).setText(tmpHourStr);

                        JsonObject hourlyJsonObject = hourlyJsonArray.get(tmpIndex).getAsJsonObject();

                        //JSONObject icon = hourlyJsonObject.getJSONObject("WeatherIcon");
                        String iconName = "i" + hourlyJsonObject.get("WeatherIcon");
                        int icon_id = getContext().getResources().getIdentifier(iconName, "drawable", getContext().getPackageName());
                        hIconForecast.get(i).setImageResource(icon_id);

                        JsonObject temperature = hourlyJsonObject.get("Temperature").getAsJsonObject();

                        int temp = (int) Math.round(Double.parseDouble("" + temperature.get("Value")));

                        String tmpTemp = temp + "°";
                        hForecast.get(i).setText(tmpTemp);
                    }
                }
                else {
                    Toast.makeText(getActivity(), "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(getActivity(), t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

//    private boolean checkPermissions(){
//        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
//                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
//            return true;
//        }
//        return false;
//    }
//
//    private void requestPermissions() {
//        String[] permissionArray = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
//        ActivityCompat.requestPermissions(this, permissionArray, PERMISSION_ID);
//    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        if (requestCode == PERMISSION_ID) {
//            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                // Granted. Start getting the location information
//                localisation = getLastLocation();
//                //return getLocation.getLastLocation(this, this);
//            }
//        }
//    }

//    private boolean isLocationEnabled(){
//        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
//        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
//                LocationManager.NETWORK_PROVIDER
//        );
//    }

//    @SuppressLint("MissingPermission")
//    private String getLastLocation(){
//        if (checkPermissions()) {
//            if (isLocationEnabled()) {
//                locationListener = new LocationListener() {
//                    @Override
//                    public void onLocationChanged(Location location) {
//                        localisation = location.toString();
//                    }
//
//                    @Override
//                    public void onStatusChanged(String provider, int status, Bundle extras) {
//
//                    }
//
//                    @Override
//                    public void onProviderEnabled(String provider) {
//
//                    }
//
//                    @Override
//                    public void onProviderDisabled(String provider) {
//
//                    }
//                };
//                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPFATE, locationListener);
//
//                Criteria criteria = new Criteria();
//                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
//                //criteria.setAltitudeRequired(true);
//                criteria.setPowerRequirement(Criteria.POWER_LOW);
//                criteria.setCostAllowed(true);
//
//                locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);
//
//
//                String best = locationManager.getBestProvider(criteria, false);
//                locationManager.requestLocationUpdates(best, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPDATE, locationListener);
//                String provider =locationManager.getBestProvider(criteria, true);
//                Location loc = locationManager.getLastKnownLocation(provider);
//
//                Log.d("GPS", "loc:" + loc.toString());
//                return  loc.getLatitude() + "," + loc.getLongitude();
//
//            }
//            else {
//                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
//                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
//                startActivity(intent);
//                return "";
//            }
//        } else {
//            requestPermissions();
//            return "";
//        }
//    }
}
