package com.buddynsoul.monitor;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
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

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

public class getLocation extends AppCompatActivity {
    private static int PERMISSION_ID;
    private static final long MIN_TIME_FOR_UPDATE = 0;
    private static final float MIN_DIS_FOR_UPDATE = 0;


    public static boolean checkPermissions(final Context context){
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            return true;
        }
        return false;
    }

    public static void requestPermissions(final Activity activity) {
        String[] permissionArray = {Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
        PERMISSION_ID = 42;
        ActivityCompat.requestPermissions(activity, permissionArray, PERMISSION_ID);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_ID) {
            if(grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED){
                // Granted. Start getting the location information
                //getLastLocation(,);
            }
        }
    }

    public static boolean isLocationEnabled(final Context context){
        LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
                LocationManager.NETWORK_PROVIDER
        );
    }

    @SuppressLint("MissingPermission")
    public static String getLastLocation(final Activity activity, final Context context){
        if (checkPermissions(context)) {
            if (isLocationEnabled(context)) {
                LocationListener locationListener = new LocationListener() {
                    @Override
                    public void onLocationChanged(Location location) {
                        String localisation = location.toString();
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

                LocationManager locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);


                String best = locationManager.getBestProvider(criteria, false);
                locationManager.requestLocationUpdates(best, MIN_TIME_FOR_UPDATE, MIN_DIS_FOR_UPDATE, locationListener);
                String provider = locationManager.getBestProvider(criteria, true);
                Location loc = locationManager.getLastKnownLocation(provider);

                try {
                    Log.d("GPS", "loc:" + loc.toString());
                    return  loc.getLatitude() + "," + loc.getLongitude();
                }
                catch (Exception e) {
                    Log.d("GPS", "loc:");
                    return  "";
                }


            }
            else {
                if (activity != null) {
                    Toast.makeText(activity, "Turn on location", Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
                    context.startActivity(intent);

                }
                return "";
            }
        } else {
            if (activity != null)
                requestPermissions(activity);
            return "";
        }
    }

}
