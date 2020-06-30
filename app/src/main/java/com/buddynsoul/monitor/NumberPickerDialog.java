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

    @Override
    public Dialog onCreateDialog(Bundle savedInstance){
        final NumberPicker numberPicker = new NumberPicker(getActivity());

        SharedPreferences sp = getActivity().getSharedPreferences("pedometer", getActivity().MODE_PRIVATE);

        int  goal = sp.getInt("goal", 10000);

        db = Database.getInstance(getActivity());
        int todayGoal = db.getStepGoal(Util.getToday());

        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(goal);
        if(todayGoal != -1)
            numberPicker.setValue(todayGoal);
        else
            numberPicker.setValue(goal);

        numberPicker.setOnValueChangedListener(new NumberPicker.OnValueChangeListener() {
            @Override
            public void onValueChange(NumberPicker picker, int oldVal, int newVal) {
                value = newVal;
//                db.insertStepGoal(newVal);
            }
        });

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("Choose youre today's step goal");

        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                TextView steps = (TextView)getActivity().findViewById(R.id.today_goal_ID);
                steps.setText(String.valueOf(value));
                db.insertStepGoal(value);
            }
        });

        builder.setView(numberPicker);
        return builder.create();
    }

    public NumberPicker.OnValueChangeListener getValueChangeListener(){
        return valueChangeListener;
    }

}

