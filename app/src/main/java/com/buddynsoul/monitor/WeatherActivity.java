package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.StrictMode;
import android.provider.Settings;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

//import org.json.JSONException;
import org.json.JSONObject;


import org.json.JSONArray;
import org.json.JSONException;

import java.net.URL;
import java.io.IOException;
import java.lang.Math;

public class WeatherActivity extends AppCompatActivity{
    private static final long MIN_TIME_FOR_UPDATE = 0;
    private static final float MIN_DIS_FOR_UPFATE = 0;
    final int PERMISSION_ID = 42;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String localisation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        TextView city = (TextView)findViewById(R.id.cityName_ID);
        URL builtUri;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // get the last location
        localisation = getLastLocation();

        // build geoposition request
        builtUri = NetworkUtils.buildUrlForWeather(this, "geoposition", localisation);
        String response;

        // url to get key value of the city
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);
        } catch (IOException e) {
            e.printStackTrace();
            response = "error";
        }

        String keyValue = "";

        // send get request to the api
        try {
            keyValue = new JSONObject(response).getString("Key");
            city.setText(new JSONObject(response).getString("EnglishName"));
        } catch (JSONException e) {
            e.printStackTrace();
        }

        // build forecast request
        builtUri = NetworkUtils.buildUrlForWeather(this, "forecast", keyValue);

        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);
        } catch (IOException e) {
            e.printStackTrace();
            response = "error";
        }

        // send get request to the api
        try {
            JSONObject jsnobject = new JSONObject(response);

            Log.d("responseFromApi", response);

            TextView dForecast_1 = (TextView)findViewById(R.id.dForecast1_ID);
            TextView dForecast_2 = (TextView)findViewById(R.id.dForecast2_ID);
            TextView dForecast_3 = (TextView)findViewById(R.id.dForecast3_ID);
            TextView dForecast_4 = (TextView)findViewById(R.id.dForecast4_ID);
            TextView dForecast_5 = (TextView)findViewById(R.id.dForecast5_ID);

            TextView[] textViewsArray = {dForecast_1, dForecast_2, dForecast_3, dForecast_4, dForecast_5};

            JSONObject forecastJson = new JSONObject(response);
            JSONArray forecastArray = forecastJson.getJSONArray("DailyForecasts");
            double minTemp, maxTemp;
            for(int i = 0; i < forecastArray.length(); i++) {
                JSONObject dailyForecast = forecastArray.getJSONObject(i);
                JSONObject tempObject = dailyForecast.getJSONObject("Temperature");

                minTemp = Math.round(tempObject.getJSONObject("Minimum").getDouble("Value"));
                maxTemp = Math.round(tempObject.getJSONObject("Maximum").getDouble("Value"));

                String tmpTemp = minTemp + "°\n" + maxTemp + "°";
                textViewsArray[i].setText(tmpTemp);

                Log.d("DebugReponse", tmpTemp);

                //add these minTemp and maxTemp to array or the
                //way you want to use
            }

            // todo --------- HERE ------------


        } catch (JSONException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
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
                locationManager.requestLocationUpdates(best, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPFATE, locationListener);
                String provider =locationManager.getBestProvider(criteria, true);
                Location loc = locationManager.getLastKnownLocation(provider);


//                locationManager.requestSingleUpdate(criteria, locationListener, null);

                Log.d("GPS", "loc:" + loc.toString());
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
}
