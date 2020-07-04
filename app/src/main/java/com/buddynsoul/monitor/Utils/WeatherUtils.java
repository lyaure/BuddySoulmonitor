package com.buddynsoul.monitor.Utils;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;
import android.widget.Toast;

import com.buddynsoul.monitor.MonitorActivity;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.getLocation;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public abstract class WeatherUtils {

    private static ArrayList<ArrayList<String>> weather_data = new ArrayList<>(Collections.nCopies(3, null));
    private static String API_KEY;
    private static SharedPreferences sp;

    public static void cityNameAndKeyFromLocation(Activity activity, Context context) {

        API_KEY = context.getResources().getString(R.string.accuweather_api_key);

        SharedPreferences sp = context.getSharedPreferences("Weather", MonitorActivity.MODE_PRIVATE);
        long last_update = sp.getLong("last_update", 0);

        // if last update is more than one hour then do new weather update
        long time_difference = System.currentTimeMillis() - last_update;
        if (TimeUnit.MILLISECONDS.toHours(time_difference) < 1 ) {
            // data is up to date
            return;
        }

        // get the last location
        String localisation = getLocation.getLastLocation(activity, context);

        if (localisation.equals("")) {
            return;
        }

        // send geoposition request to the api
        IMyService iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);
        Call<JsonElement> todoCall = iMyService.geoposition(API_KEY, localisation);

        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                //response.isSuccessful()
                if (response.code() == 200) {
                    String keyValue = ((JsonObject) response.body()).get("Key").toString();
                    keyValue = keyValue.substring(1, keyValue.length() - 1);

                    String cityName = ((JsonObject) response.body()).get("EnglishName").toString();
                    cityName = cityName.substring(1, cityName.length() - 1);

                    //Save to sharedPreference
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("keyValue", keyValue);
                    editor.putString("cityName", cityName);
                    editor.commit();

                    forecast(context, false);
                    currentConditions(context, false);
                }
                else {
                    Toast.makeText(context, "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(context, "change Api Key", Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void forecast(Context context, boolean autocomplete) {

        API_KEY = context.getResources().getString(R.string.accuweather_api_key);

        SharedPreferences prefs = context.getSharedPreferences("Settings", MonitorActivity.MODE_PRIVATE);
        boolean metricValue = prefs.getBoolean("metricValue", true);

        chooseSpFile(context, autocomplete);
        String keyValue = sp.getString("keyValue", "");

        // send forecast request to the api
        IMyService iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);
        Call<JsonElement> todoCall = iMyService.forecast(keyValue, API_KEY, ""+metricValue);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.code() == 200) {

                    ArrayList<String> forecast_data = new ArrayList<>();

                    JsonArray forecastArray = ((JsonObject) response.body()).getAsJsonArray("DailyForecasts");
                    int minTemp, maxTemp, currentDayIndex = -1;
                    for (int i = 0; i < forecastArray.size(); i++) {

                        JsonElement dailyForecast = forecastArray.get(i);

                        JsonObject icon = ((JsonObject) dailyForecast).getAsJsonObject("Day");
                        String iconName = "i" + icon.get("Icon");
                        int icon_id = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                        forecast_data.add(""+ icon_id);

                        JsonObject temperature = ((JsonObject) dailyForecast).getAsJsonObject("Temperature");

                        minTemp = (int) Math.round(Double.parseDouble(temperature.getAsJsonObject("Minimum").get("Value").toString()));
                        maxTemp = (int) Math.round(Double.parseDouble(temperature.getAsJsonObject("Maximum").get("Value").toString()));

                        forecast_data.add(""+minTemp);
                        forecast_data.add(""+maxTemp);

                        //String tmpTemp = minTemp + "°\n" + maxTemp + "°";
                        //forecast_data.add(tmpTemp);

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
                            //daysTextView.get(tmpIndexId).setText(day[tmpIndexDay]);
                            forecast_data.add(day[tmpIndexDay]);
                        }
                    }

                    weather_data.set(0, forecast_data);

                    if (weather_data.size() == 3)
                        addToSharedPreference();

                }
                else {
                    Toast.makeText(context, "change Api Key", Toast.LENGTH_LONG).show();
                }

            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("FailureDebug", t.getMessage());
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }

    public static void currentConditions(Context context, boolean autocomplete) {

        API_KEY = context.getResources().getString(R.string.accuweather_api_key);

        SharedPreferences prefs = context.getSharedPreferences("Settings", MonitorActivity.MODE_PRIVATE);
        boolean metricValue = prefs.getBoolean("metricValue", true);

        chooseSpFile(context, autocomplete);
        String keyValue = sp.getString("keyValue", "");

        // send current condition request to the api
        IMyService iMyService = RetrofitClient.getAccuweatherClient().create(IMyService.class);
        Call<JsonElement> todoCall = iMyService.currentconditions(keyValue, API_KEY);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {

                    ArrayList<String> currentConditions_data = new ArrayList<>();

                    JsonArray currentConditionsJsonArray = (JsonArray) response.body();
                    String currentConditionsIconName = "i" + currentConditionsJsonArray.get(0).getAsJsonObject().get("WeatherIcon");
                    String weatherText = currentConditionsJsonArray.get(0).getAsJsonObject().get("WeatherText").toString();
                    weatherText = weatherText.substring(1, weatherText.length()-1);

                    String day = currentConditionsJsonArray.get(0).getAsJsonObject().get("IsDayTime").toString();
                    boolean isDayTime = day.equals("true")? true : false;

                    SharedPreferences sp = context.getSharedPreferences("Weather", MonitorActivity.MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putBoolean("IsDayTime", isDayTime);
                    editor.commit();

                    int icon_id = context.getResources().getIdentifier(currentConditionsIconName, "drawable", context.getPackageName());
                    currentConditions_data.add("" + icon_id);

                    int currentConditionsTemp = (int) Math.round(Double.parseDouble("" +
                            currentConditionsJsonArray.get(0).getAsJsonObject().get("Temperature")
                                    .getAsJsonObject().get("Metric").getAsJsonObject().get("Value")));


                    currentConditions_data.add("" + currentConditionsTemp);
                    currentConditions_data.add(weatherText);
                    //String currentForecastText = currentConditionsTemp + "°\n" + weatherText;

                    weather_data.set(1, currentConditions_data);

                    if (weather_data.size() == 3)
                        addToSharedPreference();
                }
                else {
                    Toast.makeText(context, "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


        // send hourly forecast request to the api
        Call<JsonElement> todoCall2 = iMyService.hourlyforecast(keyValue, API_KEY, ""+metricValue);
        todoCall2.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {

                if (response.code() == 200) {

                    ArrayList<String> hourlyForecast_data = new ArrayList<>();

                    JsonArray hourlyJsonArray = response.body().getAsJsonArray();

                    Calendar calendar = Calendar.getInstance();
                    Date date = calendar.getTime();
                    calendar.setTime(date);
                    int currentHour = calendar.get(Calendar.HOUR_OF_DAY);


                    for (int i = 0; i < 4; i++) {

                        int tmpIndex = (i + 1) * 2;
                        int tmpHour = (currentHour + tmpIndex) % 24;

                        String tmpHourStr = tmpHour + ":00";

                        hourlyForecast_data.add(tmpHourStr);

                        JsonObject hourlyJsonObject = hourlyJsonArray.get(tmpIndex).getAsJsonObject();

                        String iconName = "i" + hourlyJsonObject.get("WeatherIcon");
                        int icon_id = context.getResources().getIdentifier(iconName, "drawable", context.getPackageName());
                        hourlyForecast_data.add("" + icon_id);

                        JsonObject temperature = hourlyJsonObject.get("Temperature").getAsJsonObject();

                        int temp = (int) Math.round(Double.parseDouble("" + temperature.get("Value")));

                        //String tmpTemp = temp + "°";
                        //hourlyForecast_data.add(tmpTemp);
                        hourlyForecast_data.add("" + temp);
                    }

                    weather_data.set(2, hourlyForecast_data);

                    if (weather_data.size() == 3)
                        addToSharedPreference();
                }
                else {
                    Toast.makeText(context, "change Api Key", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(context, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        });


    }

    private static void addToSharedPreference() {
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json_data = gson.toJson(weather_data);
        editor.putString("json_data", json_data);
        editor.putLong("last_update", System.currentTimeMillis());
        editor.commit();
    }

    private static void chooseSpFile(Context context, boolean autocomplete) {
        if(autocomplete) {
            sp = context.getSharedPreferences("Weather_autocomplete", MonitorActivity.MODE_PRIVATE);
        }
        else {
            sp = context.getSharedPreferences("Weather", MonitorActivity.MODE_PRIVATE);
        }
    }

}
