package com.buddynsoul.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.ArrayList;

import static android.content.Context.MODE_PRIVATE;

public class ScreenReceiver extends BroadcastReceiver {

    public static final String ACTION_CHARGING = "android.os.action.CHARGING";
    public static final String ACTION_DISCHARGING = "android.os.action.DISCHARGING";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //if (BuildConfig.DEBUG) Log.d("debug","shutting down");

        SharedPreferences sp = context.getSharedPreferences("tempData", MODE_PRIVATE);

        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)){
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen Off");

            long tmpScreenOff = sp.getLong("tmpScreenOff", System.currentTimeMillis());

            ArrayList<long[]>  screenInterval = retrieveArrayList(sp);
//            long endCounterScreen = System.currentTimeMillis();
//            long[] interval = {tmpScreenOff, endCounterScreen};
//            screenInterval.add(interval);

            ///////////////////// lyaure's changes ///////////////////
            long[] last = screenInterval.get(screenInterval.size() - 1);



            long durationScreenOff = System.currentTimeMillis() - tmpScreenOff;
            durationScreenOff /= 1000; // convert time to sec
//            String str = "Duration screen on: " + durationScreenOn + " sec";

            SharedPreferences.Editor editor = sp.edit();
//            long screenOff = sp.getLong("screenOff", System.currentTimeMillis()) + durationScreenOff;
            long screenOff = sp.getLong("screenOff", 0) + durationScreenOff;
            editor.putLong("screenOff", screenOff);
            editor.commit();
            String str = "Duration screen off: " + screenOff + " sec";

            if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);

            last[1] = last[0] + durationScreenOff;

            addToSharedPreference(sp, screenInterval);
        }

        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen Off");

            SharedPreferences.Editor editor = sp.edit();
            long screenOff = System.currentTimeMillis();
            editor.putLong("tmpScreenOff", screenOff);
            editor.commit();

            ///////////////////// lyaure's changes ///////////////////
            ArrayList<long[]>  screenInterval = retrieveArrayList(sp);
            screenInterval.add(new long[]{System.currentTimeMillis(), 0});
            addToSharedPreference(sp, screenInterval);

        }

        if (intent.getAction().equals(ACTION_CHARGING)) {
            //Toast.makeText(context, "IsCharging", Toast.LENGTH_LONG).show();
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","IsCharging");

            SharedPreferences.Editor editor = sp.edit();
            long tmpCharge = System.currentTimeMillis();
            editor.putLong("tmpCharge", tmpCharge);
            editor.commit();
        }
        else if (intent.getAction().equals(ACTION_DISCHARGING)) {
            //Toast.makeText(context, "Discharging", Toast.LENGTH_LONG).show();
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Discharging");

            long tmpCharge = sp.getLong("tmpCharge", System.currentTimeMillis());

            long durationCharge = System.currentTimeMillis() - tmpCharge;
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","System.currentTimeMillis(): "
                    + System.currentTimeMillis() + " tmpCharge: " + tmpCharge);
            durationCharge /= 1000; // convert time to sec
//            String str = "Duration screen on: " + durationScreenOn + " sec";


            SharedPreferences.Editor editor = sp.edit();
            durationCharge += sp.getLong("charge", 0);
            editor.putLong("charge", durationCharge);
            editor.commit();
            String str = "Duration charge: " + durationCharge + " sec";

            if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);
        }



    }

    private void addToSharedPreference(SharedPreferences sp, ArrayList<long[]> screenInterval) {
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json_data = gson.toJson(screenInterval);
        editor.putString("json_data", json_data);
        editor.commit();
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
