package com.buddynsoul.monitor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.WeatherUtils;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class UsersFragment extends Fragment {

    private ArrayList<User> userList = new ArrayList<>();
    private UserAdapter adapter;

    public UsersFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users, container, false);

        ListView userListView = (ListView) v.findViewById(R.id.userList_ID);
        adapter = new UserAdapter(getContext(), userList);
        userListView.setAdapter(adapter);
        getUserList(getActivity());

        return v;
    }

    private void getUserList(Activity activity) {

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
                        String registrationStr = convertTimeInMillisToDate(registrationDate);
                        User user = new User(name, email, registrationStr);

                        userList.add(user);
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("Response", "onFailure: " + t.getLocalizedMessage());
            }
        });
        //return userList;
    }

    private String convertTimeInMillisToDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(timeInMillis));
    }
}
