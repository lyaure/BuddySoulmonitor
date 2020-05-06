package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
//import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
//import io.reactivex.functions.Consumer;
//import io.reactivex.schedulers.Schedulers;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

import android.content.Intent;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SignupActivity extends AppCompatActivity {
    private TextView firstName;
    private TextView lastName;
    private TextView email;
    private TextView password;
    private TextView confirmPassword;
    private boolean hideP;
    private boolean hideC;

    private static final String EMAIL_PATTERN =
            "^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@"
                    + "[A-Za-z0-9-]+(\\.[A-Za-z0-9]+)*(\\.[A-Za-z]{2,})$";

//    private static final String PASSWORD_PATTERN = "(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])" +
//            "(?=.*[@#$%^&+=])(?=\\S+$).{8,}";

//    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    private Button singnup;

//    @Override
//    protected void onStop() {
//        compositeDisposable.clear();
//        super.onStop();
//    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        // Init Service
//        Retrofit retrofitClient = RetrofitClient.getInstance();
//        iMyService = retrofitClient.create(IMyService.class);
        iMyService = RetrofitClient.getClient().create(IMyService.class);

        //Button singnup = (Button)findViewById(R.id.signupBtn_ID);
        singnup = (Button)findViewById(R.id.signupBtn_ID);
        singnup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                firstName = (TextView)findViewById(R.id.txtv_firstName_ID);
                lastName = (TextView)findViewById(R.id.txtv_lastName_ID);
                email = (TextView)findViewById(R.id.txtv_email_ID);
                password = (TextView)findViewById(R.id.txtv_password_ID);
                confirmPassword = (TextView)findViewById(R.id.txtv_confirmPassword_ID);

                if(TextUtils.isEmpty(firstName.getText().toString().trim())) {
                    //Toast.makeText(SignupActivity.this, "First name cannot be null or empty", Toast.LENGTH_SHORT).show();
                    email.setError("First name required");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(lastName.getText().toString().trim())) {
                    //Toast.makeText(SignupActivity.this, "Last name cannot be null or empty", Toast.LENGTH_SHORT).show();
                    email.setError("Last name required");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(email.getText().toString().trim())) {
                    //Toast.makeText(SignupActivity.this, "Email cannot be null or empty", Toast.LENGTH_SHORT).show();
                    email.setError("Email required");
                    email.requestFocus();
                    return;
                }

                if (!email.getText().toString().matches(EMAIL_PATTERN)) {
                    email.setError("Email is not valid");
                    email.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(password.getText().toString())) {
                    //Toast.makeText(SignupActivity.this, "Password cannot be null or empty", Toast.LENGTH_SHORT).show();
                    password.setError("Password required");
                    password.requestFocus();
                    return;
                }

                if(TextUtils.isEmpty(confirmPassword.getText().toString())) {
                    //Toast.makeText(SignupActivity.this, "Confirmed password cannot be null or empty", Toast.LENGTH_SHORT).show();
                    confirmPassword.setError("Password required");
                    confirmPassword.requestFocus();
                    return;
                }

                if(!password.getText().toString().equals(confirmPassword.getText().toString())) {
                    //Toast.makeText(SignupActivity.this, "Passwords do no match", Toast.LENGTH_SHORT).show();
                    password.setError("Passwords do no match");
                    password.requestFocus();
                    return;
                }

                final String name = firstName.getText().toString().trim() + " " + lastName.getText().toString().trim();

                registerUser(name, email.getText().toString().trim(), password.getText().toString().trim());
            }
        });

        hideP = true;
        hideC = true;

        password = (TextView)findViewById(R.id.txtv_password_ID);
        password.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (password.getRight() - password.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        hideP = !hideP;
                        if(hideP)
                            password.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        else
                            password.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                    }
                }
                return false;
            }
        });

        confirmPassword = (TextView)findViewById(R.id.txtv_confirmPassword_ID);
        confirmPassword.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (confirmPassword.getRight() - confirmPassword.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        hideC = !hideC;
                        if(hideC)
                            confirmPassword.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        else
                            confirmPassword.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                    }
                }
                return false;
            }
        });
    }

    private void registerUser(String name, String email,
                              String password) {

        final Intent i = new Intent(SignupActivity.this, LoginActivity.class);
//
//        compositeDisposable.add(iMyService.registerUser(email, name, password)
//                .subscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Consumer<String>() {
//                    @Override
//                    public void accept(String response) throws Exception {
//                        Toast.makeText(SignupActivity.this, ""+response, Toast.LENGTH_LONG).show();
//                        //if (response.equals("\"Registration success\""))
//                            startActivity(i);
//                    }
//                }));

        Call<String> todoCall = iMyService.registerUser(email, name, password);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                if(response.code() == 200) {
                    startActivity(i);
                    Toast.makeText(SignupActivity.this, response.body(), Toast.LENGTH_SHORT).show();
                }
                else if(response.code() == 409) {
                    Toast.makeText(SignupActivity.this, "Email already exists", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });
    }

}
