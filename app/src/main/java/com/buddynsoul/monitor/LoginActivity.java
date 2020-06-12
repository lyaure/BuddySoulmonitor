package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.android.material.textfield.TextInputLayout;
import com.google.gson.JsonElement;

public class LoginActivity extends AppCompatActivity {
    private TextView email;
    private TextView password;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

    IMyService iMyService;

//    @Override
//    protected void onStop() {
//        compositeDisposable.clear();
//        super.onStop();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Service
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        // Init view
        email = (TextView)findViewById(R.id.txtv_email_ID);
        password = (TextView)findViewById(R.id.txtv_password_ID);

        TextInputLayout email_layout = findViewById(R.id.email_layout_ID);
        TextInputLayout password_layout = findViewById(R.id.password_layout_ID);

        //Init Loading Dialog
        LoadingDialog loadingDialog = new LoadingDialog(LoginActivity.this);

        Button login = (Button)findViewById(R.id.loginBtn_ID);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                email_layout.setError(null);
                password_layout.setError(null);

                if(TextUtils.isEmpty(email.getText().toString().trim()))
                {
                    email_layout.setError("Email required");
                    email.requestFocus();
                    return;
                }

                if (!email.getText().toString().matches(EMAIL_PATTERN)) {
                    email_layout.setError("Email is not valid");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(password.getText().toString().trim()))
                {
                    password_layout.setError("Password required");
                    password.requestFocus();
                    return;
                }

                if (!Util.isNetworkAvailable(LoginActivity.this)) {
                    Toast.makeText(LoginActivity.this, "Please check your internet connection", Toast.LENGTH_LONG).show();
                    return;
                }

                loadingDialog.startLoadingDialog();
                loginUser(email.getText().toString().trim(), password.getText().toString().trim(), loadingDialog);
//                Intent i = new Intent(LoginActivity.this, PedometerActivity.class);
//                startActivity(i);
            }
        });


        SpannableString ss = new SpannableString("Not a Buddy&Soul member yet? Sign up here");
        ClickableSpan clickableSpan = new ClickableSpan() {
            @Override
            public void onClick(View textView) {
                Intent i = new Intent(LoginActivity.this, SignupActivity.class);
                startActivity(i);
            }
            @Override
            public void updateDrawState(TextPaint ds) {
                super.updateDrawState(ds);
                ds.setUnderlineText(false);
            }
        };
        ss.setSpan(clickableSpan, 37, 41, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        TextView textView = (TextView) findViewById(R.id.signup_link_ID);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
        textView.setHighlightColor(Color.TRANSPARENT);

//        hide = true;
//        password.setOnTouchListener(new View.OnTouchListener() {
//            @Override
//            public boolean onTouch(View v, MotionEvent event) {
//                final int DRAWABLE_LEFT = 0;
//                final int DRAWABLE_TOP = 1;
//                final int DRAWABLE_RIGHT = 2;
//                final int DRAWABLE_BOTTOM = 3;
//
//                if(event.getAction() == MotionEvent.ACTION_UP) {
//                    if(event.getX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
//                        hide = !hide;
//                        if(hide)
//                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
//                        else
//                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
//
//                    }
//                }
//                return false;
//            }
//        });

        TextView forgot = (TextView)findViewById(R.id.forgotLink_ID);
        forgot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Uri uriUrl = Uri.parse("https://www.buddynsoul.com/Account/ForgotPassword");
//                Intent launchBrowser = new Intent(Intent.ACTION_VIEW, uriUrl);
//                startActivity(launchBrowser);

                Intent i = new Intent(LoginActivity.this, ResetPasswordActivity.class);
                startActivity(i);
            }
        });

    }

    private void loginUser(final String email, String password, LoadingDialog loadingDialog) {
        final Intent i = new Intent(LoginActivity.this, MainActivity.class);

        Call<JsonElement> todoCall = iMyService.loginUser(email, password);
        todoCall.enqueue(new Callback<JsonElement>() {
            @Override
            public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
                if (response.code() == 200) {
                    String res = response.body().toString();
                    res = res.substring(1, res.length()-1);
                    if(res.equals("Please confirm your email")) {
                        loadingDialog.dismissDialog();
                        Toast.makeText(LoginActivity.this, "Please confirm your email", Toast.LENGTH_SHORT).show();
                    }
                    else {
                        String refreshToken = response.body().getAsJsonObject().get("refreshToken").getAsString();
                        boolean admin = response.body().getAsJsonObject().get("admin").getAsBoolean();
                        String name = response.body().getAsJsonObject().get("name").getAsString();
                        long registrationDate = response.body().getAsJsonObject().get("registration_date").getAsLong();

                        SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                        SharedPreferences.Editor editor = sp.edit();
                        editor.putString("email", email);
                        editor.putString("name", name);
                        editor.putLong("registrationDate", registrationDate);
                        editor.putString("refreshToken", refreshToken);
                        editor.putBoolean("admin", admin);
                        editor.putBoolean("logged", true);
                        editor.apply();

                        startActivity(i);
                        loadingDialog.dismissDialog();
                        finish();
                    }
                }
                else if(response.code() == 404){
                    loadingDialog.dismissDialog();
                    Toast.makeText(LoginActivity.this, "Your account doesn\'t exist", Toast.LENGTH_SHORT).show();
                }
                else if(response.code() == 401){
                    loadingDialog.dismissDialog();
                    Toast.makeText(LoginActivity.this, "Wrong password", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<JsonElement> call, Throwable t) {
                loadingDialog.dismissDialog();
                Toast.makeText(LoginActivity.this, "Something wrong happened", Toast.LENGTH_SHORT).show();
                Log.d("Response", "onFailure: "+t.getLocalizedMessage());
            }
        });



    }

    @Override
    protected void onStop(){
        super.onStop();
        finish();
    }
}