package com.buddynsoul.monitor.Fragments.Monitor;

import androidx.fragment.app.Fragment;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.MonitorService;
import com.buddynsoul.monitor.Dialogs.TimePickerFragment;

import static android.content.Context.MODE_PRIVATE;

public class SettingsFragment extends Fragment {
    private TextView goal, fromTime, toTime;
    private RadioButton oldButton, c, f;
    private int oldGoal;
    private Boolean boolTemp;
    private SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private int old_from_hour, old_from_min, old_to_hour, old_to_min;


    public SettingsFragment(){

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_setting, container, false);

        sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);

        oldGoal = sp.getInt("goal", 10000);

        goal = v.findViewById(R.id.goalTxtv_ID);
        goal.setText(Integer.toString(oldGoal));

        goal.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(goal.getText().toString().equals("-")) {
                    goal.setText("");
                    Toast.makeText(getActivity(), "Goal value need to be above 0", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void afterTextChanged(Editable s) {
                if(!goal.getText().toString().equals("") && Integer.parseInt(goal.getText().toString()) == 0) {
                    goal.setText("");
                    Toast.makeText(getActivity(), "Goal value need to be above 0", Toast.LENGTH_LONG).show();
                }
            }
        });

        final RadioGroup unitTemperature = v.findViewById(R.id.radioGroupTemp_ID);
        c = (RadioButton)v.findViewById(R.id.radioButtonC_ID);
        f = (RadioButton) v.findViewById(R.id.radioButtonF_ID);

        getActivity();
        sp = getActivity().getSharedPreferences("Settings", MODE_PRIVATE);
        final boolean oldTemperature = sp.getBoolean("metricValue", true);
        if(oldTemperature) {
            c.setChecked(true);
            oldButton = c;
            boolTemp = true;
        }
        else {
            f.setChecked(true);
            oldButton = f;
            boolTemp = false;
        }

        fromTime = (TextView) v.findViewById(R.id.fromTime_ID);

        final SharedPreferences preferences = getActivity().getSharedPreferences("prefTime", getActivity().MODE_PRIVATE);

        old_from_hour = preferences.getInt("fromHour", 20);
        old_from_min = preferences.getInt("fromMinute", 0);

        fromTime.setText(String.format("%s:%s %s", String.format("%02d", preferences.getInt("fromHour", 20)), String.format("%02d", preferences.getInt("fromMinute", 0)), preferences.getString("am_pm_from", "pm")));
        fromTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment dialogFragment = new TimePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "fromTime");
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getActivity().getFragmentManager(), "time");
            }
        });

        toTime = (TextView) v.findViewById(R.id.toTime_ID);

        old_to_hour = preferences.getInt("toHour", 8);
        old_to_min = preferences.getInt("toMinute", 0);

        toTime.setText(String.format("%s:%s %s", String.format("%02d", preferences.getInt("toHour", 8)), String.format("%02d", preferences.getInt("toMinute", 0)), preferences.getString("am_pm_to", "am")));
        toTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerFragment dialogFragment = new TimePickerFragment();
                Bundle bundle = new Bundle();
                bundle.putString("from", "toTime");
                dialogFragment.setArguments(bundle);
                dialogFragment.show(getActivity().getFragmentManager(), "time");
            }
        });

        Button apply = v.findViewById(R.id.applyBtn_ID);
        apply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String oldGoalStr = "" + oldGoal;
                if (!oldGoalStr.equals(goal.getText().toString()) && !goal.getText().toString().equals("")){
                    getActivity();
                    sp = getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);
                    editor = sp.edit();
                    editor.putInt("goal", Integer.parseInt(goal.getText().toString()));
                    editor.apply();
                }

                if(unitTemperature.getCheckedRadioButtonId() != oldButton.getId()){
                    boolean temp = !boolTemp;

                    getActivity();
                    sp = getActivity().getSharedPreferences("Settings", MODE_PRIVATE);

                    editor = sp.edit();
                    editor.putBoolean("metricValue", temp);
                    editor.commit();
                }

                Toast.makeText(getContext(), "Changes have been successfully completed", Toast.LENGTH_SHORT).show();

                if ((old_from_hour != preferences.getInt("fromHour", 20))
                    || (old_from_min != preferences.getInt("fromMinute", 0))
                    || (old_to_hour != preferences.getInt("toHour", 8))
                    || (old_to_min != preferences.getInt("toMinute", 0))) {

                    Intent myService = new Intent(getActivity(), MonitorService.class);
                    getActivity().stopService(myService);
                    getActivity().startService(myService);
                }

                Fragment fragment = new ProfileFragment();

                getActivity().getSupportFragmentManager().beginTransaction()
                        .replace(R.id.container_ID, fragment, "tag")
                        .addToBackStack(null)
                        .commit();
            }
        });

        Button reset = (Button) v.findViewById(R.id.resetBtn_ID);
        reset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                getActivity();
                sp = getActivity().getSharedPreferences("pedometer", MODE_PRIVATE);
                editor = sp.edit();
                editor.putInt("goal", 10000);
                editor.apply();

                goal.setText("10000");

                getActivity();
                sp = getActivity().getSharedPreferences("Settings", MODE_PRIVATE);
                editor = sp.edit();
                editor.putBoolean("metricValue", true);
                editor.commit();

                c.setChecked(true);
                oldButton = c;
                boolTemp = true;

                Toast.makeText(getContext(), "Settings have been successfully reset", Toast.LENGTH_SHORT).show();
            }
        });
        return v;
    }
    
}
