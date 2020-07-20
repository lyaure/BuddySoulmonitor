package com.buddynsoul.monitor.Adapters;

import android.content.ContentValues;
import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.buddynsoul.monitor.Objects.Goal;
import com.buddynsoul.monitor.R;

import java.util.ArrayList;

public class GoalAdapter extends ArrayAdapter<Goal> {
    private Context context;
    private ArrayList<Goal> goals;
    public TextView text;

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

        text = (TextView)listItem.findViewById(R.id.goal_text_ID);

        CheckBox checked = (CheckBox)listItem.findViewById(R.id.checkBox_goal_ID);
        checked.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                goal.setChecked(isChecked);
                scratchText(isChecked);
                goals.set(position, goal);
                notifyDataSetChanged();
                updateDB(goal);
            }
        });

        checked.setChecked(goal.isChecked());

        text.setText(goal.getText());
        scratchText(goal.isChecked());

        Button remove = (Button)listItem.findViewById(R.id.goal_remove_btn_ID);
        remove.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                goals.remove(goal);
                notifyDataSetChanged();
                deleteFromDB(goal);
            }
        });

        return listItem;
    }

    private void scratchText(boolean isChecked){
        if(isChecked)
            text.setPaintFlags(Paint.STRIKE_THRU_TEXT_FLAG);
        else
            text.setPaintFlags(text.getPaintFlags() & (~Paint.STRIKE_THRU_TEXT_FLAG));
    }

    private void updateDB(Goal goal) {
        SQLiteDatabase db = context.openOrCreateDatabase("MyGoals", context.MODE_PRIVATE, null);

        String bool = goal.isChecked()? "true" : "false";

        ContentValues cv = new ContentValues();
        cv.put("checked", bool);

        db.update("MyGoals", cv, "id = ?", new String[]{String.valueOf(goal.getId())});
    }

    private void deleteFromDB(Goal goal){
        SQLiteDatabase db = context.openOrCreateDatabase("MyGoals", context.MODE_PRIVATE, null);
        db.delete("MyGoals", "id = ?", new String[]{String.valueOf(goal.getId())});
    }
}
