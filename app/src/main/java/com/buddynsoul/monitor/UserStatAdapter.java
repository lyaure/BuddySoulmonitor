package com.buddynsoul.monitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class UserStatAdapter extends ArrayAdapter<UserStat> {
    private Context context;
    private ArrayList<UserStat> userStatList;

    public UserStatAdapter(Context context, ArrayList<UserStat> list) {
        super(context, 0, list);
        this.context = context;
        userStatList = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItem = convertView;

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.user_stat_list_item, parent, false);

        final UserStat userStat = userStatList.get(position);

        TextView userStatDate = (TextView)listItem.findViewById(R.id.userStatDate_ID);
        userStatDate.setText(String.format("%s%s", "Date: ", String.valueOf(userStat.getDate())));


        TextView userStatSteps = (TextView)listItem.findViewById(R.id.userStatSteps_ID);
        userStatSteps.setText(String.format("%s%s", "Steps: ", String.valueOf(userStat.getSteps())));

        TextView userStatAsleep = (TextView)listItem.findViewById(R.id.userStatAsleep_ID);
        userStatAsleep.setText(String.format("%s%s", "Asleep: ", String.valueOf(userStat.getAsleepTime())));

        TextView userStatWokeUp = (TextView)listItem.findViewById(R.id.userStatWokeUp_ID);
        userStatWokeUp.setText(String.format("%s%s", "Woke up: ", String.valueOf(userStat.getWokeUpTime())));

        TextView userStatDeepSleep = (TextView)listItem.findViewById(R.id.userStatDeepSleep_ID);
        userStatDeepSleep.setText(String.format("%s%s", "Deep sleep: ", String.valueOf(userStat.getDeepSleep())));

        TextView userStatMorningLocation = (TextView)listItem.findViewById(R.id.userStatMorningLocation_ID);
        userStatMorningLocation.setText(String.format("%s%s", "Morning location: ", String.valueOf(userStat.getMorning_location())));

        TextView userStatNightLocation = (TextView)listItem.findViewById(R.id.userStatNightLocation_ID);
        userStatNightLocation.setText(String.format("%s%s", "Night location: ", String.valueOf(userStat.getNight_location())));

        return listItem;
    }
}
