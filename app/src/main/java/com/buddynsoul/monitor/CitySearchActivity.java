package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.os.StrictMode;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CitySearchActivity extends AppCompatActivity {

    private ArrayList<City> cities = new ArrayList<>();
    private TextView cityName;
    private Button search;
    private ListView citiesList;
    private CityAdapter adapter;
    private URL builtUri;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_city_search);

        cityName = (TextView)findViewById(R.id.autocomplete_ID);
        citiesList = (ListView)findViewById(R.id.citiesList_ID);

        adapter = new CityAdapter(this, cities);
        citiesList.setAdapter(adapter);

        search = (Button)findViewById(R.id.searchBtn_ID);
        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                cities.clear();
                buildAutoCompleteListSearch();
            }
        });

        citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                String[] values = {cities.get(position).getCityName() ,cities.get(position).getKeyValue()};
                Intent i = new Intent(CitySearchActivity.this, WeatherActivity.class);
                i.putExtra("cityValues", values);
                startActivity(i);
            }
        });



        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

    }

    public void buildAutoCompleteListSearch(){
        // build geoposition request
        builtUri = NetworkUtils.buildUrlForWeather(this, "autocomplete", cityName.getText().toString(), "true");
        String response = "";

        // url to get key value of the city
        String keyValue = "";
        try {
            response = NetworkUtils.getResponseFromHttpUrl(builtUri);

            // send get request to the api to get the uniqueId and the city name
            try {
                JSONArray autocompleteArray = new JSONArray(response);
                if (autocompleteArray.length() == 0) {
                    cities.add(new City());
                }
                else{
                    int loopSize = autocompleteArray.length() < 10 ? autocompleteArray.length() : 10;

                    for(int i = 0; i < loopSize; i++) {
                        JSONObject jObj = autocompleteArray.getJSONObject(i);
                        String cityName = jObj.getString("LocalizedName");
                        String countryName = jObj.getJSONObject("Country").getString("LocalizedName");
                        keyValue = jObj.getString("Key");
                        cities.add(new City(cityName, countryName, keyValue));
                    }
                }
                adapter.notifyDataSetChanged();

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
