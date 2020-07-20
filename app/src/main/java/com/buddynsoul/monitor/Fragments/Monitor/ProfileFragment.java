package com.buddynsoul.monitor.Fragments.Monitor;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Activities.AdminActivity;
import com.buddynsoul.monitor.Activities.LoginActivity;
import com.buddynsoul.monitor.Activities.MonitorActivity;
import com.buddynsoul.monitor.Dialogs.LoadingDialog;
import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.MonitorService;
import com.buddynsoul.monitor.Utils.Util;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ProfileFragment extends Fragment {
    private TextView name, email, registration;
    private SharedPreferences sp;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    public ProfileFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_profile, container, false);

        sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);

        name = (TextView)v.findViewById(R.id.profile_name_ID);
        name.setText(sp.getString("name", ""));

        email = (TextView)v.findViewById(R.id.profile_email_ID);
        email.setText(sp.getString("email", ""));
        email.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final EditText input = new EditText(getActivity());
                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                input.setLayoutParams(lp);
                input.setHint("Enter new mail");

                new AlertDialog.Builder(getActivity())
                                .setTitle("Edit email")
                                .setView(input)
                                .setPositiveButton("SEND", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {
                                        String newEmail = input.getText().toString().trim();
                                        if (!newEmail.matches(EMAIL_PATTERN)) {
                                            Toast.makeText(getContext(), "Email is not valid", Toast.LENGTH_SHORT).show();
                                            return;
                                        }

                                        if (newEmail.equals("")) {
                                            Toast.makeText(getContext(), "Email can not be empty", Toast.LENGTH_SHORT).show();
                                            return;
                                        }
                                        updateEmail(getActivity(), newEmail);
                                    }
                                })
                                .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                    @Override
                                    public void onClick(DialogInterface dialog, int which) {

                                    }
                                })
                                .show();
            }
        });

        registration = (TextView)v.findViewById(R.id.profile_registrationDate_ID);
        String registrationStr = Util.convertTimeInMillisToDate(sp.getLong("registrationDate", 0));
        registration.setText(registrationStr);

        boolean isAdmin = sp.getBoolean("admin", false);

        LinearLayout settings = (LinearLayout) v.findViewById(R.id.profile_settings_layout_ID);
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Fragment fragment = new SettingsFragment();
                MonitorActivity activity = (MonitorActivity)getActivity();
                activity.setFragmentID(R.layout.fragment_setting);
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
                MonitorActivity activity = (MonitorActivity)getActivity();
                activity.setAdminButtonPressed();
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
                MonitorActivity m = (MonitorActivity)getActivity();
                m.setFragmentID(R.layout.fragment_contact_us);
                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_ID, fragment, "tag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        LinearLayout delete = (LinearLayout) v.findViewById(R.id.profile_deleteAccount_layout_ID);
        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new AlertDialog.Builder(getActivity())
                        .setTitle("Delete Account")
                        .setMessage("Are you sure you want to delete your account? " +
                                "\nAll your data will be loss, this will immediately log you out and" +
                                " you will not be able to log in again.")
                        .setPositiveButton("DELETE", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO ---- delete account from DB
                                deleteAccount(getActivity());
                            }
                        })
                        .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        })
                        .show();

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
                                logout();
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

    private void logout(){
        final Intent myService = new Intent(getActivity(), MonitorService.class);

        SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();
        editor.putBoolean("logged", false);
        editor.apply();

        getActivity().stopService(myService);

        MonitorActivity activity = (MonitorActivity)getActivity();
        activity.setLogoutButtonPressed();

        Intent i = new Intent(getActivity(), LoginActivity.class);
        startActivity(i);
        getActivity().finish();
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
                            int stepGoal = jsonObject.get("step_goal").getAsInt();
                            int sleepGoal = jsonObject.get("sleep_goal").getAsInt();

                            Database db = Database.getInstance(getContext());
                            db.insertBackupDay(date, steps, stepGoal, morning_location, night_location,
                                    sleepDuration, asleepTime, wokeUpTime, deepSleep, lightSleep, sleepGoal);
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

    private void deleteAccount(Activity activity) {
        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<String> todoCall = iMyService.deleteAccount(refreshToken);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.code() == 200) {
                    logout();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                Toast.makeText(getContext(), "Failed to delete your account", Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void updateEmail(Activity activity, String newEmail) {
        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());
        loadingDialog.startLoadingDialog();

        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<String> todoCall = iMyService.sendVerificationCode(refreshToken, newEmail);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if (response.code() == 200) {
                    loadingDialog.dismissDialog();

                    final EditText input = new EditText(getActivity());
                    LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
                    input.setLayoutParams(lp);

                    input.setHint("Enter verification code");
                    new AlertDialog.Builder(getActivity())
                            .setTitle("Email confirmation")
                            .setView(input)
                            .setPositiveButton("CONFIRM", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                    String verificationCode = input.getText().toString().trim();
                                    if (verificationCode.equals("")) {
                                        Toast.makeText(getContext(), "Verification code can not be empty!", Toast.LENGTH_SHORT).show();
                                    }
                                    else {
                                        loadingDialog.startLoadingDialog();

                                        Call<JsonElement> todoCall = iMyService.updateEmail(refreshToken, verificationCode);
                                        todoCall.enqueue(new Callback<JsonElement>() {
                                            @Override
                                            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                                                if (response.code() == 200) {
                                                    loadingDialog.dismissDialog();

                                                    // update refresh token
                                                    String refreshToken = response.body().getAsJsonObject().get("refreshToken").getAsString();
                                                    SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
                                                    SharedPreferences.Editor editor = sp.edit();
                                                    editor.putString("refreshToken", refreshToken);
                                                    editor.putString("email", newEmail);
                                                    editor.apply();

                                                    // update email textView
                                                    email.setText(newEmail);
                                                }
                                                else {
                                                    loadingDialog.dismissDialog();
                                                    Toast.makeText(getContext(), "Verification code is not correct", Toast.LENGTH_SHORT).show();
                                                }
                                            }

                                            @Override
                                            public void onFailure(Call<JsonElement> call, Throwable t) {
                                                loadingDialog.dismissDialog();
                                                Toast.makeText(getContext(), "Failed to update the email", Toast.LENGTH_SHORT).show();
                                            }
                                        });
                                    }
                                }
                            })
                            .setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {

                                }
                            })
                            .show();
                }
                else if (response.code() == 409) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(getContext(), "Please choose an other new email", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(getContext(), "Failed to send verification code", Toast.LENGTH_SHORT).show();
            }
        });



    }
}
