package com.buddynsoul.monitor.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    private static Retrofit retrofit_accuwather = null;
    private static final String BASE_URL = "http://3.12.111.177/";

    private static final String BASE_URL_ACCUWEATHER = "http://dataservice.accuweather.com/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }

    public static Retrofit getAccuweatherClient() {
        if (retrofit_accuwather == null) {
            retrofit_accuwather = new Retrofit.Builder()
                    .baseUrl(BASE_URL_ACCUWEATHER)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit_accuwather;
    }


}