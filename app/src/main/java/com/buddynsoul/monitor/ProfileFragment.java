package com.buddynsoul.monitor;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    TextView name, email, registration;

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_profile, container, false);

        SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);

        name = (TextView)v.findViewById(R.id.profile_name_ID);
        name.setText(sp.getString("name", ""));

        email = (TextView)v.findViewById(R.id.profile_email_ID);
        email.setText(sp.getString("email", ""));

        registration = (TextView)v.findViewById(R.id.profile_registrationDate_ID);
        String registrationStr = Util.convertTimeInMillisToDate(sp.getLong("registrationDate", 0));
        registration.setText(registrationStr);

        boolean isAdmin = sp.getBoolean("admin", false);

        LinearLayout settings = (LinearLayout) v.findViewById(R.id.profile_settings_layout_ID);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SettingsFragment();
                MainActivity m = (MainActivity)getActivity();
                m.setFragmentID(R.layout.fragment_setting);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_ID, fragment, "tag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        LinearLayout admin = (LinearLayout) v.findViewById(R.id.profile_admin_layout_ID);
        admin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent i = new Intent(getActivity(), AdminActivity.class);
                startActivity(i);
                getActivity().finish();
            }
        });

        if(isAdmin)
            admin.setVisibility(View.VISIBLE);

        LinearLayout contactUs = (LinearLayout) v.findViewById(R.id.profile_contactUs_layout_ID);
        contactUs.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new ContactUsFragment();
                MainActivity m = (MainActivity)getActivity();
                m.setFragmentID(R.layout.fragment_contact_us);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_ID, fragment, "tag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());

        LinearLayout restore = (LinearLayout) v.findViewById(R.id.profile_restore_layout_ID);
        restore.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                loadingDialog.startLoadingDialog();
                backupData(loadingDialog);
            }
        });

        LinearLayout logout = (LinearLayout) v.findViewById(R.id.profile_logout_layout_ID);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getContext())
                        .setTitle("Log out")
                        .setMessage("Are you sure you want to log out?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                final Intent myService = new Intent(getActivity(), StepCounterListener.class);

                                SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
                                SharedPreferences.Editor editor = sp.edit();
                                editor.putBoolean("logged", false);
                                editor.apply();

                                getActivity().stopService(myService);

                                Intent i = new Intent(getActivity(), LoginActivity.class);
                                startActivity(i);
                                getActivity().finish();
                            }
                        })
                        .setNegativeButton("No", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();
            }
        });


        return v;
    }

    private void backupData(LoadingDialog loadingDialog) {

        SharedPreferences sp = getContext().getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<JsonElement> todoCall = iMyService.backupuserdata(refreshToken);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                loadingDialog.dismissDialog();
                if (response.code() == 200) {
                    String res = response.body().toString();
                    res = res.substring(1, res.length() - 1);

                    if (res.equals("Still no data")) {
                        Toast.makeText(getContext(), "No data to backup", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        JsonArray statsArray = response.body().getAsJsonArray();

                        for (int i = 0; i < statsArray.size(); i++) {
                            JsonObject jsonObject = statsArray.get(i).getAsJsonObject();
                            long date = jsonObject.get("timestamps").getAsLong();
                            int steps = jsonObject.get("steps").getAsInt();
                            String morning_location = jsonObject.get("morning_location").getAsString();
                            String night_location = jsonObject.get("night_location").getAsString();
                            long asleepTime = jsonObject.get("asleep_time").getAsLong();
                            long wokeUpTime = jsonObject.get("woke_up_time").getAsLong();
                            int sleepDuration = (int)(wokeUpTime - asleepTime);
                            int deepSleep = jsonObject.get("deep_sleep").getAsInt();
                            int lightSleep = sleepDuration - deepSleep;

                            Database db = Database.getInstance(getContext());
                            db.insertBackupDay(date, steps, morning_location, night_location,
                                    sleepDuration, asleepTime, wokeUpTime, deepSleep, lightSleep);
                        }
                        Toast.makeText(getContext(), "Data has been backed up", Toast.LENGTH_SHORT).show();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });
    }
}
