package com.buddynsoul.monitor.Dialogs;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import android.widget.DatePicker;
import android.widget.TextView;

import com.buddynsoul.monitor.R;

import java.util.Calendar;


public class DatePickerFragment extends DialogFragment {
    private String from;

    public DatePickerFragment() {
        // Required empty public constructor
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        final Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        Bundle bundle = this.getArguments();
        if(bundle != null)
            from = bundle.getString("from", "null");

        return new DatePickerDialog(getActivity(), new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                if(from.equals("fromDate")){
                    TextView fromDate = (TextView)getActivity().findViewById(R.id.fromDate_txtv_ID);
                    fromDate.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                }
                else{
                    TextView toDate = (TextView)getActivity().findViewById(R.id.toDate_txtv_ID);
                    toDate.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                }

            }
        }, year, month, day);
    }
}
