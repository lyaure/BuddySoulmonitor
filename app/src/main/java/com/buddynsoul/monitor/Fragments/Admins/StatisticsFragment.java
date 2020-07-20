package com.buddynsoul.monitor.Fragments.Admins;

import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;
import androidx.viewpager.widget.ViewPager;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.buddynsoul.monitor.Adapters.ViewPagerAdapter;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.tbuonomo.viewpagerdotsindicator.DotsIndicator;

import static android.content.Context.MODE_PRIVATE;


public class StatisticsFragment extends Fragment {
    View v;
    private ViewPager viewPager;
    private ViewPagerAdapter adapter;

    public StatisticsFragment() {
        // Required empty public constructor
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v = inflater.inflate(R.layout.fragment_statistics, container, false);


        viewPager = (ViewPager) v.findViewById(R.id.pedometer_viewpager_ID);
        adapter = new ViewPagerAdapter(getFragmentManager(), getActivity(), viewPager);
        viewPager.setAdapter(adapter);

        DotsIndicator dotsIndicator = (DotsIndicator)v.findViewById(R.id.dots_indicator);
        dotsIndicator.setViewPager(viewPager);

        StepsStatsFragment steps = new StepsStatsFragment();
        SleepStatsFragment sleep = new SleepStatsFragment();

        adapter.addFrag(steps, "current");
        adapter.addFrag(sleep, "recent");
        adapter.notifyDataSetChanged();
        getAllUserData();
        return v;
    }

    private void getAllUserData() {
        SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);
        Call<JsonElement> todoCall = iMyService.getAllUserData(refreshToken);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                JsonArray dataArray = response.body().getAsJsonArray();

                for (int i = 0; i < dataArray.size(); i++) {
                    JsonObject user = dataArray.get(i).getAsJsonObject();
                    String userEmail = user.get("email").getAsString();

                    JsonArray userData = user.get("data").getAsJsonArray();
                    for (int j = 0; j < dataArray.size(); j++) {
                        JsonObject data = userData.get(j).getAsJsonObject();
                        int steps = data.get("steps").getAsInt();
                        long asleep = data.get("asleep_time").getAsLong();
                        long wokeUp = data.get("woke_up_time").getAsLong();
                        long deepSleep = data.get("deep_sleep").getAsLong();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to get all users data", Toast.LENGTH_SHORT).show();
            }
        });

    }
}
