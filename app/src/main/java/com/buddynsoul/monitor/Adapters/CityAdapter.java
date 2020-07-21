package com.buddynsoul.monitor.Adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.buddynsoul.monitor.Objects.City;
import com.buddynsoul.monitor.R;

import java.util.ArrayList;

// adapter for search city list with custom list item
public class CityAdapter extends ArrayAdapter<City> {
    private Context context;
    private ArrayList<City> users;

    public CityAdapter(Context context, ArrayList<City> list) {
        super(context, 0, list);
        this.context = context;
        users = list;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent){
        View listItem = convertView;

        if(listItem == null)
            listItem = LayoutInflater.from(context).inflate(R.layout.cities_list_item, parent, false);

        final City city = users.get(position);

        TextView cityName = (TextView)listItem.findViewById(R.id.cityName_ID);
        cityName.setText(city.getCityName());

        TextView countryName = (TextView)listItem.findViewById(R.id.countryName_ID);
        countryName.setText(String.valueOf(city.getCountryName()));

        return listItem;
    }
}
