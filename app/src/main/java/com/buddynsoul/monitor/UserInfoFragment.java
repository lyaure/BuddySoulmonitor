package com.buddynsoul.monitor;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;

import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;


/**
     * A simple {@link Fragment} subclass.
     * Use the  factory method to
     * create an instance of this fragment.
     */
    public class UserInfoFragment extends Fragment {

        private String userName;
        private String userEmail;
        private boolean userAdminPermission;
        private Switch userAdminPermissionSwitch;

        public UserInfoFragment() {
            // Required empty public constructor
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            // Inflate the layout for this fragment
            View v = inflater.inflate(R.layout.fragment_user_info, container, false);

            Bundle bundle = this.getArguments();
            if (bundle != null) {
                userName = bundle.getString("userName", "");
                userEmail = bundle.getString("userEmail", "");
                userAdminPermission = bundle.getBoolean("admin", false);
            }

            TextView userNameView = (TextView) v.findViewById(R.id.userNameInfo_ID);
            userNameView.setText(userName);

            userAdminPermissionSwitch = (Switch) v.findViewById(R.id.userAdminPermission_ID);
            userAdminPermissionSwitch.setChecked(userAdminPermission);

            userAdminPermissionSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    updateUserPermission(getActivity());
                }
            });

            return v;
        }

        private void updateUserPermission(Activity activity) {
            SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
            String refreshToken = sp.getString("refreshToken", "");

            IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

            Call<String> todoCall = iMyService.updatepermission(refreshToken, userEmail, !userAdminPermission);
            todoCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() == 200) {
                        userAdminPermission = !userAdminPermission;
                        userAdminPermissionSwitch.setChecked(userAdminPermission);
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {

                }
            });
        }
}
