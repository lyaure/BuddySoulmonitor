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
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;


public class UsersSearchFragment extends Fragment {

    private ArrayList<UserStat> userStatList = new ArrayList<>();
    private ArrayList<String> userEmailList;
    private UserStatAdapter adapter;
    private AutoCompleteTextView autoCompleteTextView;

    public UsersSearchFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users_search, container, false);

        ListView userListView = (ListView) v.findViewById(R.id.userStatList_ID);
        autoCompleteTextView = (AutoCompleteTextView)v.findViewById(R.id.autoCompleteEmail_ID);

        SharedPreferences sp = getContext().getSharedPreferences("admin", MainActivity.MODE_PRIVATE);
        Gson gson = new Gson();
        String json_data = sp.getString("emailList", "");

        if (!json_data.equals("")) {
            Type type = new TypeToken<ArrayList<String> >(){}.getType();
            userEmailList = gson.fromJson(json_data, type);

            ArrayAdapter<String> autoCompleteAdapter = new ArrayAdapter<String>
                    (getActivity(), android.R.layout.select_dialog_item, userEmailList);

            autoCompleteTextView.setThreshold(1);
            autoCompleteTextView.setAdapter(autoCompleteAdapter);
        }

        adapter = new UserStatAdapter(getContext(), userStatList);
        userListView.setAdapter(adapter);

        Button searchBtn = (Button)v.findViewById(R.id.searchBtn_ID);
        searchBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getUserData(getActivity());
            }
        });

        Button fromDate = (Button)v.findViewById(R.id.fromDate_ID);
        fromDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialogFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "fromDate");
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "date");
            }
        });

        Button toDate = (Button)v.findViewById(R.id.toDate_ID);
        toDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerFragment dialogFragment = new DatePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "toDate");
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getActivity().getSupportFragmentManager(), "date");
            }
        });


        return v;
    }

    private void getUserData(Activity activity) {

        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        String email = autoCompleteTextView.getText().toString().trim();
        long start = -1;
        long end = -1;

        Call<JsonElement> todoCall = iMyService.databetweentwodates(refreshToken, email, start, end);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    // todo check if response.body() == "User's email doesn't exist"
                    String checkResponse = response.body().toString();
                    checkResponse = checkResponse.substring(1, checkResponse.length()-1);
                    if(checkResponse.equals("User's email doesn't exist")) {
                        Toast.makeText(getActivity(), "User's email doesn't exist", Toast.LENGTH_LONG).show();
                    }
                    else {
                        JsonArray statsArray = response.body().getAsJsonArray();

                        for (int i = 0; i < statsArray.size(); i++) {
                            long date = statsArray.get(i).getAsJsonObject().get("timestamps").getAsLong();
                            String dateStr = convertTimeInMillisToDate(date);
                            int steps = statsArray.get(i).getAsJsonObject().get("steps").getAsInt();
                            int sleepingTime = statsArray.get(i).getAsJsonObject().get("sleeping_time").getAsInt();
                            String morning_location = statsArray.get(i).getAsJsonObject().get("morning_location").getAsString();
                            String night_location = statsArray.get(i).getAsJsonObject().get("night_location").getAsString();
                            UserStat stat = new UserStat(dateStr, steps, sleepingTime, morning_location, night_location);

                            userStatList.add(stat);
                        }
                    }
                    adapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("Response", "onFailure: " + t.getLocalizedMessage());
            }
        });
    }

    private String convertTimeInMillisToDate(long timeInMillis) {
        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy");
        return sdf.format(new Date(timeInMillis));
    }
}
