package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;
import retrofit2.Retrofit;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;

public class ResetPasswordActivity extends AppCompatActivity {
    private TextView email;
    private TextView password;

    CompositeDisposable compositeDisposable = new CompositeDisposable();
    IMyService iMyService;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

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

                resetUserPassword(email.getText().toString().trim());
//                Intent i = new Intent(LoginActivity.this, PedometerActivity.class);
//                startActivity(i);
            }
        });
    }

    private void resetUserPassword(final String email) {
        final Intent i = new Intent(ResetPasswordActivity.this, LoginActivity.class);

        compositeDisposable.add(iMyService.resetUserPassword(email)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<String>() {
                    @Override
                    public void accept(String response) throws Exception {
                        String msg = "If a matching account was found an email was sent to "
                                + email + " to allow you to reset your password.";
                        Toast.makeText(ResetPasswordActivity.this, msg, Toast.LENGTH_LONG).show();
                        startActivity(i);
//                        if (response.equals("\"Login success\"")) {
//                            SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
//                            SharedPreferences.Editor editor = sp.edit();
//                            editor.putBoolean("logged", true);
//                            editor.commit();
//
//                            startActivity(i);
//                        }
                    }
                }));
    }
}
