package com.buddynsoul.monitor.Fragments.Monitor;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.buddynsoul.monitor.Dialogs.LoadingDialog;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.google.android.material.textfield.TextInputEditText;
import com.google.android.material.textfield.TextInputLayout;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Date;

import androidx.fragment.app.Fragment;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import static android.content.Context.MODE_PRIVATE;

public class ContactUsFragment extends Fragment {

    public ContactUsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v =  inflater.inflate(R.layout.fragment_contact_us, container, false);

        TextInputEditText subject = (TextInputEditText)v.findViewById(R.id.contact_us_subject_ID);
        EditText message = (EditText)v.findViewById(R.id.contact_us_message_ID);

        TextInputLayout subject_layout = v.findViewById(R.id.contact_us_subject_layout_ID);

        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(getActivity());

        Button send = (Button)v.findViewById(R.id.contact_us_sendButton_ID);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                subject_layout.setError(null);

                if(TextUtils.isEmpty(subject.getText().toString().trim()))
                {
                    subject_layout.setError("Subject required");
                    subject.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(message.getText().toString().trim()))
                {
                    Toast.makeText(getContext(), "Message to send is empty", Toast.LENGTH_SHORT).show();
                    return;
                }

                loadingDialog.startLoadingDialog();
                sendData(loadingDialog, subject.getText().toString(), message.getText().toString());

                Fragment fragment = new ProfileFragment();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_ID, fragment, "tag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        return v;
    }

    private void sendData(LoadingDialog loadingDialog, String subject, String message) {

        SharedPreferences sp = getActivity().getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");
        String email = sp.getString("email", "");

        String timestamps = new Date(System.currentTimeMillis()).toString();

        String messageToSend = preprocessData(timestamps, email, subject, message);

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<String> todoCall = iMyService.contactUs(refreshToken, messageToSend);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                loadingDialog.dismissDialog();
                if (response.code() == 200) {
                    Toast.makeText(getContext(), "Message have been send", Toast.LENGTH_SHORT).show();

                    Fragment fragment = new ProfileFragment();

                    getActivity().getSupportFragmentManager().beginTransaction()
                            .replace(R.id.container_ID, fragment, "tag")
                            .addToBackStack(null)
                            .commit();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(getContext(), "An error occurred", Toast.LENGTH_SHORT).show();
            }
        });

    }

    private String preprocessData(String timestamps, String email, String subject, String message) {

        JSONObject json = new JSONObject();

        try {
            json.put("timestamps", timestamps);
            json.put("email", email);
            json.put("subject", subject);
            json.put("message", message);
        } catch (JSONException e) {
            //e.printStackTrace();
        }
        return json.toString();
    }
}
