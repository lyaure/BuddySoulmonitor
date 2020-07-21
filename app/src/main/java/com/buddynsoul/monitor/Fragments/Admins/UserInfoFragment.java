package com.buddynsoul.monitor.Fragments.Admins;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.R;
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
        private String userDateRegistration;

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
                userDateRegistration = bundle.getString("registration", "");
            }

            TextView userNameView = (TextView) v.findViewById(R.id.userNameInfo_ID);
            userNameView.setText(userName);

            userAdminPermissionSwitch = (Switch) v.findViewById(R.id.userAdminPermission_ID);
            userAdminPermissionSwitch.setChecked(userAdminPermission);

            userAdminPermissionSwitch.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    userAdminPermissionSwitch.setChecked(userAdminPermission);

                    String msg;
                    if(!userAdminPermission) {
                        msg = "Are you sure to grant admin permission to this user?";
                    }
                    else {
                        msg = "Are you sure to remove admin permission to this user?";
                    }

                    new AlertDialog.Builder(getContext())
                            .setTitle("Permission")
                            .setMessage(msg)
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    updateUserPermission(getActivity());
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

            TextView mail = (TextView)v.findViewById(R.id.userEmail_ID);
            mail.setText(userEmail);

            TextView registration = (TextView)v.findViewById(R.id.userRegistration_ID);
            registration.setText(userDateRegistration);

            ImageButton deleteUserBtn = (ImageButton)v.findViewById(R.id.deleteUser_ID);
            deleteUserBtn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    new AlertDialog.Builder(getContext())
                            .setTitle("Delete")
                            .setMessage("Are you sure to delete this user?")
                            .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                                @Override
                                public void onClick(DialogInterface dialog, int which) {
                                    deleteUser(getActivity());
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
                    Toast.makeText(getContext(), "An error occurred when updating permission", Toast.LENGTH_SHORT).show();
                }
            });
        }

        private void deleteUser(Activity activity) {
            SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
            String refreshToken = sp.getString("refreshToken", "");

            IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

            Call<String> todoCall = iMyService.deleteUser(refreshToken, userEmail);
            todoCall.enqueue(new Callback<String>() {
                @Override
                public void onResponse(Call<String> call, Response<String> response) {
                    if (response.code() == 200) {
                        Toast.makeText(getContext(), "User has been deleted", Toast.LENGTH_SHORT).show();

                        Fragment fragment = new UsersListFragment();

                        getActivity().getSupportFragmentManager().beginTransaction()
                                .replace(R.id.admin_container_ID, fragment, "tag")
                                .addToBackStack(null)
                                .commit();
                    }
                }

                @Override
                public void onFailure(Call<String> call, Throwable t) {
                    Toast.makeText(getContext(), "An error occurred when deleting user", Toast.LENGTH_SHORT).show();
                }
            });
        }
}
