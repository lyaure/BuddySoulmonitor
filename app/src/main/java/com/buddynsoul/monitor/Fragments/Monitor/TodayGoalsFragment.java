package com.buddynsoul.monitor.Fragments.Monitor;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;

import androidx.fragment.app.Fragment;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.buddynsoul.monitor.Adapters.GoalAdapter;
import com.buddynsoul.monitor.Objects.Goal;
import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Utils.Util;

import java.util.ArrayList;


public class TodayGoalsFragment extends Fragment {
    private View v;
    private ListView goalsList;
    private ArrayList<Goal> goals;
    private GoalAdapter adapter;
    private TextView add;
    private SQLiteDatabase db;
    private EditText newGoal;

    public TodayGoalsFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        v =  inflater.inflate(R.layout.fragment_today_goals, container, false);

        // init viewa
        goalsList = (ListView)v.findViewById(R.id.goalsList_ID);
        add = (TextView) v.findViewById(R.id.add_txtv_ID);
        newGoal = (EditText)v.findViewById(R.id.new_goal_ID);

        goals = new ArrayList<>();

        adapter = new GoalAdapter(getContext(), goals);
        goalsList.setAdapter(adapter);

        // fill and notify
        fillList();
        adapter.notifyDataSetChanged();

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newGoalStr = newGoal.getText().toString();
                if(newGoalStr.trim().length() == 0) {
                    Toast.makeText(getContext(), "Goal can't be empty!", Toast.LENGTH_SHORT).show();
                }
                else {
                    // add and update list
                    Goal goal = new Goal(newGoal.getText().toString(), false);
                    goals.add(goal);
                    adapter.notifyDataSetChanged();

                    // update db
                    addNewGoalToDB(goal);

                    // clear textview
                    newGoal.setText("");
                    newGoal.clearFocus();
                }
            }
        });

        return v;
    }

    // insert new goal to SQLite db
    private void addNewGoalToDB(Goal goal) {
        db = getActivity().openOrCreateDatabase("MyGoals", getActivity().MODE_PRIVATE, null);

        String bool = goal.isChecked()? "true" : "false";

        ContentValues cv = new ContentValues();
        cv.put("id", goal.getId());
        cv.put("date", Util.getToday());
        cv.put("goal", goal.getText());
        cv.put("checked", bool);

        db.insert("MyGoals", null, cv);
    }

    // fill list after reopening app from db's data
    private void fillList(){
        db = getActivity().openOrCreateDatabase("MyGoals", getActivity().MODE_PRIVATE, null);
        String query = "CREATE TABLE IF NOT EXISTS MyGoals (id INTEGER, date INTEGER, goal TEXT, checked TEXT)";
        db.execSQL(query);

        Cursor cursor = db.rawQuery("SELECT * FROM MyGoals WHERE date = " + Util.getToday(), null );

        if(cursor.moveToFirst()){
            do{
                String text = cursor.getString(cursor.getColumnIndex("goal"));
                String checked = cursor.getString(cursor.getColumnIndex("checked"));
                goals.add(new Goal(text, checked.equals("true")));
            }while (cursor.moveToNext());
        }
        cursor.close();
        adapter.notifyDataSetChanged();
    }
}
