package com.buddynsoul.monitor;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.buddynsoul.monitor.Utils.Util;

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

        LinearLayout lougout = (LinearLayout) v.findViewById(R.id.profile_logout_layout_ID);
        lougout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Intent myService = new Intent(getActivity(), StepCounterListener.class);

                SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("logged", false);
                editor.apply();

                getActivity().stopService(myService);

                Intent i = new Intent(getActivity(), LoginActivity.class);
                startActivity(i);
            }
        });


        return v;
    }
}
