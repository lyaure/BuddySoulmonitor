package com.buddynsoul.monitor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;

public class UserAdapter extends ArrayAdapter<User> {
    private Context context;
    private ArrayList<User> users;

    public UserAdapter(Context context, ArrayList<User> list) {
        super(context, 0, list);
        this.context = context;
        users = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItem = convertView;

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.users_list_item, parent, false);

        final User user = users.get(position);

        TextView userName = (TextView)listItem.findViewById(R.id.user_name_ID);
        userName.setText(user.getName());

        ImageView icon = (ImageView)listItem.findViewById(R.id.user_list_icon_ID);
        icon.setImageResource(R.drawable.users_list_icon);

//        TextView userEmail = (TextView)listItem.findViewById(R.id.userEmail_ID);
//        userEmail.setText(String.valueOf(user.getEmail()));
//
//        TextView userRegistration = (TextView)listItem.findViewById(R.id.userRegistration_ID);
//        userRegistration.setText(user.getRegistrationDate());

        return listItem;
    }
}
