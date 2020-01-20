package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.os.StrictMode;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CitySearchActivity extends AppCompatActivity {

    private ArrayList<City> Cities = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);

        URL builtUri;

        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        // build geoposition request
        builtUri = NetworkUtils.buildUrlForWeather(this, "autocomplete", "Paris");
        String response = "";

        // url to get key value of the city
        String keyValue = "";
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);

            // send get request to the api to get the uniqueId and the city name
            try {
                JSONArray autocompleteArray = new JSONArray(response);
                int loopSize = autocompleteArray.length() < 10 ? autocompleteArray.length() : 10;

                for(int i = 0; i < loopSize; i++) {
                    JSONObject jObj = autocompleteArray.getJSONObject(i);
                    String cityName = jObj.getString("LocalizedName");
                    String countryName = jObj.getJSONObject("Country").getString("LocalizedName");
                    keyValue = jObj.getString("Key");
                    Cities.add(new City(cityName, countryName, keyValue));
                }




            } catch (JSONException e) {
                e.printStackTrace();
            }

        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
            //response = "error";
        }



    }


}
