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

public class SignupActivity extends AppCompatActivity {
    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView password;
    private TextView confirmPassword;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

//    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])" +
//            "(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

    IMyService iMyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Init Service
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(SignupActivity.this);

        Button signUp = (Button) findViewById(R.id.signupBtn_ID);
        signUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstName = (TextView)findViewById(R.id.txtv_firstName_ID);
                lastName = (TextView)findViewById(R.id.txtv_lastName_ID);
                email = (TextView)findViewById(R.id.txtv_email_ID);
                password = (TextView)findViewById(R.id.txtv_password_ID);
                confirmPassword = (TextView)findViewById(R.id.txtv_confirmPassword_ID);

                TextInputLayout firstName_layout = findViewById(R.id.firstName_layout_ID);
                TextInputLayout lastName_layout = findViewById(R.id.lastName_layout_ID);
                TextInputLayout email_layout = findViewById(R.id.email_layout_ID);
                TextInputLayout password_layout = findViewById(R.id.password_layout_ID);
                TextInputLayout confirmPassword_layout = findViewById(R.id.confirmPassword_layout_ID);

                firstName_layout.setError(null);
                lastName_layout.setError(null);
                email_layout.setError(null);
                password_layout.setError(null);
                confirmPassword_layout.setError(null);

                if(TextUtils.isEmpty(firstName.getText().toString().trim())) {
                    firstName_layout.setError("First name required");
                    firstName.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(lastName.getText().toString().trim())) {
                    lastName_layout.setError("Last name required");
                    lastName.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(email.getText().toString().trim())) {
                    email_layout.setError("Email required");
                    email.requestFocus();
                    return;
                }

                if (!email.getText().toString().matches(EMAIL_PATTERN)) {
                    email_layout.setError("Email is not valid");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(password.getText().toString())) {
                    password_layout.setError("Password required");
                    return;
                }

                if(TextUtils.isEmpty(confirmPassword.getText().toString())) {
                    confirmPassword_layout.setError("Password required");
                    confirmPassword.requestFocus();
                    return;
                }

                if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    password_layout.setError("Passwords do no match");
                    confirmPassword.requestFocus();
                    return;
                }

                final String name = firstName.getText().toString().trim() + " " + lastName.getText().toString().trim();

                if (!Util.isNetworkAvailable(SignupActivity.this)) {
                    Toast.makeText(SignupActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                    return;
                }

                loadingDialog.startLoadingDialog();
                registerUser(name, email.getText().toString().trim(), password.getText().toString().trim(),
                        loadingDialog);
            }
        });
    }

    private void registerUser(String name, String email,
                              String password, LoadingDialog loadingDialog) {

        final Intent i = new Intent(SignupActivity.this, LoginActivity.class);

        Call<String> todoCall = iMyService.registerUser(email, name, password);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(SignupActivity.this, response.body(), Toast.LENGTH_LONG).show();
                    startActivity(i);
                    finish();
                }
                else if(response.code() == 409) {
                    loadingDialog.dismissDialog();
                    Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(SignupActivity.this, "Something wrong happened", Toast.LENGTH_SHORT).show();
                Log.d("Response", "onFailure: "+t.getLocalizedMessage());
            }
        });
    }

    @Override
    public void onBackPressed(){
        Intent intent = new Intent(SignupActivity.this, LoginActivity.class);
        startActivity(intent);
        finish();
    }
}
