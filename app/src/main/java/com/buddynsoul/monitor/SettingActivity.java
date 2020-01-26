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
    private String oldGoal;
    private RadioGroup temperature;
    private Boolean boolTemp;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);

        SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
        oldGoal = sp.getString("stepGoal", "10000");

        goal = findViewById(R.id.goalTxtv_ID);
        goal.setText(sp.getString("stepGoal", "10000"));

        final RadioGroup temperature = findViewById(R.id.radioGroupTemp_ID);
        RadioButton c = findViewById(R.id.radioButtonC_ID);
        RadioButton f = findViewById(R.id.radioButtonF_ID);
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
                SharedPreferences sp = getSharedPreferences("Settings", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();

                if (!oldGoal.equals(goal.getText().toString()) && !goal.getText().toString().equals("")){
                    editor.putString("stepGoal", goal.getText().toString());
                    editor.commit();
                }

                if(temperature.getCheckedRadioButtonId() != oldButton.getId()){
                    String temp = boolTemp ? "true" : "false";

                    editor.putString("metricValue", temp);
                    editor.commit();
                }

                Intent i = new Intent(SettingActivity.this, PedometerActivity.class);
                startActivity(i);
            }
        });
    }
}
