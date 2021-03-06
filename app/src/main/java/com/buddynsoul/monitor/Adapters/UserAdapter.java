package com.buddynsoul.monitor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.buddynsoul.monitor.R;
import com.buddynsoul.monitor.Objects.User;

import java.util.ArrayList;

// adapter for users list in admin activity with custom list item
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

        return listItem;
    }
}
