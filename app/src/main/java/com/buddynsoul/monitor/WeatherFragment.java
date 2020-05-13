package com.buddynsoul.monitor;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;

import java.util.ArrayList;
import java.util.concurrent.TimeUnit;

public class WeatherFragment extends Fragment {
    private View v;

    private ArrayList<ImageView> dIconForecast = new ArrayList<>();
    private ArrayList<TextView> dForecast = new ArrayList<>();
    private ArrayList<TextView> daysTextView = new ArrayList<>();
    private ArrayList<ImageView> hIconForecast = new ArrayList<>();
    private ArrayList<TextView> hForecast = new ArrayList<>();
    private ArrayList<TextView> hours = new ArrayList<>();

    private ArrayList<String> forecast_data = new ArrayList<>();
    private ArrayList<String> currentConditions_data = new ArrayList<>();
    private ArrayList<String> hourlyForecast_data = new ArrayList<>();

    private SharedPreferences sp;


    public WeatherFragment() {

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        v = inflater.inflate(R.layout.fragment_weather, container, false);

        if (!Util.isNetworkAvailable(getActivity())) {
            Toast.makeText(getActivity(), "Please check your internet connection", Toast.LENGTH_LONG).show();
            return v;
        }

        // choose the right sp file and update last update text field
        TextView last_update_txtv = (TextView) v.findViewById(R.id.lastUpdate_ID);
        Bundle bundle = this.getArguments();
        if (bundle != null) {
            if (bundle.getBoolean("autocomplete", true)) {
                sp = getActivity().getSharedPreferences("Weather_autocomplete", MainActivity.MODE_PRIVATE);
                last_update_txtv.setText("");
            }
        }
        else {
            sp = getActivity().getSharedPreferences("Weather", MainActivity.MODE_PRIVATE);

            String last_update_txt = lastUpdate(sp);
            last_update_txtv.setText(last_update_txt);
        }

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


        TextView changeCity = (TextView) v.findViewById(R.id.searchCity_ID);
        changeCity.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment search = new CitySearchFragment();
                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.container, search ); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
            }
        });

        if (!initialize_arrayList(sp)) {
            return v;
        }

        ImageButton actualPosition = (ImageButton) v.findViewById(R.id.actualPositionBtn_ID);
        actualPosition.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //cityNameAndKeyFromLocation();
                sp = getActivity().getSharedPreferences("Weather", MainActivity.MODE_PRIVATE);

                if (initialize_arrayList(sp)) {
                    cityNameAndKeyFromLocation();
                    forecast(forecast_data);
                    currentConditions(currentConditions_data, hourlyForecast_data);

                    String last_update_txt = lastUpdate(sp);
                    last_update_txtv.setText(last_update_txt);
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


        cityNameAndKeyFromLocation();
        forecast(forecast_data);
        currentConditions(currentConditions_data, hourlyForecast_data);

        return v;
    }



    private Boolean cityNameAndKeyFromLocation() {

        String keyValue = sp.getString("keyValue", "");

        if (keyValue.equals("")) {
            return false;
        }

        String cityName = sp.getString("cityName", "");

        TextView city = (TextView) v.findViewById(R.id.cityName_ID);
        city.setText(cityName);

        return true;
    }

    private void forecast(ArrayList<String> forecast_data) {
        // add forecast data to fragment view
        final int forecast_size = 5;

        for (int i = 0, j = 0; i < forecast_size; i++, j+=2) {
            dIconForecast.get(i).setImageResource(Integer.parseInt(forecast_data.get(j)));
            dForecast.get(i).setText(forecast_data.get(j+1));

            if (i != 0) {
                daysTextView.get(i-1).setText(forecast_data.get(j+2));
                j++;
            }

        }
    }

    private void currentConditions(ArrayList<String> currentConditions_data, ArrayList<String> hourlyForecast_data) {
        // add current conditions data to fragment view

        ImageView currentIconForecast = (ImageView) v.findViewById(R.id.currentIconForecast_ID);
        TextView currentForecast = (TextView) v.findViewById(R.id.currentForecast_ID);

        currentIconForecast.setImageResource(Integer.parseInt(currentConditions_data.get(0)));
        currentForecast.setText(currentConditions_data.get(1));

        // add hourly forecast data to fragment view
        for (int i = 0, j = 0; i < 4; i++, j+=3) {
            hours.get(i).setText(hourlyForecast_data.get(j));
            hIconForecast.get(i).setImageResource(Integer.parseInt(hourlyForecast_data.get(j+1)));
            hForecast.get(i).setText(hourlyForecast_data.get(j+2));
        }
    }

    private boolean initialize_arrayList(SharedPreferences sp) {
        Gson gson = new Gson();
        String json_data = sp.getString("json_data", "");

        if (json_data.equals("")) {
            Toast.makeText(getActivity(), "change Api Key", Toast.LENGTH_LONG).show();
            return false;
        }

        Type type = new TypeToken<ArrayList<ArrayList<String>> >(){}.getType();
        ArrayList<ArrayList<String>>  weather_data = gson.fromJson(json_data, type);

        forecast_data = weather_data.get(0);
        currentConditions_data = weather_data.get(1);
        hourlyForecast_data = weather_data.get(2);

        return true;
    }

    private String lastUpdate(SharedPreferences sp) {
        long last_update = sp.getLong("last_update", System.currentTimeMillis());
        long time_difference = System.currentTimeMillis() - last_update;
        last_update = TimeUnit.MILLISECONDS.toMinutes(time_difference);

        String last_update_txt = "";
        if (last_update == 0) {
            last_update_txt = "just upadated";
        }
        else {
            last_update_txt = "last update "+ (int) last_update + " min";
        }
        return last_update_txt;
    }

}
