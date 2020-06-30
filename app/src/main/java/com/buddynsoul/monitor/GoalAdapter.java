package com.buddynsoul.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.TextView;

import com.buddynsoul.monitor.Utils.Util;

import java.util.ArrayList;

public class GoalAdapter extends ArrayAdapter<Goal> {
    private Context context;
    private ArrayList<Goal> goals;
    public TextView lastEdit;
    private int lastPos = -1;

    public GoalAdapter(Context context, ArrayList<Goal> list) {
        super(context, 0, list);
        this.context = context;
        goals = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItem = convertView;

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.goals_list_item, parent, false);

        Goal goal = goals.get(position);

        CheckBox checked = (CheckBox)listItem.findViewById(R.id.checkBox_goal_ID);
        checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                goal.setChecked(isChecked);
                goals.set(position, goal);
                notifyDataSetChanged();
                updateDB(goal);
            }
        });

        checked.setChecked(goal.isChecked());

        TextView text = (TextView)listItem.findViewById(R.id.goal_text_ID);
        text.setText(goal.getText());


        return listItem;
    }

    private void updateDB(Goal goal) {
        SQLiteDatabase db = context.openOrCreateDatabase("MyGoals", context.MODE_PRIVATE, null);

        String bool = goal.isChecked()? "true" : "false";

        ContentValues cv = new ContentValues();
        cv.put("checked", bool);

        db.update("MyGoals", cv, "id = ?", new String[]{String.valueOf(goal.getId())});
    }
}
