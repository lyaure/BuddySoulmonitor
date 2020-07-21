package com.buddynsoul.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ScreenReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {

        SharedPreferences sp = context.getSharedPreferences("tempData", MODE_PRIVATE);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            long currTime = System.currentTimeMillis();
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen Off");

            long tmpScreenOff = sp.getLong("tmpScreenOff", System.currentTimeMillis());

            ArrayList<long[]>  screenInterval = retrieveArrayList(sp);

            long[] last = screenInterval.get(screenInterval.size() - 1);
            long durationScreenOff = System.currentTimeMillis() - tmpScreenOff;
            durationScreenOff /= 1000; // convert time to sec

            SharedPreferences.Editor editor = sp.edit();
            long screenOff = sp.getLong("screenOff", 0) + durationScreenOff;
            editor.putLong("screenOff", screenOff);
            editor.apply();

            String str = "Duration screen off: " + screenOff + " sec";
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);

            last[1] = currTime;

            addToSharedPreference(sp, screenInterval);
        }

        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen Off");

            SharedPreferences.Editor editor = sp.edit();
            long screenOff = System.currentTimeMillis();
            editor.putLong("tmpScreenOff", screenOff);
            editor.apply();

            ArrayList<long[]>  screenInterval = retrieveArrayList(sp);
            long[] last = screenInterval.get(screenInterval.size() - 1);
            if(last[1] != 0) {
                screenInterval.add(new long[]{System.currentTimeMillis(), 0});
                addToSharedPreference(sp, screenInterval);
            }
        }
    }

    private void addToSharedPreference(SharedPreferences sp, ArrayList<long[]> screenInterval) {
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json_data = gson.toJson(screenInterval);
        editor.putString("json_data", json_data);
        editor.apply();
    }

    private ArrayList<long[]> retrieveArrayList(SharedPreferences sp) {
        Gson gson = new Gson();
        String json_data = sp.getString("json_data", "");

        if (json_data.equals("")) {
            return new ArrayList<long[]>();
        }

        Type type = new TypeToken<ArrayList<long[]>>(){}.getType();
        ArrayList<long[]>  screenInterval = gson.fromJson(json_data, type);

        return screenInterval;
    }
}
