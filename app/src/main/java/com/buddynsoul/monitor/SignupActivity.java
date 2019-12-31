package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

public class SignupActivity extends AppCompatActivity {
    private TextView password;
    private TextView confirm;
    private boolean hideP;
    private boolean hideC;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_signup);

        hideP = true;
        hideC = true;

        password = (TextView)findViewById(R.id.txtv_password2_ID);
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

        confirm = (TextView)findViewById(R.id.txtv_confirm_ID);
        confirm.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                final int DRAWABLE_LEFT = 0;
                final int DRAWABLE_TOP = 1;
                final int DRAWABLE_RIGHT = 2;
                final int DRAWABLE_BOTTOM = 3;

                if(event.getAction() == MotionEvent.ACTION_UP) {
                    if(event.getX() >= (confirm.getRight() - confirm.getCompoundDrawables()[DRAWABLE_RIGHT].getBounds().width())) {
                        hideC = !hideC;
                        if(hideC)
                            confirm.setTransformationMethod(PasswordTransformationMethod.getInstance());
                        else
                            confirm.setTransformationMethod(HideReturnsTransformationMethod.getInstance());

                    }
                }
                return false;
            }
        });
    }
}
