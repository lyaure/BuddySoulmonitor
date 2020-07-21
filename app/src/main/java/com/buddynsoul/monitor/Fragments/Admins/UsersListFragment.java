package com.buddynsoul.monitor.Fragments.Admins;

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
import android.widget.AutoCompleteTextView;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import com.buddynsoul.monitor.Activities.MonitorActivity;
import com.buddynsoul.monitor.Adapters.UserAdapter;
import com.buddynsoul.monitor.Objects.User;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import java.util.ArrayList;
import java.util.Date;

import static android.content.Context.MODE_PRIVATE;


/**
 * A simple {@link Fragment} subclass.
 * Use the  factory method to
 * create an instance of this fragment.
 */
public class UsersListFragment extends Fragment {

    private ArrayList<User> userList = new ArrayList<>();
    private UserAdapter adapter;
    private AutoCompleteTextView autoCompleteUserSearch;

    public UsersListFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_users_list, container, false);

        final RadioGroup userStatus = v.findViewById(R.id.userStatusRadioGroup_ID);
        RadioButton confirmedUser = (RadioButton)v.findViewById(R.id.confirmedUserBtn_ID);
        RadioButton notConfirmedUser = (RadioButton) v.findViewById(R.id.notConfirmedUserBtn_ID);

        confirmedUser.setChecked(true);

        ListView userListView = (ListView) v.findViewById(R.id.userList_ID);
        adapter = new UserAdapter(getContext(), userList);
        userListView.setAdapter(adapter);
        getUserList(getActivity(), true);


        userStatus.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                if(checkedId == R.id.confirmedUserBtn_ID) {
                    getUserList(getActivity(), true);
                }
                else {
                    getUserList(getActivity(), false);
                }
            }
        });

        userListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

                User user = userList.get(position);

                Fragment userInfo = new UserInfoFragment();
                Bundle bundle = new Bundle();
                bundle.putString("userName", user.getName());
                bundle.putString("userEmail", user.getEmail());
                bundle.putBoolean("admin", user.isAdmin());
                bundle.putString("registration", user.getRegistrationDate());
                userInfo.setArguments(bundle);

                FragmentTransaction transaction = getFragmentManager().beginTransaction();
                transaction.replace(R.id.admin_container_ID, userInfo); // give your fragment container id in first parameter
                transaction.addToBackStack(null);  // if written, this transaction will be added to backstack
                transaction.commit();
            }
        });

        return v;
    }

    private void getUserList(Activity activity, boolean status) {

        if(userList != null) {
            userList.clear();
        }

        SharedPreferences sp = activity.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<JsonElement> todoCall = iMyService.listusers(refreshToken, status);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {

                    ArrayList<String> emailList = new ArrayList<>();

                    JsonArray usersArray = response.body().getAsJsonArray();

                    if(usersArray.size() == 0) {
                        adapter.notifyDataSetChanged();
                        Toast.makeText(getContext(), "List of not confirmed users is empty", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        for (int i = 0; i < usersArray.size(); i++) {
                            String name = usersArray.get(i).getAsJsonObject().get("name").getAsString();
                            String email = usersArray.get(i).getAsJsonObject().get("email").getAsString();
                            long registrationDate = usersArray.get(i).getAsJsonObject().get("registration_date").getAsLong();

                            String registrationStr;
                            if(status) {
                                registrationStr = Util.convertTimeInMillisToDate(registrationDate);
                            }
                            else {
                                registrationStr = String.valueOf(new Date(registrationDate));
                            }

                            boolean admin = usersArray.get(i).getAsJsonObject().get("admin").getAsBoolean();
                            User user = new User(name, email, registrationStr, admin);

                            emailList.add(email);
                            userList.add(user);
                        }
                        adapter.notifyDataSetChanged();

                        // save users email to the shared preferences
                        Gson gson = new Gson();
                        String emailList_str = gson.toJson(emailList);

                        SharedPreferences sp = getContext().getSharedPreferences("admin", MonitorActivity.MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("emailList", emailList_str);
                        editor.apply();
                    }
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                Log.d("Response", "onFailure: " + t.getLocalizedMessage());
            }
        });
    }
}
