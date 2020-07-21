package com.buddynsoul.monitor.Activities;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Dialogs.LoadingDialog;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.android.material.textfield.TextInputLayout;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextView email;

    IMyService iMyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        // Init Service
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        // Init view
        email = (TextView) findViewById(R.id.txtv_email_ID);

        TextInputLayout email_layout = findViewById(R.id.password_layout_ID);

        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(ResetPasswordActivity.this);

        Button reset = (Button) findViewById(R.id.resetBtn_ID);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email_layout.setError(null);

                if (TextUtils.isEmpty(email.getText().toString().trim())) {
                    email_layout.setError("Email required");
                    email.requestFocus();
                    return;
                }

                if (!Util.isNetworkAvailable(ResetPasswordActivity.this)) {
                    Toast.makeText(ResetPasswordActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                    return;
                }

                loadingDialog.startLoadingDialog();
                resetUserPassword(email.getText().toString().trim(), loadingDialog);
            }
        });
    }

    private void resetUserPassword(final String email, LoadingDialog loadingDialog) {
        final Intent i = new Intent(ResetPasswordActivity.this, LoginActivity.class);

        Call<String> todoCall = iMyService.resetUserPassword(email);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                loadingDialog.dismissDialog();
                String msg = "If a matching account was found an email was sent to "
                        + email + " to allow you to reset your password.";
                Toast.makeText(ResetPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                startActivity(i);
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(ResetPasswordActivity.this, "Something wrong happened", Toast.LENGTH_SHORT).show();
                Log.d("Response", "onFailure: "+t.getLocalizedMessage());
            }

        });

    }

    @Override
    public void onBackPressed(){
        finish();
    }
}
