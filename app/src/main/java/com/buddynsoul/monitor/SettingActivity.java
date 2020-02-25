package com.buddynsoul.monitor;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class SettingActivity extends AppCompatActivity {
    private TextView goal;
    private RadioButton oldButton;
    private int oldGoal;
    private RadioGroup temperature;
    private Boolean boolTemp;
    private int val;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        Intent intent = getIntent();
        val = intent.getIntExtra("activity", 0);


        SharedPreferences sp = getSharedPreferences("pedometer", MODE_PRIVATE);
        oldGoal = sp.getInt("goal", 10000);

        goal = findViewById(R.id.goalTxtv_ID);
        goal.setText(Integer.toString(sp.getInt("goal", 10000)));

        final RadioGroup unitTemperature = findViewById(R.id.radioGroupTemp_ID);
        RadioButton c = findViewById(R.id.radioButtonC_ID);
        RadioButton f = findViewById(R.id.radioButtonF_ID);

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
                    SharedPreferences sp = getSharedPreferences("pedometer", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
                    editor.putInt("goal", Integer.parseInt(goal.getText().toString()));
                    editor.commit();
                }

                if(unitTemperature.getCheckedRadioButtonId() != oldButton.getId()){
                    String temp = boolTemp ? "false" : "true";

                    SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
                    SharedPreferences.Editor editor = sp.edit();
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
    }
}
