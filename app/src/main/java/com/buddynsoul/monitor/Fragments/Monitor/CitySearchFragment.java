package com.buddynsoul.monitor.Fragments.Monitor;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Activities.MonitorActivity;
import com.buddynsoul.monitor.Adapters.CityAdapter;
import com.buddynsoul.monitor.Objects.City;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.WeatherUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;

public class CitySearchFragment extends Fragment {

    private ArrayList<City> cities = new ArrayList<>();
    private TextView cityName;
    private ListView citiesList;
    private CityAdapter adapter;
    private IMyService iMyService;
    private String API_KEY;

    public CitySearchFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_city_search, container, false);

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


        citiesList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //String[] values = {cities.get(position).getCityName() ,cities.get(position).getKeyValue()};

                SharedPreferences sp = getContext().getSharedPreferences("Weather_autocomplete", MonitorActivity.MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putString("keyValue", cities.get(position).getKeyValue());
                editor.putString("cityName", cities.get(position).getCityName());
                editor.apply();

                WeatherUtils.forecast(getContext(), true);
                WeatherUtils.currentConditions(getContext(), true);

                Fragment weather = new WeatherFragment();

                Bundle bundle = new Bundle();
                bundle.putBoolean("autocomplete", true);
                weather.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container_ID, weather); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
            }
        });

        return v;
    }
    // load fragment
    private void loadFragment(Fragment fragment) {
        FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
        transaction.replace(R.id.container_ID, fragment);
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

                            String countryName = jObj.get("Country").getAsJsonObject().get("LocalizedName").toString();
                            countryName = countryName.substring(1, countryName.length()-1);

                            keyValue = jObj.get("Key").toString();
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
                Toast.makeText(getActivity(), "An error occurred", Toast.LENGTH_LONG).show();
            }
        });
    }
}
