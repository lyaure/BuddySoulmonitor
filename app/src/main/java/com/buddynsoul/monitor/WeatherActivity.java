package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.view.GestureDetectorCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONObject;
import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.io.IOException;
import java.lang.Math;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

public class WeatherActivity extends AppCompatActivity implements GestureDetector.OnGestureListener{
    private static final long MIN_TIME_FOR_UPDATE = 0;
    private static final float MIN_DIS_FOR_UPDATE = 0;
    final int PERMISSION_ID = 42;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String localisation;
    private GestureDetectorCompat detector;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        Intent intent = getIntent();
        String keyValue = null;
        String[] cityValues = intent.getStringArrayExtra("cityValues");
//        Toast.makeText(this, keyValue, Toast.LENGTH_SHORT).show();



        ImageButton changeCity = (ImageButton)findViewById(R.id.changeCityBtn_ID);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(WeatherActivity.this, CitySearchActivity.class);
                startActivity(i);
            }
        });

        detector = new GestureDetectorCompat(this, this);

        TextView city = (TextView)findViewById(R.id.cityName_ID);

        URL builtUri;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // get the last location
        localisation = getLastLocation();

//        if (localisation.equals("")) {
//
//        }
//        else {
//
//        }
        String response = "";

       if(cityValues == null){
           // build geoposition request
           builtUri = NetworkUtils.buildUrlForWeather(this, "geoposition", localisation);


           // url to get key value of the city

           try {
               response = NetworkUtils.getResponseFromHttpUrl(builtUri);

               // send get request to the api to get the uniqueId and the city name
               try {
                   keyValue = new JSONObject(response).getString("Key");
                   city.setText(new JSONObject(response).getString("EnglishName"));
               } catch (JSONException e) {
                   e.printStackTrace();
               }

           } catch (IOException e) {
               e.printStackTrace();
               Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
               //response = "error";
           }

       }
       else {
           city.setText(cityValues[0]);
           keyValue = cityValues[1];
       }


        // build forecast request
        builtUri = NetworkUtils.buildUrlForWeather(this, "forecast", keyValue);
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);

            // send get request to the api
            try {
                //JSONObject jsnobject = new JSONObject(response);

                //Log.d("responseFromApi", response);

                ImageView dIconForecast_1 = (ImageView) findViewById(R.id.dIconForecast1_ID);
                ImageView dIconForecast_2 = (ImageView)findViewById(R.id.dIconForecast2_ID);
                ImageView dIconForecast_3 = (ImageView)findViewById(R.id.dIconForecast3_ID);
                ImageView dIconForecast_4 = (ImageView)findViewById(R.id.dIconForecast4_ID);
                ImageView dIconForecast_5 = (ImageView)findViewById(R.id.dIconForecast5_ID);

                ImageView[] dIconForecast = {dIconForecast_1, dIconForecast_2, dIconForecast_3, dIconForecast_4, dIconForecast_5};

                TextView dForecast_1 = (TextView)findViewById(R.id.dForecast1_ID);
                TextView dForecast_2 = (TextView)findViewById(R.id.dForecast2_ID);
                TextView dForecast_3 = (TextView)findViewById(R.id.dForecast3_ID);
                TextView dForecast_4 = (TextView)findViewById(R.id.dForecast4_ID);
                TextView dForecast_5 = (TextView)findViewById(R.id.dForecast5_ID);

                TextView[] dForecast = {dForecast_1, dForecast_2, dForecast_3, dForecast_4, dForecast_5};

                TextView day2 = (TextView)findViewById(R.id.day2_ID);
                TextView day3 = (TextView)findViewById(R.id.day3_ID);
                TextView day4 = (TextView)findViewById(R.id.day4_ID);
                TextView day5 = (TextView)findViewById(R.id.day5_ID);

                TextView[] daysTextView = {day2, day3, day4, day5};

                JSONObject forecastJson = new JSONObject(response);
                JSONArray forecastArray = forecastJson.getJSONArray("DailyForecasts");
                int minTemp, maxTemp, currentDayIndex = -1;
                for(int i = 0; i < forecastArray.length(); i++) {
                    JSONObject dailyForecast = forecastArray.getJSONObject(i);

                    JSONObject icon = dailyForecast.getJSONObject("Day");
                    String iconName = "i" + icon.getInt("Icon");
                    int icon_id = getApplicationContext().getResources().getIdentifier(iconName, "drawable", getPackageName());
                    dIconForecast[i].setImageResource(icon_id);

                    JSONObject temperature = dailyForecast.getJSONObject("Temperature");

                    minTemp = (int)Math.round(temperature.getJSONObject("Minimum").getDouble("Value"));
                    maxTemp = (int)Math.round(temperature.getJSONObject("Maximum").getDouble("Value"));

                    String tmpTemp = minTemp + "°\n" + maxTemp + "°";
                    dForecast[i].setText(tmpTemp);

                    // get the forecast day
                    String[] day = {"Monday", "Tuesday" , "Wednesday", "Thursday", "Friday", "Saturday", "Sunday"};

                    if ( i == 0 ) {
                        Calendar calendar = Calendar.getInstance();
                        Date date = calendar.getTime();
                        String forecastDay = new SimpleDateFormat("EEEE", Locale.ENGLISH).format(date.getTime());

                        // get the current day index
                        for (int j=0; j<day.length; j++) {
                            if (day[j].equals(forecastDay)) {
                                currentDayIndex = j;
                                break;
                            }
                        }
                    }
                    else {
                        int tmpIndexId = i - 1;
                        int tmpIndexDay = (currentDayIndex + i) % 7;
                        daysTextView[tmpIndexId].setText(day[tmpIndexDay]);
                    }


                }

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            //response = "error";
        }

        // build currentconditions request
        builtUri = NetworkUtils.buildUrlForWeather(this, "currentconditions", keyValue);
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);

            // send get request to the api
            try {
                final String METRIC_VALUE = "Metric";
                final String IMPERIAL_VALUE = "Imperial";

                ImageView currentIconForecast = (ImageView) findViewById(R.id.currentIconForecast_ID);
                TextView currentForecast = (TextView)findViewById(R.id.currentForecast_ID);

                JSONArray currentConditionsJsonArray = new JSONArray(response);
                String currentConditionsIconName = "i" + currentConditionsJsonArray.getJSONObject(0).getInt("WeatherIcon");
                String weatherText = currentConditionsJsonArray.getJSONObject(0).getString("WeatherText");
                int icon_id = getApplicationContext().getResources().getIdentifier(currentConditionsIconName, "drawable", getPackageName());
                currentIconForecast.setImageResource(icon_id);

                JSONObject currentConditions = currentConditionsJsonArray.getJSONObject(0).getJSONObject("Temperature");
                int currentConditionsTemp = (int)Math.round(currentConditions.getJSONObject(METRIC_VALUE).getDouble("Value"));

                String currentForecastText = currentConditionsTemp + "°\n" + weatherText;
                currentForecast.setText(currentForecastText);

            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                Log.d("Debug2", response);
            }

        } catch (IOException e) {
            e.printStackTrace();
            //response = "error";
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d("Debug1", response);
        }

        // build hourly forecast request
        builtUri = NetworkUtils.buildUrlForWeather(this, "hourlyforecast", keyValue);
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);

            // send get request to the api
            try {

                ImageView hIconForecast_1 = (ImageView) findViewById(R.id.hIconForecast1_ID);
                ImageView hIconForecast_2 = (ImageView)findViewById(R.id.hIconForecast2_ID);
                ImageView hIconForecast_3 = (ImageView)findViewById(R.id.hIconForecast3_ID);
                ImageView hIconForecast_4 = (ImageView)findViewById(R.id.hIconForecast4_ID);

                ImageView[] hIconForecast = {hIconForecast_1, hIconForecast_2, hIconForecast_3, hIconForecast_4};

                TextView hForecast_1 = (TextView)findViewById(R.id.hForecast1_ID);
                TextView hForecast_2 = (TextView)findViewById(R.id.hForecast2_ID);
                TextView hForecast_3 = (TextView)findViewById(R.id.hForecast3_ID);
                TextView hForecast_4 = (TextView)findViewById(R.id.hForecast4_ID);

                TextView[] hForecast = {hForecast_1, hForecast_2, hForecast_3, hForecast_4};

                TextView h_1 = (TextView)findViewById(R.id.hour1_ID);
                TextView h_2 = (TextView)findViewById(R.id.hour2_ID);
                TextView h_3 = (TextView)findViewById(R.id.hour3_ID);
                TextView h_4 = (TextView)findViewById(R.id.hour4_ID);

                TextView[] hours = {h_1, h_2, h_3, h_4};

                JSONArray hourlyJsonArray = new JSONArray(response);

                Calendar calendar = Calendar.getInstance();
                Date date = calendar.getTime();
                calendar.setTime(date);
                int currentHour = calendar.get(Calendar.HOUR_OF_DAY);


                for(int i = 0; i < 4; i++) {

                    int tmpIndex = (i+1) * 2;
                    int tmpHour = (currentHour + tmpIndex) % 24;

                    String tmpHourStr = tmpHour + ":00";

                    hours[i].setText(tmpHourStr);

                    JSONObject hourlyJsonObject = hourlyJsonArray.getJSONObject(tmpIndex);

                    //JSONObject icon = hourlyJsonObject.getJSONObject("WeatherIcon");
                    String iconName = "i" + hourlyJsonObject.getInt("WeatherIcon");
                    int icon_id = getApplicationContext().getResources().getIdentifier(iconName, "drawable", getPackageName());
                    hIconForecast[i].setImageResource(icon_id);

                    JSONObject temperature = hourlyJsonObject.getJSONObject("Temperature");

                    int temp = (int) Math.round(temperature.getDouble("Value"));

                    String tmpTemp = temp + "°";
                    hForecast[i].setText(tmpTemp);
                }



            } catch (JSONException e) {
                e.printStackTrace();
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                Log.d("Debug2", response);
            }

        } catch (IOException e) {
            e.printStackTrace();
            //response = "error";
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            Log.d("Debug1", response);
        }




    }

    private boolean checkPermissions(){
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    private void requestPermissions() {
        String[] permissionArray = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        ActivityCompat.requestPermissions(this, permissionArray, PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                getLastLocation();            }
        }
    }

    private boolean isLocationEnabled(){
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("MissingPermission")
    private String getLastLocation(){
        if (checkPermissions()) {
            if (isLocationEnabled()) {
                locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        localisation = location.toString();
                    }

                    @Override
                    public void onStatusChanged(String provider, int status, Bundle extras) {

                    }

                    @Override
                    public void onProviderEnabled(String provider) {

                    }

                    @Override
                    public void onProviderDisabled(String provider) {

                    }
                };
                // locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPFATE, locationListener);

                Criteria criteria = new Criteria();
                criteria.setAccuracy(Criteria.ACCURACY_COARSE);
                //criteria.setAltitudeRequired(true);
                criteria.setPowerRequirement(Criteria.POWER_LOW);
                criteria.setCostAllowed(true);

                locationManager = (LocationManager)this.getSystemService(Context.LOCATION_SERVICE);


                String best = locationManager.getBestProvider(criteria, false);
                locationManager.requestLocationUpdates(best, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPDATE, locationListener);
                String provider =locationManager.getBestProvider(criteria, true);
                Location loc = locationManager.getLastKnownLocation(provider);


//                locationManager.requestSingleUpdate(criteria, locationListener, null);

                //Log.d("GPS", "loc:" + loc.toString());
                return  loc.getLatitude() + "," + loc.getLongitude();

            }
            else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show();
                Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                startActivity(intent);
                return "";
            }
        } else {
            requestPermissions();
            return "";
        }
    }

    @Override
    public boolean onDown(MotionEvent e) {
        return false;
    }

    @Override
    public void onShowPress(MotionEvent e) {

    }

    @Override
    public boolean onSingleTapUp(MotionEvent e) {
        return false;
    }

    @Override
    public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
        return false;
    }

    @Override
    public void onLongPress(MotionEvent e) {

    }


    @Override
    public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
        if(e1.getX() < e2.getX()){
            Intent i = new Intent(WeatherActivity.this, PedometerActivity.class);
            startActivity(i);
        }
        return true;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        detector.onTouchEvent(event);
        return super.onTouchEvent(event);
    }
}
