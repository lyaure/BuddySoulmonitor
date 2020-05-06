//package com.buddynsoul.monitor.Retrofit;
//
//import retrofit2.Retrofit;
//import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
//import retrofit2.converter.scalars.ScalarsConverterFactory;
//
//public class RetrofitClient {
////    private static RetrofitClient instance;
////    private static Retrofit retrofit;
//    private static Retrofit instance;
//    private static final String BASE_URL = "http://192.168.14.183:3000/";
//
//    public static Retrofit getInstance() {
//        if(instance == null) {
//            instance = new Retrofit.Builder()
//                    .baseUrl(BASE_URL) // In emulator, localhost will changed to 10.0.2.2
//                    .addConverterFactory(ScalarsConverterFactory.create())
//                    .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
//                    .build();
//        }
//        return instance;
//    }
//}

package com.buddynsoul.monitor.Retrofit;

import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class RetrofitClient {

    private static Retrofit retrofit = null;
    //private static final String BASE_URL = "http://192.168.14.183:3000/";
    //private static final String BASE_URL = "http://3.135.240.60:3000/";
    private static final String BASE_URL = "https://3.135.240.60/";

    public static Retrofit getClient() {
        if (retrofit == null) {
            retrofit = new Retrofit.Builder()
                    .baseUrl(BASE_URL)
                    .addConverterFactory(GsonConverterFactory.create())
                    .build();
        }
        return retrofit;
    }


}