package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

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
import android.provider.Settings;
import android.util.Log;
import android.widget.Toast;

public class WeatherActivity extends AppCompatActivity{
    private static final long MIN_TIME_FOR_UPDATE = 0;
    private static final float MIN_DIS_FOR_UPFATE = 0;
    final int PERMISSION_ID = 42;
    private LocationManager locationManager;
    private LocationListener locationListener;
    private String localisation;
    private static final String ACCUWEATHER_MAP_API =
            "http://api.openweathermap.org/data/2.5/weather?q=%s&units=metric";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_weather);

        localisation = getLastLocation();

        Toast.makeText(this, "loc"+localisation, Toast.LENGTH_LONG).show();


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
                return loc.toString();

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
