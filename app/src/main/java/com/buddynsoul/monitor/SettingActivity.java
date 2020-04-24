package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

public class SettingActivity extends AppCompatActivity {
    private TextView goal;
    private RadioButton oldButton, c, f;
    private int oldGoal;
    private RadioGroup temperature;
    private Boolean boolTemp;
    private int val;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        val = intent.getIntExtra("activity", 0);


        sp = getSharedPreferences("pedometer", MODE_PRIVATE);
        oldGoal = sp.getInt("goal", 10000);

        goal = findViewById(R.id.goalTxtv_ID);
        goal.setText(Integer.toString(oldGoal));

        goal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(goal.getText().toString().equals("-")) {
                    goal.setText("");
                    Toast.makeText(getApplicationContext(), "Goal value need to be above 0", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!goal.getText().toString().equals("") && Integer.parseInt(goal.getText().toString()) == 0) {
                    goal.setText("");
                    Toast.makeText(getApplicationContext(), "Goal value need to be above 0", Toast.LENGTH_LONG).show();
                }
            }
        });

        final RadioGroup unitTemperature = findViewById(R.id.radioGroupTemp_ID);
        c = (RadioButton)findViewById(R.id.radioButtonC_ID);
        f = (RadioButton) findViewById(R.id.radioButtonF_ID);

        sp = getSharedPreferences("Settings", MODE_PRIVATE);
        final String oldTemperature = sp.getString("metricValue", "true");
        if(oldTemperature.equals("true")) {
            c.setChecked(true);
            oldButton = c;
            boolTemp = true;
        }
        else {
            f.setChecked(true);
            oldButton = f;
            boolTemp = false;
        }

        Button apply = findViewById(R.id.applyBtn_ID);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldGoalStr = "" + oldGoal;
                if (!oldGoalStr.equals(goal.getText().toString()) && !goal.getText().toString().equals("")){
                    sp = getSharedPreferences("pedometer", MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putInt("goal", Integer.parseInt(goal.getText().toString()));
                    editor.commit();
                }

                if(unitTemperature.getCheckedRadioButtonId() != oldButton.getId()){
                    String temp = boolTemp ? "false" : "true";

                    sp = getSharedPreferences("Settings", MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putString("metricValue", temp);
                    editor.commit();
                }

                Intent i;

                if(val == 2)
                    i = new Intent(SettingActivity.this, WeatherActivity.class);
                else
                    i = new Intent(SettingActivity.this, PedometerActivity.class);

                startActivity(i);
            }
        });

        Button reset = (Button) findViewById(R.id.resetBtn_ID);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sp = getSharedPreferences("pedometer", MODE_PRIVATE);
                editor = sp.edit();
                editor.putInt("goal", 10000);
                editor.commit();

                goal.setText("10000");

                sp = getSharedPreferences("Settings", MODE_PRIVATE);
                editor = sp.edit();
                editor.putString("metricValue", "true");
                editor.commit();

                c.setChecked(true);
                oldButton = c;
                boolTemp = true;
            }
        });
    }
}
