//package com.buddynsoul.monitor.Retrofit;
//
//import io.reactivex.Observable;
//import okhttp3.RequestBody;
//import retrofit2.http.Body;
//import retrofit2.http.Field;
//import retrofit2.http.FormUrlEncoded;
//import retrofit2.http.GET;
//import retrofit2.http.Header;
//import retrofit2.http.POST;
//import retrofit2.http.Path;
//import retrofit2.http.Url;
//
//public interface IMyService {
//
//    @POST("register")
//    @FormUrlEncoded
//    Observable<String> registerUser(@Field("email") String email,
//                                    @Field("name") String name,
//                                    @Field("password") String password);
//
//    @POST("login")
//    @FormUrlEncoded
//    Observable<String> loginUser(@Field("email") String email,
//                                    @Field("password") String password);
//
//    @POST("sendresetmail")
//    @FormUrlEncoded
//    Observable<String> resetUserPassword(@Field("email") String email);
//
//    @POST("senddata/{refreshToken}")
//    @FormUrlEncoded
//    Observable<String> sendData(@Path("refreshToken") String refreshToken,
//                                @Field("data") String data);
//}

package com.buddynsoul.monitor.Retrofit;

import com.google.gson.JsonElement;

import retrofit2.Call;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface IMyService {

    @POST("register")
    @FormUrlEncoded
    Call<String> registerUser(@Field("email") String email,
                                  @Field("name") String name,
                                  @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Call<JsonElement> loginUser(@Field("email") String email,
                                    @Field("password") String password);

    @POST("sendresetmail")
    @FormUrlEncoded
    Call<String> resetUserPassword(@Field("email") String email);

    @POST("senddata/{refreshToken}")
    @FormUrlEncoded
    Call<String> sendData(@Path("refreshToken") String refreshToken,
                                @Field("data") String data);

    @POST("contactus/{refreshToken}")
    @FormUrlEncoded
    Call<String> contactUs(@Path("refreshToken") String refreshToken,
                          @Field("message") String message);

    @POST("listusers/{refreshToken}")
    @FormUrlEncoded
    Call<JsonElement> listusers(@Path("refreshToken") String refreshToken,
                                @Field("status") boolean status);

    @POST("databetweentwodates/{refreshToken}")
    @FormUrlEncoded
    Call<JsonElement> databetweentwodates(@Path("refreshToken") String refreshToken,
                                     @Field("email") String email,
                                     @Field("start") long start,
                                     @Field("end") long end);

    @POST("backupuserdata/{refreshToken}")
    Call<JsonElement> backupuserdata(@Path("refreshToken") String refreshToken);

    @POST("updatepermission/{refreshToken}")
    @FormUrlEncoded
    Call<String> updatepermission(@Path("refreshToken") String refreshToken,
                                     @Field("email") String email,
                                     @Field("allow") boolean allow);

    @POST("deleteuser/{refreshToken}")
    @FormUrlEncoded
    Call<String> deleteUser(@Path("refreshToken") String refreshToken,
                                  @Field("email") String email);

    @GET("deleteaccount/{refreshToken}")
    Call<String> deleteAccount(@Path("refreshToken") String refreshToken);


    @POST("sendverificationcode/{refreshToken}")
    @FormUrlEncoded
    Call<String> sendVerificationCode(@Path("refreshToken") String refreshToken,
                                      @Field("newEmail") String newEmail);

    @POST("updateemail/{refreshToken}")
    @FormUrlEncoded
    Call<JsonElement> updateEmail(@Path("refreshToken") String refreshToken,
                                @Field("verificationCode") String verificationCode);

    // AccuWeather Request

    @GET("locations/v1/cities/geoposition/search")
    Call<JsonElement> geoposition(@Query("apikey") String apikey,
                                  @Query ("q") String location);

    @GET("forecasts/v1/daily/5day/{keyValue}")
    Call<JsonElement> forecast(@Path("keyValue") String keyValue,
                          @Query("apikey") String apikey,
                          @Query("metric") String metric);

    @GET("currentconditions/v1/{keyValue}")
    Call<JsonElement> currentconditions(@Path("keyValue") String keyValue,
                                   @Query("apikey") String apikey);

    @GET("forecasts/v1/hourly/12hour/{keyValue}")
    Call<JsonElement>hourlyforecast(@Path("keyValue") String keyValue,
                                @Query("apikey") String apikey,
                                @Query("metric") String metric);

    @GET("locations/v1/cities/autocomplete")
    Call<JsonElement> autocomplete(@Query("apikey") String apikey,
                              @Query("q") String location);
}