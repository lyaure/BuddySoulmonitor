package com.buddynsoul.monitor;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.os.Bundle;
import android.os.StrictMode;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

public class CitySearchFragment extends Fragment {

    private ArrayList<City> cities = new ArrayList<>();
    private TextView cityName;
    private TextView search;
    private ListView citiesList;
    private CityAdapter adapter;
    private URL builtUri;
    private View v;
    private IMyService iMyService;
    private String API_KEY;


    public CitySearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_city_search, container, false);

        iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);

        API_KEY = getResources().getString(R.string.accuweather_api_key);

        cityName = (TextView) v.findViewById(R.id.autocomplete_ID);
        citiesList = (ListView) v.findViewById(R.id.citiesList_ID);

        cityName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                cities.clear();
            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

            }

            @Override
            public void afterTextChanged(Editable s) {
                if (!cityName.getText().toString().equals("")) {
                    autoCompleteListSearch();
                    if (citiesList.getVisibility() == View.INVISIBLE)
                        citiesList.setVisibility(View.VISIBLE);
                } else {
                    if (citiesList.getVisibility() == View.VISIBLE)
                        citiesList.setVisibility(View.INVISIBLE);
                    cities.clear();
                }

            }
        });

        adapter = new CityAdapter(getContext(), cities);
        citiesList.setAdapter(adapter);

        search = (TextView)v.findViewById(R.id.searchCity_ID);

        search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("DebugTextViewCity", "clicked");
                cities.clear();
                autoCompleteListSearch();
                Fragment fragment = new PedometerFragment();
                loadFragment(fragment);
            }
        });

        citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
//                String[] values = {cities.get(position).getCityName() ,cities.get(position).getKeyValue()};
//                Intent i = new Intent(CitySearchFragment.this, WeatherFragment.class);
//                i.putExtra("cityValues", values);
//                startActivity(i);
            }
        });


        if (android.os.Build.VERSION.SDK_INT > 9) {
            StrictMode.ThreadPolicy policy = new
                    StrictMode.ThreadPolicy.Builder().permitAll().build();
            StrictMode.setThreadPolicy(policy);
        }

        return v;
    }

    private void loadFragment(Fragment fragment) {
        // load fragment
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container, fragment);
        transaction.addToBackStack(null);
        transaction.commit();
    }

    public void autoCompleteListSearch() {
        // send autocomplete request to the api
        Call<JsonElement> todoCall = iMyService.autocomplete(API_KEY, cityName.getText().toString());
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.code() == 200) {
                    // url to get key value of the city
                    String keyValue = "";

                    JsonArray autocompleteArray = response.body().getAsJsonArray();
                    if (autocompleteArray.size() == 0) {
                        cities.add(new City());
                    } else {
                        int loopSize = Math.min(autocompleteArray.size(), 10);

                        for (int i = 0; i < loopSize; i++) {
                            JsonObject jObj = autocompleteArray.get(i).getAsJsonObject();

                            String cityName = "" + jObj.get("LocalizedName").toString();
                            cityName = cityName.substring(1, cityName.length()-1);

                            String countryName = jObj.get("Country").getAsJsonObject().get("LocalizedName").getAsJsonObject().toString();
                            countryName = countryName.substring(1, countryName.length()-1);

                            keyValue = jObj.get("Key").getAsJsonObject().toString();
                            keyValue = keyValue.substring(1, keyValue.length()-1);
                            cities.add(new City(cityName, countryName, keyValue));
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
                else {
                    Toast.makeText(getActivity(), "change Api Key", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {

            }
        });


    }
}
