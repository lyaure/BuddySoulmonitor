package com.buddynsoul.monitor.Retrofit;

import io.reactivex.Observable;
import okhttp3.RequestBody;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Url;

public interface IMyService {

    @POST("register")
    @FormUrlEncoded
    Observable<String> registerUser(@Field("email") String email,
                                    @Field("name") String name,
                                    @Field("password") String password);

    @POST("login")
    @FormUrlEncoded
    Observable<String> loginUser(@Field("email") String email,
                                    @Field("password") String password);

    @POST("sendresetmail")
    @FormUrlEncoded
    Observable<String> resetUserPassword(@Field("email") String email);

    @POST("senddata/{refreshToken}")
    @FormUrlEncoded
    Observable<String> sendData(@Path("refreshToken") String refreshToken,
                                @Field("data") String data);
}
