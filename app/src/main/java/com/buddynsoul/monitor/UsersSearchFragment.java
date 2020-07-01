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
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.INPUT_METHOD_SERVICE;
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
                hideKeyboard(v);

                userStatList.clear();
                adapter.notifyDataSetChanged();

                String email = autoCompleteTextView.getText().toString().trim();

                if(email.equals("")) {
                    Toast.makeText(getContext(), "User's mail is empty!", Toast.LENGTH_SHORT).show();
                    return;
                }

                TextView fromDate = (TextView)getActivity().findViewById(R.id.fromDate_txtv_ID);
                TextView toDate = (TextView)getActivity().findViewById(R.id.toDate_txtv_ID);

                long start = -1, end = -1;

                if(!fromDate.getText().equals("") && !toDate.getText().equals("")) {
                    start = Util.convertDateToTimeInMillis((String) fromDate.getText());
                    end = Util.convertDateToTimeInMillis((String) toDate.getText());
                }
                getUserData(getActivity(), email, start, end);
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

    private void getUserData(Activity activity, String email, long start, long end) {

        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

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
                        String res = response.body().toString();
                        res = res.substring(1, res.length()-1);

                        if (res.equals("No data between these dates")) {
                            Toast.makeText(getContext(), "No data between these dates", Toast.LENGTH_SHORT).show();
                        }
                        else if (res.equals("Still no data")) {
                            Toast.makeText(getContext(), "Still no data", Toast.LENGTH_SHORT).show();
                        }
                        else {
                            JsonArray statsArray = response.body().getAsJsonArray();

                            for (int i = 0; i < statsArray.size(); i++) {
                                JsonObject jsonObject = statsArray.get(i).getAsJsonObject();
                                long date = jsonObject.get("timestamps").getAsLong();
                                String dateStr = convertTimeInMillisToDate(date);
                                int steps = jsonObject.get("steps").getAsInt();
                                long asleepTime = jsonObject.get("asleep_time").getAsLong();
                                long wokeUpTime = jsonObject.get("woke_up_time").getAsLong();
                                int deepSleep = jsonObject.get("deep_sleep").getAsInt();
                                String morning_location = jsonObject.get("morning_location").getAsString();
                                String night_location = jsonObject.get("night_location").getAsString();
                                int stepGoal = jsonObject.get("step_goal").getAsInt();
                                int sleepGoal = jsonObject.get("sleep_goal").getAsInt();

                                UserStat stat = new UserStat(dateStr, steps, stepGoal, asleepTime, wokeUpTime,
                                        deepSleep, sleepGoal, morning_location, night_location);

                                userStatList.add(stat);
                            }
                            adapter.notifyDataSetChanged();
                        }
                    }
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

    private void hideKeyboard(View v) {
        InputMethodManager inputMethodManager = (InputMethodManager) getContext().getSystemService(INPUT_METHOD_SERVICE);
        inputMethodManager.hideSoftInputFromWindow(v.getApplicationWindowToken(),0);
    }
}
