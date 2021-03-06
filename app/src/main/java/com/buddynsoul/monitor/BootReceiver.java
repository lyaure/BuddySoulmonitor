package com.buddynsoul.monitor;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.util.Log;

import com.buddynsoul.monitor.Objects.Database;

public class BootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(final Context context, final Intent intent) {
        if (BuildConfig.DEBUG) Log.d("debug", "booted");

        if (intent.getAction().equals(Intent.ACTION_BOOT_COMPLETED)) {
            SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);

            Database db = Database.getInstance(context);
            if (!prefs.getBoolean("correctShutdown", false)) {
                if (BuildConfig.DEBUG) Log.d("debug","Incorrect shutdown");
                // can we at least recover some steps?
                int steps = Math.max(0, db.getCurrentSteps());
                if (BuildConfig.DEBUG) Log.d("debug","Trying to recover " + steps + " steps");
                db.addToLastEntry(steps);
            }
            // last entry might still have a negative step value, so remove that
            // row if that's the case
            db.removeNegativeEntries();
            db.saveCurrentSteps(0);
            prefs.edit().remove("correctShutdown").apply();

            if (Build.VERSION.SDK_INT >= 26) {
                context.startForegroundService(new Intent(context, MonitorService.class));
            } else {
                context.startService(new Intent(context, MonitorService.class));
            }
        }

    }
}

