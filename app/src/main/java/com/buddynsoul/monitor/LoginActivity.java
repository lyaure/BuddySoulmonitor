package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.app.ProgressDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.LinkMovementMethod;
import android.text.method.PasswordTransformationMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;

import java.io.IOException;

public class LoginActivity extends AppCompatActivity {
    private TextView email;
    private TextView password;
    private Boolean hide;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    @Override
    protected void onStop() {
        compositeDisposable.clear();
        super.onStop();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        // Init Service
        Retrofit retrofitClient = RetrofitClient.getInstance();
        iMyService = retrofitClient.create(IMyService.class);

        // Init view
        email = (TextView)findViewById(R.id.txtv_email_ID);
        password = (TextView)findViewById(R.id.txtv_password_ID);

        Button login = (Button)findViewById(R.id.loginBtn_ID);
        login.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if(TextUtils.isEmpty(email.getText().toString().trim()))
                {
                    //Toast.makeText(this, "Email cannot be null or empty", Toast.LENGTH_SHORT).show();
                    email.setError("Email required");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(password.getText().toString().trim()))
                {
                    //Toast.makeText(this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                    email.setError("Password required");
                    email.requestFocus();
                    return;
                }

                loginUser(email.getText().toString().trim(), password.getText().toString().trim());
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

        hide = true;
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        hide = !hide;
                        if(hide)
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        else
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                    }
                }
                return false;
            }
        });

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

    private void loginUser(final String email, String password) {
        final Intent i = new Intent(LoginActivity.this, PedometerActivity.class);

        compositeDisposable.add(iMyService.loginUser(email, password)
        .subscribeOn(Schedulers.io())
        .observeOn(AndroidSchedulers.mainThread())
        .subscribe(new Consumer<String>() {
            @Override
            public void accept(String response) throws Exception {
                //Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                //Log.d("Response", response);
                Log.d("Response", response);
                if (!response.equals("\"Wrong password\"") && !response.equals("\"Your account doesn\'t exist\"")) {
                    SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putString("email", email);
                    editor.putString("refreshToken", response);

                    response = response.substring(1, response.length()-1);
                    Log.d("Response", "After substring:" + response);

                    editor.putBoolean("logged", true);
                    editor.commit();

                    startActivity(i);
                }
                else {
                    Toast.makeText(LoginActivity.this, ""+response, Toast.LENGTH_SHORT).show();
                }

            }
        }));

//        Call<ResponseBody<ResponseData>> call = (Call<ResponseBody>) RetrofitClient
//                .getInstance()
//                .getImyService()
//                .loginUser(email, password);
//
//        call.enqueue(new Callback<ResponseBody>() {
//            @Override
//            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
//                try {
//                    if(response.)
//                    String s = response.body().string();
//                    Toast.makeText(LoginActivity.this, s, Toast.LENGTH_LONG).show();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
//            }
//
//            @Override
//            public void onFailure(Call<ResponseBody> call, Throwable t) {
//                Toast.makeText(LoginActivity.this, t.getMessage(), Toast.LENGTH_LONG).show();
//            }
//        });

    }
}
