package com.buddynsoul.monitor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {


        getUserList(getActivity());


        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_users, container, false);
    }

    private static ArrayList<User> getUserList(Activity activity) {

        ArrayList<User> userList = new ArrayList<>();

        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<JsonElement> todoCall = iMyService.listusers(refreshToken);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    JsonArray usersArray = response.body().getAsJsonArray();

                    for (int i = 0; i < usersArray.size(); i++) {
                        String name = usersArray.get(i).getAsJsonObject().get("name").getAsString();
                        String email = usersArray.get(i).getAsJsonObject().get("email").getAsString();
                        long registrationDate = usersArray.get(i).getAsJsonObject().get("registration_date").getAsLong();
                        User user = new User(name, email, registrationDate);

                        userList.add(user);
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                //loadingDialog.dismissDialog();
                Log.d("Response", "onFailure: " + t.getLocalizedMessage());
            }
        });

        return userList;


    }
}
