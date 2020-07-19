package com.buddynsoul.monitor;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Environment;
import android.os.IBinder;
import android.os.PowerManager;
import android.os.SystemClock;
import android.util.Log;

import com.buddynsoul.monitor.Retrofit.IMyService;
import com.buddynsoul.monitor.Retrofit.RetrofitClient;
import com.buddynsoul.monitor.Utils.Util;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.lang.reflect.Type;
import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.CopyOnWriteArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;


/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class StepCounterListener extends Service implements SensorEventListener {

    public static final String ACTION_CHARGING = "android.os.action.CHARGING";
    public static final String ACTION_DISCHARGING = "android.os.action.DISCHARGING";

    public final static int NOTIFICATION_ID = 1;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    private final static int THIRTY_MIN_TO_MILLISEC = 1800000;

    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;
    private static boolean inSleepingTime;

    private final BroadcastReceiver shutdownReceiver = new ShutdownReceiver();
    private final BroadcastReceiver screenReceiver = new ScreenReceiver();

    private CopyOnWriteArrayList<long[]> lightInterval = new CopyOnWriteArrayList<>();
    private CopyOnWriteArrayList<long[]> stationaryInterval = new CopyOnWriteArrayList<>();
    private static CopyOnWriteArrayList<long[]> finalIntervals = new CopyOnWriteArrayList<>();


    private float oldPitch, oldRoll, oldAzimuth;

    //private int startHour = 20, startMin = 00, endHour = 8, endMin = 00;
    int startHour, startMin, endHour, endMin;


    @Override
    public void onSensorChanged(SensorEvent event) {

        SharedPreferences sp = this.getSharedPreferences("tempData", MODE_PRIVATE);

        // step counter sensor
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (event.values[0] > Integer.MAX_VALUE) {
                if (BuildConfig.DEBUG)
                    Log.d("DebugStepCounter", "probably not a real value: " + event.values[0]);
                return;
            } else {
                steps = (int) event.values[0];
                updateIfNecessary();
            }
        }

        // light sensor
        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (event.values[0] > Integer.MAX_VALUE) {
                if (BuildConfig.DEBUG)
                    Log.d("DebugStepCounter", "probably not a real value: " + event.values[0]);
                return;
            } else {
                float luxVal = event.values[0];
                long[] last = lightInterval.get(lightInterval.size() - 1);
                boolean inDarkRoom = sp.getBoolean("inDarkRoom", false);
//                if (luxVal == 0 && !inDarkRoom) { // dark room - light off
                if (luxVal <= 10 && !inDarkRoom) { // dark room - light off
                    if(last[1] != 0)
                        lightInterval.add(new long[]{System.currentTimeMillis(), 0});
                    //Toast.makeText(this, "Dark room", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sp.edit();
                    long tmpLight = System.currentTimeMillis();
                    editor.putLong("tmpLight", tmpLight);

                    editor.putBoolean("inDarkRoom", true);
                    editor.apply();
//                } else if (luxVal != 0 && inDarkRoom) { // bright room - light on
                } else if (luxVal > 10 && inDarkRoom) { // bright room - light on
//                    if(last[1] == 0)
                    last[1] = System.currentTimeMillis();
//                    Toast.makeText(getApplicationContext(), "ON", Toast.LENGTH_SHORT).show();
                    SharedPreferences.Editor editor = sp.edit();

//                    long tmpLight = sp.getLong("tmpLight", System.currentTimeMillis());
//                    long endCounterLight = System.currentTimeMillis();
//                    long[] interval = {tmpLight, endCounterLight};
//                    lightInterval.add(interval);


                    ///////////////////// lyaure's changes ///////////////////
                    long tmpLight = last[0];

                    long lightDuration = System.currentTimeMillis() - tmpLight;
                    lightDuration /= 1000; // convert time to sec

                    lightDuration += sp.getLong("light", 0);
                    editor.putLong("light", lightDuration);

                    editor.putBoolean("inDarkRoom", false);
                    editor.apply();

                    String str = "Duration darkRoom: " + lightDuration + " sec";
                    if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);


//                    lightInterval.add(new long[]{System.currentTimeMillis(), 0});
                }
            }
        }

        // accelerometer sensor
        if (event.sensor.getType() == Sensor.TYPE_ACCELEROMETER) {

            float tempX = oldPitch - event.values[0];
            float tempY = oldRoll - event.values[1];
            float tempZ = oldAzimuth - event.values[2];

            if (tempX > Math.abs(1) || tempY > Math.abs(1) || tempZ > Math.abs(1)) {
                long currTime = System.currentTimeMillis();
                long[] last = stationaryInterval.get(stationaryInterval.size() -1);

                // get the last time when the phone was stationary
//                long tmpStationary = sp.getLong("tmpStationary", System.currentTimeMillis());
//
                ///////////////////// lyaure's changes ///////////////////
                long tmpStationary = last[0];



                if (BuildConfig.DEBUG)
                    Log.d("DebugStepCounter", "tmp stationary: " + tmpStationary);

                // get the stationary duration time
                long stationaryDuration = currTime - last[0];

                // if more than one second
                if (stationaryDuration >= 1000) {
                    stationaryDuration /= 1000; // convert time to sec

                    if (BuildConfig.DEBUG)
                        Log.d("DebugStepCounter", "phone is moving: x=" + tempX + " y=" + tempY + " z=" + tempZ);

                    long endCounterStationary = System.currentTimeMillis();
//                    long[] interval = {tmpStationary, endCounterStationary};
//                    stationaryInterval.add(interval);

                    last[1] = currTime;

                    // add the current stationary duration our stationary variable
                    stationaryDuration += sp.getLong("stationary", 0);

                    SharedPreferences.Editor editor = sp.edit();
                    editor.putLong("stationary", stationaryDuration);

                    // update the tmpStationary for the next time (event)
                    editor.putLong("tmpStationary", endCounterStationary);
                    editor.apply();

                    String txt_body = new Date(System.currentTimeMillis()).toLocaleString() + ":" + "\n\t\t\t\t\t"
                            + "tmpStationary: " + tmpStationary + "\n\t\t\t\t\t"
                            + "stationaryDuration: " + (System.currentTimeMillis() - tmpStationary) + "\n\t\t\t\t\t"
                            + "stationaryDuration(sec): " + stationaryDuration + "\n\t\t\t\t\t"
                            + "endCounterStationary: " + endCounterStationary + "\n\t\t\t\t\t"
                            + "currentTime: " + System.currentTimeMillis() + "\n";

                    generateNoteOnSD(this, "buddynsoul_debug_sleeping", txt_body);

                    if (BuildConfig.DEBUG)
                        Log.d("DebugStepCounter", "Duration stationary: " + stationaryDuration + " sec");

                    ///////////////////// lyaure's changes ///////////////////
                    stationaryInterval.add(new long[]{System.currentTimeMillis(), 0});
                }

            }

            oldPitch = event.values[0];
            oldRoll = event.values[1];
            oldAzimuth = event.values[2];
        }
    }

    @Override
    public void onAccuracyChanged(final Sensor sensor, int accuracy) {
        // nobody knows what happens here: step value might magically decrease
        // when this method is called...
        if (BuildConfig.DEBUG)
            Log.d("DebugStepCounter", sensor.getName() + " accuracy changed: " + accuracy);
    }

    /**
     * @return true, if notification was updated
     */
    private boolean updateIfNecessary() {
        if (steps > lastSaveSteps + SAVE_OFFSET_STEPS ||
                (steps > 0 && System.currentTimeMillis() > lastSaveTime + SAVE_OFFSET_TIME)) {
            if (BuildConfig.DEBUG) Log.d("DebugStepCounter",
                    "saving steps: steps=" + steps + " lastSave=" + lastSaveSteps +
                            " lastSaveTime=" + new Date(lastSaveTime));
//            Database db = new Database(this);
            Database db = Database.getInstance(this);
            if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
                int pauseDifference = steps -
                        getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                                .getInt("pauseCount", steps);
                db.insertNewDay(Util.getToday(), steps - pauseDifference);
                if (pauseDifference > 0) {
                    // update pauseCount for the new day
                    getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                            .putInt("pauseCount", steps).apply();
                }
            }
            db.saveCurrentSteps(steps);
            //db.close();
            lastSaveSteps = steps;
            lastSaveTime = System.currentTimeMillis();
            showNotification(); // update notification
            //WidgetUpdateService.enqueueUpdate(this);
            return true;
        } else {
            return false;
        }
    }

    private void showNotification() {
        if (Build.VERSION.SDK_INT >= 26) {
            startForeground(NOTIFICATION_ID, getNotification(this));
        } else if (getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                .getBoolean("notification", true)) {
            ((NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE))
                    .notify(NOTIFICATION_ID, getNotification(this));
        }
    }

    @Override
    public IBinder onBind(final Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(final Intent intent, int flags, int startId) {

        SharedPreferences sp = this.getSharedPreferences("tempData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        SharedPreferences settings_preferences = this.getSharedPreferences("prefTime", MODE_PRIVATE);
//        SharedPreferences.Editor editor1 = settings_preferences.edit();
//        editor1.clear();
//        editor1.commit();


        startHour = settings_preferences.getInt("fromHour", 8);
        startMin = settings_preferences.getInt("fromMinute", 0);

        endHour = settings_preferences.getInt("toHour", 8);
        endMin = settings_preferences.getInt("toMinute", 0);

        String am_pm_from = settings_preferences.getString("am_pm_from", "pm");
        String am_pm_to = settings_preferences.getString("am_pm_to", "am");

        if(am_pm_from.equals("pm") && startHour != 12) {
            startHour += 12;
        }

        if(am_pm_to.equals("pm") && endHour != 12){
            endHour += 12;
        }


        if (isTimeBetweenTwoHours(startHour, startMin, endHour, endMin)) { //slepping time mode

            if (!sp.contains("initializedSensorsValue") || !sp.getBoolean("initializedSensorsValue", true)) {

                SharedPreferences user_sp = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor user_editor = user_sp.edit();
                user_editor.putBoolean("sendToServer", false);
                user_editor.apply();


                inSleepingTime = true;

                editor.putBoolean("initializedSensorsValue", true);
                editor.putBoolean("inDarkRoom", false);

                Log.d("DebugStepCounter", "stationary(need to be 0 at start): " + sp.getLong("stationary", 0));

                editor.putLong("stationary", 0);
                editor.putLong("tmpStationary", System.currentTimeMillis());

                finalIntervals.clear();

                ///////////////////// lyaure's changes ///////////////////
                stationaryInterval.clear();
                stationaryInterval.add(new long[]{System.currentTimeMillis(), 0});

                editor.putLong("screenOff", 0);
                editor.putLong("tmpScreenOff", System.currentTimeMillis());

                editor.putLong("light", 0);
                editor.putBoolean("inDarkRoom", true);
                editor.putLong("tmpLight", System.currentTimeMillis());
                editor.putString("json_data", "");
                editor.apply();

                ///////////////////// lyaure's changes ///////////////////
                lightInterval.clear();
                lightInterval.add(new long[]{System.currentTimeMillis(), 0});

                CopyOnWriteArrayList<long[]>  screenInterval = retrieveArrayList(sp);
                screenInterval.clear();
                PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
                boolean isScreenOn = Util.hasLollipop() ? pm.isInteractive() : pm.isScreenOn();
                if(!isScreenOn){
                    screenInterval.add(new long[]{System.currentTimeMillis(), 0});
                    addToSharedPreference(sp, screenInterval);
                }

                String txt_body = new Date(System.currentTimeMillis()).toLocaleString() + ":" + "\n\t\t\t\t\t"
                        + "tmpStationary: " + sp.getLong("tmpStationary", System.currentTimeMillis()) + "\n\t\t\t\t\t"
                        + "stationary(need to be 0 at start): " + sp.getLong("stationary", 0) + "\n\t\t\t\t\t";

                generateNoteOnSD(this, "buddynsoul_debug_sleeping", txt_body);
            }

            if (!sp.getBoolean("nightLocationSavedInDb", false)) {

                String lastLocation = getLocation.getLastLocation(null, this);

                if (!lastLocation.equals("")) {
//                    Database db = new Database(this);
                    Database db = Database.getInstance(this);
                    // if it's after or equal to midnight, we need to select the date of the day before
                    long update_date = -1;
                    if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) <= 8) {
                        update_date = Util.getYesterday();
                    } else {
                        update_date = Util.getToday();
                    }

                    db.insertLocation(update_date, lastLocation, "night_location");

                    editor.putBoolean("nightLocationSavedInDb", true);
                    editor.commit();

                    Log.d("DebugStepCounter", "Location night: " + lastLocation);
                }

            }
        }
        else {            // not sleeping time mode

            inSleepingTime = false;

            // calculate sleeping time and add it to the db
            if (!sp.getBoolean("sleepingTimeSavedInDb", false)) {

                int sleepingTime = calculateSleepingTime();

                Log.d("DebugStepCounter", "########## Saving sleepingTime ############");

                Database db = Database.getInstance(this);

                if(!finalIntervals.isEmpty())
                    db.insertSleepingTime(Util.getYesterday(), sleepingTime, finalIntervals.get(0)[0], finalIntervals.get(finalIntervals.size()-1)[1]);
                else
                    db.insertSleepingTime(Util.getYesterday(), sleepingTime, 0, 0);
                //db.insertSleepingTime(Util.getYesterday(), sp.getLong("stat", 0));

                editor.putInt("sleepingTime", sleepingTime);

                editor.putBoolean("sleepingTimeSavedInDb", true);
                editor.commit();

                Log.d("DebugStepCounter", "SleepingTime: " + sleepingTime);
                Log.d("Stat", "Stat: " + sp.getLong("stat", 0));
            }

            // find the last location and add it to the db
            String lastLocation = getLocation.getLastLocation(null, this);
            if (!sp.getBoolean("morningLocationSavedInDb", false) && !lastLocation.equals("")) {
//                Database db = new Database(this);
                Database db = Database.getInstance(this);
                db.insertLocation(Util.getToday(), lastLocation, "morning_location");

                editor.putBoolean("morningLocationSavedInDb", true);
                editor.commit();

                Log.d("DebugStepCounter", "Location morning: " + lastLocation);
            }

            editor.putBoolean("initializedSensorsValue", false);
            editor.commit();

            // todo send data to the server
            if(!getSharedPreferences("user", MODE_PRIVATE).getBoolean("sendToServer", true)) {
                sendData();
            }
        }


        // check the current time and set relevant alarm (to start/stop the sleeping time algorithm)
        reRegisterSensorAndSetAlarm();
        registerBroadcastReceiver();

        if (!updateIfNecessary()) {
            showNotification();
        }

        // restart service every hour to save the current step count
        long nextUpdate = Math.min(Util.getTomorrow(), System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);

        if (BuildConfig.DEBUG)
            Log.d("DebugStepCounter", "next update: " + new Date(nextUpdate).toLocaleString());

        AlarmManager am = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent
                .getService(getApplicationContext(), 2, new Intent(this, StepCounterListener.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);

        if (Build.VERSION.SDK_INT >= 23) {
            am.setExactAndAllowWhileIdle(AlarmManager.RTC, nextUpdate, pi);
            //am.setExact(AlarmManager.RTC, nextUpdate, pi);
        } else {
            am.set(AlarmManager.RTC, nextUpdate, pi);
        }

        return START_STICKY;
    }

    // calculate sleeping time in sec
    private int calculateSleepingTime() {
        // TODO remove if not necessary
        if(lightInterval.isEmpty() || stationaryInterval.isEmpty())
            return 0;

        SharedPreferences sp = this.getSharedPreferences("tempData", MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // add last interval for stationary sensor
//        long startCounterStationary = sp.getLong("tmpStationary", System.currentTimeMillis());
//        long endCounterStationary = System.currentTimeMillis();
//        long[] interval = {startCounterStationary, endCounterStationary};
//        stationaryInterval.add(interval);

        ///////////////////// lyaure's changes ///////////////////
        long[] stationaryLast = stationaryInterval.get(stationaryInterval.size() -1);
        stationaryLast[1] = System.currentTimeMillis();

        // add last interval for light sensor
        long tmp3 = sp.getLong("light", 0);
        if (sp.getBoolean("inDarkRoom", false)) {
//            long startCounterLight = sp.getLong("tmpLight", System.currentTimeMillis());
//            long endCounterLight = System.currentTimeMillis();
//            interval[0] = startCounterLight;
//            interval[1] = endCounterLight;
//            lightInterval.add(interval);

            long[] lightLast = lightInterval.get(lightInterval.size() - 1);
            lightLast[1] = System.currentTimeMillis();


            tmp3 += (System.currentTimeMillis() - sp.getLong("tmpLight", System.currentTimeMillis())) / 1000;
        }


        // add last interval for screen sensor
        long tmp2 = sp.getLong("screenOff", 0);

        CopyOnWriteArrayList<long[]> screenIntervals = retrieveArrayList(sp);

        PowerManager pm = (PowerManager) getSystemService(Context.POWER_SERVICE);
        boolean isScreenOn = Util.hasLollipop() ? pm.isInteractive() : pm.isScreenOn();

        // if screen off when sleeping time is done we save the last values
        if (!isScreenOn) {
//            long startCounterScreen = sp.getLong("tmpScreenOff", System.currentTimeMillis());
//            long endCounterScreen = System.currentTimeMillis();
//            interval[0] = startCounterScreen;
//            interval[1] = endCounterScreen;
//            screenIntervals.add(interval);

            ///////////////////// lyaure's changes ///////////////////
            long[] screenLast = screenIntervals.get(screenIntervals.size() - 1);
            screenLast[1] = System.currentTimeMillis();

            addToSharedPreference(sp, screenIntervals);
            tmp2 += (System.currentTimeMillis() - sp.getLong("tmpScreenOff", System.currentTimeMillis())) / 1000;
        }

        //////////////////////////////////// for printing in file //////////////////////////////////////////////

        long tmp = sp.getLong("stationary", 0);
        tmp += (System.currentTimeMillis() - sp.getLong("tmpStationary", System.currentTimeMillis())) / 1000;
        editor.putLong("stat", tmp);



        String txt_body = new Date(System.currentTimeMillis()).toLocaleString() + "\n\t\t\t\t\t"
                //+ "stationary: " + sp.getLong("stationary", 0) + "\n\t\t\t\t\t"
                + "currentTimeMillis(): " + System.currentTimeMillis() + "\n\t\t\t\t\t"
                + "Stationary sensor: " + tmp + "\n\t\t\t\t\t"
                //+ "tmpStationary: " + sp.getLong("tmpStationary", System.currentTimeMillis()) + "\n\t\t\t\t\t"
                //+ "Calculate sleeping time(stationary): " + tmp + "\n\t\t\t\t\t"
                + "Screen sensor: " + tmp2 + "\n\t\t\t\t\t"
                + "Light sensor: " + tmp3 + "\n\t\t\t\t\t";

        txt_body += "Stationary interval: " + arrayListToString(stationaryInterval) + "\n\t\t\t\t\t"
                + "Dark room interval: " + arrayListToString(lightInterval) + "\n\t\t\t\t\t"
                + "Screen off interval: " + arrayListToString(retrieveArrayList(sp)) + "\n";

        generateNoteOnSD(this, "buddynsoul_debug_sleeping", txt_body);

        /////////////////////////////////////////////////////////////////////////////////////////////////////////

        editor.putBoolean("sleepingTimeStart", false);
        editor.commit();

        //////// todo NEW ALGORITHM  /////////
        return calculateTimeDifference(stationaryInterval, lightInterval, retrieveArrayList(sp));
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "SensorListener onCreate");
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "sensor service task removed");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

        if (Build.VERSION.SDK_INT >= 23) {
            alarmService.setExactAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
            //alarmService.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
        } else {
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
        }

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
            this.unregisterReceiver(shutdownReceiver);
            this.unregisterReceiver(screenReceiver);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("error", e.toString());
            e.printStackTrace();
        }
    }


    public static Notification getNotification(final Context context) {
        if (BuildConfig.DEBUG) Log.d("debug", "getNotification");
        SharedPreferences sp_pedometer = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        int goal = sp_pedometer.getInt("goal", 10000);

//        Database db = new Database(context);
        Database db = Database.getInstance(context);
        int today_offset = db.getSteps(Util.getToday());
        if (steps == 0)
            steps = db.getCurrentSteps(); // use saved value if we haven't anything better
        //db.close();
        Notification.Builder notificationBuilder =
                Build.VERSION.SDK_INT >= 26 ? getNotificationBuilder(context) :
                        new Notification.Builder(context);
        if (steps > 0) {
            if (today_offset == Integer.MIN_VALUE) today_offset = -steps;
            NumberFormat format = NumberFormat.getInstance(Locale.getDefault());
            notificationBuilder.setProgress(goal, today_offset + steps, false).setContentText(
                    today_offset + steps >= goal ?
                            context.getString(R.string.goal_reached_notification,
                                    format.format((today_offset + steps))) :
                            context.getString(R.string.notification_text,
                                    format.format((goal - today_offset - steps)))).setContentTitle(
                    format.format(today_offset + steps) + " " + context.getString(R.string.steps));
        } else { // still no step value?
            notificationBuilder.setContentText(
                    context.getString(R.string.your_progress_will_be_shown_here_soon))
                    .setContentTitle(context.getString(R.string.notification_title));
        }
        notificationBuilder.setPriority(Notification.PRIORITY_MIN).setShowWhen(false)
                .setContentIntent(PendingIntent
                        .getActivity(context, 0, new Intent(context, MonitorActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true);
        return notificationBuilder.build();
    }


    private void registerBroadcastReceiver() {

        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "register broadcastreceiver");

        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);

        //if (isTimeBetweenTwoHours(startHour, startMin, endHour, endMin)) {
        if (inSleepingTime) {
            if (Build.VERSION.SDK_INT >= 23) {
                filter = new IntentFilter();
                filter.addAction(ACTION_CHARGING);
                filter.addAction(ACTION_DISCHARGING);
                filter.addAction(Intent.ACTION_SCREEN_ON);
                filter.addAction(Intent.ACTION_SCREEN_OFF);
                registerReceiver(screenReceiver, filter);
            }
        }
    }

    private void reRegisterSensorAndSetAlarm() {

        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "re-register sensor listener");

        // open the 'tempData' shared preference file
        SharedPreferences sp = this.getSharedPreferences("tempData", Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sp.edit();

        // create a sensor manager object to reregister sensors
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        try {
            sm.unregisterListener(this);
            //Toast.makeText(this, "OFF", Toast.LENGTH_LONG).show();
            this.unregisterReceiver(screenReceiver);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("error", e.toString());
            e.printStackTrace();
        }

        // can't register the sensor if we are in an emulator
        if (BuildConfig.DEBUG) {
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
        }

        // enable batching with delay of max 5 min
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, (int) (5 * MICROSECONDS_IN_ONE_MINUTE));

        //if (isTimeBetweenTwoHours(startHour, startMin, endHour, endMin)) {
        if (inSleepingTime) { //night
            // in this hour interval we enable the light and the accelerometer sensor

            editor.putBoolean("sleepingTimeSavedInDb", false);
            editor.putBoolean("morningLocationSavedInDb", false);

            if (BuildConfig.DEBUG) {
                if (sm.getSensorList(Sensor.TYPE_LIGHT).size() < 1) return; // emulator
            }

            // register light sensor
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);

            // register accelerometer sensor
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                    SensorManager.SENSOR_DELAY_NORMAL);

            // set alarm to disable the light and accelerometer sensor at the end of sleeping time
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());

            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(),
                    3, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            Calendar calendarEnd = Calendar.getInstance();
            calendarEnd.setTimeInMillis(System.currentTimeMillis());

            // if it's after or equal 8 am schedule for next day
//            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 8 &&
//                    Calendar.getInstance().get(Calendar.MINUTE) >= 00 ) {
            String to_debug = "\nActual Hour: " + Calendar.getInstance().get(Calendar.HOUR_OF_DAY) + " End Hour: " + endHour +"\n"
                    + "Actual Min: " + Calendar.getInstance().get(Calendar.MINUTE) + " End Minute: " + endMin;

            Log.d("StepCounterDebug", to_debug);

            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= endHour && Calendar.getInstance().get(Calendar.MINUTE) >= endMin) {
                calendarEnd.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
                //Log.d("DebugStepCounter", "Alarm will schedule for next day at " + endHour + ": " + endMin +" !");
            } else {
                //Log.d("DebugStepCounter", "Alarm will schedule for today!");
            }

            calendarEnd.set(Calendar.HOUR_OF_DAY, endHour);
            calendarEnd.set(Calendar.MINUTE, endMin);
            calendarEnd.set(Calendar.SECOND, 0);

            if (BuildConfig.DEBUG)
                Log.d("DebugStepCounter", "Next Alarm1: " + new Date(calendarEnd.getTimeInMillis()).toLocaleString());

            if (Build.VERSION.SDK_INT >= 23) {
                //alarmService.setExact(AlarmManager.RTC_WAKEUP ,calendarEnd.getTimeInMillis(), restartServicePendingIntent);
                alarmService.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarEnd.getTimeInMillis(), restartServicePendingIntent);
            } else {
                alarmService.set(AlarmManager.RTC_WAKEUP, calendarEnd.getTimeInMillis(), restartServicePendingIntent);
            }
        } else { //day

            editor.putBoolean("nightLocationSavedInDb", false);


            // set alarm to enable the light sensor at lower bound sleeping time
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 4, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            Calendar calendarStart = Calendar.getInstance();

            calendarStart.setTimeInMillis(System.currentTimeMillis());

            // if it's after or equal 8 am schedule for next day
//            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 20) {
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) == startHour && Calendar.getInstance().get(Calendar.MINUTE) >= startMin
                    || Calendar.getInstance().get(Calendar.HOUR_OF_DAY) > startHour) {
                calendarStart.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
            }
            calendarStart.set(Calendar.HOUR_OF_DAY, startHour);
            calendarStart.set(Calendar.MINUTE, startMin);
            calendarStart.set(Calendar.SECOND, 0);

            Log.d("DebugStepCounter", "Next Alarm2: " + new Date(calendarStart.getTimeInMillis()));

            if (Build.VERSION.SDK_INT >= 23) {
                //alarmService.setExact(AlarmManager.RTC_WAKEUP ,calendarStart.getTimeInMillis(), restartServicePendingIntent);
                alarmService.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), restartServicePendingIntent);
            } else {
                alarmService.set(AlarmManager.RTC_WAKEUP, calendarStart.getTimeInMillis(), restartServicePendingIntent);
            }
        }
        editor.commit();
    }

    @TargetApi(Build.VERSION_CODES.O)
    public static Notification.Builder getNotificationBuilder(final Context context) {
        final String NOTIFICATION_CHANNEL_ID = "Notification";
        NotificationManager manager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        NotificationChannel channel =
                new NotificationChannel(NOTIFICATION_CHANNEL_ID, NOTIFICATION_CHANNEL_ID,
                        NotificationManager.IMPORTANCE_LOW);
        channel.setImportance(NotificationManager.IMPORTANCE_LOW); // ignored by Android O ...
        channel.enableLights(false);
        channel.enableVibration(false);
        channel.setBypassDnd(false);
        channel.setSound(null, null);
        manager.createNotificationChannel(channel);
        Notification.Builder builder = new Notification.Builder(context, NOTIFICATION_CHANNEL_ID);
        return builder;
    }

    /**
     * @param fromHour Start Time
     * @param toHour   Stop Time
     * @return true if Current Time is between fromHour(include) and toHour(exclude)
     */
    private boolean isTimeBetweenTwoHours(int fromHour, int fromMinute, int toHour, int toMinute) {

        //Start Time
        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR_OF_DAY, fromHour);
        from.set(Calendar.MINUTE, fromMinute);

        //Stop Time
        Calendar to = Calendar.getInstance();
        to.set(Calendar.HOUR_OF_DAY, toHour);
        to.set(Calendar.MINUTE, toMinute);

        Calendar now = Calendar.getInstance();

        if (to.before(from)) {
            if (now.after(to)) {
                to.add(Calendar.DATE, 1);
            } else {
                from.add(Calendar.DATE, -1);
            }
        }
        return now.getTimeInMillis() >= from.getTimeInMillis() && now.getTimeInMillis() < to.getTimeInMillis();
    }

    public void generateNoteOnSD(Context context, String sFileName, String sBody) {
        try {
            File root = new File(Environment.getExternalStorageDirectory(), "buddynsoul_debug");
            if (!root.exists()) {
                boolean mkdirs = root.mkdirs();
                if (!mkdirs)
                    return;
            }
            File file = new File(root, sFileName + ".txt");
            FileOutputStream writer = new FileOutputStream(file, true);
            OutputStreamWriter streamWriter = new OutputStreamWriter(writer);
            sBody = "\n" + sBody;
            streamWriter.append(sBody);
            streamWriter.flush();
            streamWriter.close();
            writer.close();
            //Toast.makeText(context, "Saved", Toast.LENGTH_SHORT).show();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private CopyOnWriteArrayList<long[]> retrieveArrayList(SharedPreferences sp) {
        Gson gson = new Gson();
        String json_data = sp.getString("json_data", "");

        if (json_data.equals("")) {
            return new CopyOnWriteArrayList<long[]>();
        }

        Type type = new TypeToken<CopyOnWriteArrayList<long[]>>() {
        }.getType();
        CopyOnWriteArrayList<long[]> screenInterval = gson.fromJson(json_data, type);

        return screenInterval;
    }

    private void addToSharedPreference(SharedPreferences sp, CopyOnWriteArrayList<long[]> screenInterval) {
        SharedPreferences.Editor editor = sp.edit();
        Gson gson = new Gson();
        String json_data = gson.toJson(screenInterval);
        editor.putString("json_data", json_data);
        editor.commit();
    }

    private StringBuilder arrayListToString(CopyOnWriteArrayList<long[]> arrayListToConvert) {
        StringBuilder str = new StringBuilder();
        str.append("[ ");
        for (int i = 0; i < arrayListToConvert.size(); i++) {
            long[] s = arrayListToConvert.get(i);
            str.append("[").append(s[0]).append(", ");
            str.append(s[1]).append("]");
            if (i != arrayListToConvert.size() - 1) {
                str.append(", ");
            }
        }
        str.append(" ]");
        return str;
    }

    private static long[] isThereOverlap(long[] interval_1, long[] interval_2) {

        final int START = 0;
        final int END = 1;

        if(interval_1[START] > interval_2[END]) {
            return null;
        }
        else if(interval_2[START] > interval_1[END]) {
            return new long[] {Long.MIN_VALUE, Long.MIN_VALUE};
        }
        else {
            return new long[] { Math.max(interval_1[START], interval_2[START]),
                    Math.min(interval_1[END], interval_2[END])};
        }
    }

    private static int calculateTimeDifference(CopyOnWriteArrayList<long[]> list_1,
                                               CopyOnWriteArrayList<long[]> list_2,
                                               CopyOnWriteArrayList<long[]> list_3) {
        
        cleanData(list_1);
        cleanData(list_2);
        cleanData(list_3);

        CopyOnWriteArrayList<long[]> first_result = new CopyOnWriteArrayList<>();

        for (long[] interval_1: list_1) {
            for (long[] interval_2: list_2) {
                long[] res = isThereOverlap(interval_1, interval_2);
                if(res != null) {
                    if(res[0] == Long.MIN_VALUE) {
                        break;
                    }
                    else {
                        first_result.add(res);
                    }
                }
            }
        }

        for (long[] interval_1: first_result) {
            for (long[] interval_2: list_3) {
                long[] res = isThereOverlap(interval_1, interval_2);
                if(res != null) {
                    if(res[0] == Long.MIN_VALUE) {
                        break;
                    }
                    else {
                        finalIntervals.add(res);
                    }
                }
            }
        }

        long sleeping_time = 0;
        for (long[] interval : finalIntervals) {
            sleeping_time += interval[1] - interval[0];
        }
        return (int) sleeping_time / 1000;
    }

    private void sendData() {

        SharedPreferences sp = this.getSharedPreferences("user", MODE_PRIVATE);
        String refreshToken = sp.getString("refreshToken", "");

        String dataToSend = preprocessData();

        IMyService iMyService = RetrofitClient.getClient().create(IMyService.class);

        Call<String> todoCall = iMyService.sendData(refreshToken, dataToSend);
        todoCall.enqueue(new Callback<String>() {
            @Override
            public void onResponse(Call<String> call, Response<String> response) {
                SharedPreferences sp = getSharedPreferences("user", MODE_PRIVATE);
                SharedPreferences.Editor editor = sp.edit();
                editor.putBoolean("sendToServer", true);
                editor.apply();
            }

            @Override
            public void onFailure(Call<String> call, Throwable t) {

            }
        });

    }

    private String preprocessData() {

        long timestamps = Util.getYesterday();
        Database db = Database.getInstance(this);
        int steps = db.getSteps(timestamps);
        long asleepTime = db.getAsleep(timestamps);
        long wokeUpTime = db.getWokeUp(timestamps);
        int deepSleep = db.getDeepSleep(timestamps);
        String morning_location = db.getLocation(timestamps, "morning_location");
        String night_location = db.getLocation(timestamps, "night_location");
        int stepGoal = db.getStepGoal(timestamps);
        int sleepGoal = db.getSleepGoal(timestamps);

        JSONObject json = new JSONObject();

        try {
            json.put("timestamps", String.valueOf(timestamps));
            json.put("steps", steps);
            json.put("step_goal", stepGoal);
            json.put("asleep_time", asleepTime);
            json.put("woke_up_time", wokeUpTime);
            json.put("deep_sleep", deepSleep);
            json.put("sleep_goal", sleepGoal);
            json.put("morning_location", morning_location);
            json.put("night_location", night_location);
        } catch (JSONException e) {
           //e.printStackTrace();
        }
        return json.toString();
    }


    private static void cleanData(CopyOnWriteArrayList<long[]> data){
        long diff = 0;

        for(long[] inter : data){
            diff += inter[1] - inter[0];
            if(diff < THIRTY_MIN_TO_MILLISEC)
                data.remove(inter);
            else
                break;
        }

        for(int i=data.size()-1; i>=0; i--){
            diff = data.get(i)[1] - data.get(i)[0];
            if(diff < THIRTY_MIN_TO_MILLISEC)
                data.remove(i);
            else
                break;

        }
    }

}
