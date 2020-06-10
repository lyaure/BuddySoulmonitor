package com.buddynsoul.monitor;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.fragment.app.Fragment;


    /**
     * A simple {@link Fragment} subclass.
     * Use the  factory method to
     * create an instance of this fragment.
     */
    public class UserInfoFragment extends Fragment {

        private String userName;
        private String userEmail;

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
            }

            TextView userNameView = (TextView) v.findViewById(R.id.userNameInfo_ID);
            userNameView.setText(userName);

            return v;
        }
}
