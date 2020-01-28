package com.buddynsoul.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.hardware.SensorEventListener;
import android.util.Log;
import android.widget.Toast;

import static android.content.Context.MODE_PRIVATE;

public class ScreenReceiver extends BroadcastReceiver {

    public static final String ACTION_CHARGING = "android.os.action.CHARGING";
    public static final String ACTION_DISCHARGING = "android.os.action.DISCHARGING";

    @Override
    public void onReceive(final Context context, final Intent intent) {
        //if (BuildConfig.DEBUG) Log.d("debug","shutting down");

        SharedPreferences sp = context.getSharedPreferences("TempData", MODE_PRIVATE);


        if (intent.getAction().equals(Intent.ACTION_SCREEN_ON)) {
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen On");

            long screenOff = sp.getLong("screenOff", System.currentTimeMillis());

            long durationScreenOff = System.currentTimeMillis() - screenOff;
            durationScreenOff /= 1000; // convert time to sec
            String str = "Duration screen off: " + durationScreenOff + " sec";
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);
        }
        else if (intent.getAction().equals(Intent.ACTION_SCREEN_OFF)) {
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Screen Off");

            SharedPreferences.Editor editor = sp.edit();
            long screenOff = System.currentTimeMillis();
            editor.putLong("screenOff", screenOff);
            editor.commit();
        }

        if (intent.getAction().equals(ACTION_CHARGING)) {
            Toast.makeText(context, "IsCharging", Toast.LENGTH_LONG).show();
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","IsCharging");
        }
        else if (intent.getAction().equals(ACTION_DISCHARGING)) {
            Toast.makeText(context, "Discharging", Toast.LENGTH_LONG).show();
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter","Discharging");
        }



    }
}
