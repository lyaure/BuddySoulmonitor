package com.buddynsoul.monitor;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.NumberPicker;
import android.widget.TextView;

import androidx.fragment.app.DialogFragment;

import com.buddynsoul.monitor.Utils.Util;

public class NumberPickerDialog extends DialogFragment {
    private NumberPicker.OnValueChangeListener valueChangeListener;
    private int value = 0;
    private Database db;
    private String from;
    private TextView textView;
    private String message;

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        Bundle bundle = this.getArguments();
        if(bundle != null)
            from = bundle.getString("from", "null");

        final NumberPicker numberPicker = new NumberPicker(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);

        int  goal = sp.getInt("goal", 10000);

        db = Database.getInstance(getActivity());
        int todayGoal = db.getStepGoal(Util.getToday());

        numberPicker.setMinValue(0);

        if(from.equals("pedometer")){
            textView = (TextView)getActivity().findViewById(R.id.today_goal_ID);
            message = "Choose your today's step goal";
            numberPicker.setMaxValue(goal);
            if(todayGoal != -1)
                numberPicker.setValue(todayGoal);
            else
                numberPicker.setValue(goal);
        }
        else if(from.equals("sleepHours")) {
            textView = (TextView)getActivity().findViewById(R.id.sleep_hours_goal_ID);
            message = "Choose your tonight's duration sleep goal";
            numberPicker.setMaxValue(12);
        }
        else {
            textView = (TextView)getActivity().findViewById(R.id.sleep_min_goal_ID);
            message = "Choose your tonight's duration sleep goal";
            numberPicker.setMaxValue(60);
        }

//        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
//            @Override
//            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
//                value = newVal;
//            }
//        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(message);

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                numberPicker.clearFocus();
                value = numberPicker.getValue();
                textView.setText(String.valueOf(value));

                if(from.equals("pedometer"))
                   db.insertStepGoal(value);
                else if(from.equals("sleepHours")) {
                    TextView txtv = (TextView)getActivity().findViewById(R.id.sleep_min_goal_ID);
                    db.insertSleepGoal(value * 3600 + Integer.parseInt(txtv.getText().toString()) * 60);
                }
                else {
                    TextView txtv = (TextView)getActivity().findViewById(R.id.sleep_hours_goal_ID);
                    db.insertSleepGoal(Integer.parseInt(txtv.getText().toString()) * 3600 + value * 60);
                }
            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener(){
        return valueChangeListener;
    }

}

