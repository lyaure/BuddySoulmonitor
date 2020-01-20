package com.buddynsoul.monitor;

import android.content.Context;
import android.net.Uri;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Scanner;

public class NetworkUtils {
    private static final String TAG = "NetworkUtils";

private final static String GEOPOSITION_BASE_URL=
            "https://dataservice.accuweather.com/locations/v1/cities/geoposition/search?";

private final static String FORECAST_BASE_URL=
            "https://dataservice.accuweather.com/forecasts/v1/daily/5day";

private final static String CURRENT_CONDITIONS_BASE_URL=
            "https://dataservice.accuweather.com/currentconditions/v1";

private final static String HOURLY_FORECAST_BASE_URL=
            "https://dataservice.accuweather.com/forecasts/v1/hourly/12hour";

private final static String AUTOCOMPLETE_BASE_URL=
        "https://dataservice.accuweather.com/locations/v1/cities/autocomplete?";


    private final static String PARAM_API_KEY = "apikey";

    private final static String PARAM_LOCATION = "q";

    // to display units in Celsius
    private final static String PARAM_METRIC = "metric";

    private final static String METRIC_VALUE = "true";

    // requestType: "geoposition" , "forecast", "currentconditions"
    public static URL buildUrlForWeather(Context context, String requestType, String params) {

        final String API_KEY = context.getString(R.string.accuweather_api_key);

        Uri builtUri = null;

        if (requestType.equals("geoposition")) {
            // here params is the geoloaction like this format "31.768370,35.193890"

            // http://dataservice.accuweather.com/locations/v1/cities/geoposition/
            // search?apikey=sQ4Urdjd2y0wIofeyDZDUKzwK4FMxQEk&q=31.768370%2C35.193890

            builtUri = Uri.parse(GEOPOSITION_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_API_KEY, API_KEY)
                    .appendQueryParameter(PARAM_LOCATION, params)
                    .build();
        }
        else if (requestType.equals("forecast")) {
            // here params is the "uniqueId"
            String forecastUrlBase = FORECAST_BASE_URL + "/" + params + "?";
            builtUri = Uri.parse(forecastUrlBase).buildUpon()
                    .appendQueryParameter(PARAM_API_KEY, API_KEY)
                    .appendQueryParameter(PARAM_METRIC, METRIC_VALUE)
                    .build();
        }
        else if (requestType.equals("currentconditions")) {
            // here params is the "uniqueId"
            String forecastUrlBase = CURRENT_CONDITIONS_BASE_URL + "/" + params + "?";
            builtUri = Uri.parse(forecastUrlBase).buildUpon()
                    .appendQueryParameter(PARAM_API_KEY, API_KEY)
                    .build();
        }
        else if (requestType.equals("hourlyforecast")){
            // here params is the "uniqueId"
            String forecastUrlBase = HOURLY_FORECAST_BASE_URL + "/" + params + "?";
            builtUri = Uri.parse(forecastUrlBase).buildUpon()
                    .appendQueryParameter(PARAM_API_KEY, API_KEY)
                    .appendQueryParameter(PARAM_METRIC, METRIC_VALUE)
                    .build();
        }
        else if (requestType.equals("autocomplete")){
            // here params is the "autocomplete" search field
            builtUri = Uri.parse(AUTOCOMPLETE_BASE_URL).buildUpon()
                    .appendQueryParameter(PARAM_API_KEY, API_KEY)
                    .appendQueryParameter(PARAM_LOCATION, params)
                    .build();
        }

        URL url = null;
        try {
            url = new URL(builtUri.toString());
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

        Log.i(TAG, "buildUrlForWeather: url: "+url);
        return url;
    }

    public static String getResponseFromHttpUrl(URL url) throws IOException {
        HttpURLConnection urlConnection = (HttpURLConnection) url.openConnection();
        try {
            InputStream in  = urlConnection.getInputStream();

            Scanner scanner = new Scanner(in);
            scanner.useDelimiter("\\A");

            boolean hasInput = scanner.hasNext();
            if(hasInput) {
                return scanner.next();
            } else {
                return null;
            }
        } finally {
            urlConnection.disconnect();
        }
    }

}
