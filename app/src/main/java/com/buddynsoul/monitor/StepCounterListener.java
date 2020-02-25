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
import android.os.IBinder;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import java.text.NumberFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;


/**
 * Background service which keeps the step-sensor listener alive to always get
 * the number of steps since boot.
 * <p/>
 * This service won't be needed any more if there is a way to read the
 * step-value without waiting for a sensor event
 */
public class StepCounterListener extends Service implements SensorEventListener {

    int debugHour = 8;
    int debugMinute = 00;

    public static final String ACTION_CHARGING = "android.os.action.CHARGING";
    public static final String ACTION_DISCHARGING = "android.os.action.DISCHARGING";

    public final static int NOTIFICATION_ID = 1;
    private final static long MICROSECONDS_IN_ONE_MINUTE = 60000000;
    private final static long SAVE_OFFSET_TIME = AlarmManager.INTERVAL_HOUR;
    private final static int SAVE_OFFSET_STEPS = 500;

    private static int steps;
    private static int lastSaveSteps;
    private static long lastSaveTime;

    public static String CHANNEL_ID;

    private final BroadcastReceiver shutdownReceiver = new ShutdownReceiver();
    private final BroadcastReceiver screenReceiver = new ScreenReceiver();

    private static SharedPreferences sp;
    private SharedPreferences.Editor editor;

    private float oldPitch, oldRoll, oldAzimuth;
    private boolean flagAccStart;

    @Override
    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (event.values[0] > Integer.MAX_VALUE) {
                if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "probably not a real value: " + event.values[0]);
                return;
            } else {
                steps = (int) event.values[0];
                updateIfNecessary();
            }
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            if (event.values[0] > Integer.MAX_VALUE) {
                if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "probably not a real value: " + event.values[0]);
                return;
            }
            else {
                float luxVal = event.values[0];
                boolean inDarkRoom = sp.getBoolean("inDarkRoom", false);
                if (luxVal == 0 && !inDarkRoom) {
                    //Toast.makeText(this, "Dark room", Toast.LENGTH_SHORT).show();

                    SharedPreferences.Editor editor = sp.edit();
                    long darkRoom = System.currentTimeMillis();
                    editor.putLong("tmpDarkRoom", darkRoom);

                    editor.putBoolean("inDarkRoom", true);
                    editor.commit();
                }
                else if (luxVal != 0 && inDarkRoom) {

                    SharedPreferences.Editor editor = sp.edit();

                    long tmpDarkRoom = sp.getLong("tmpDarkRoom", System.currentTimeMillis());
                    long durationDarkRoom = System.currentTimeMillis() - tmpDarkRoom;
                    durationDarkRoom /= 1000; // convert time to sec

                    long darkRoom = sp.getLong("darkRoom", 0) + durationDarkRoom;
                    editor.putLong("darkRoom", darkRoom);

                    editor.putBoolean("inDarkRoom", false);
                    editor.commit();

                    String str = "Duration darkRoom: " + darkRoom + " sec";
                    if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);
                }
            }
        }

        if(event.sensor.getType() == Sensor.TYPE_ACCELEROMETER){
            if(flagAccStart){
                float tempX = oldPitch - event.values[0];
                float tempY = oldRoll - event.values[1];
                float tempZ = oldAzimuth - event.values[2];

                if(tempX > Math.abs(1) || tempY > Math.abs(1) || tempZ > Math.abs(1)) {
                    //Toast.makeText(this, "phone is moving\n x=" + tempX + "\ny=" + tempY + "\nz=" + tempZ, Toast.LENGTH_LONG).show();

                    long tmpStationary = sp.getLong("tmpStationary", System.currentTimeMillis());

                    long durationStationary = System.currentTimeMillis() - tmpStationary;
                    durationStationary /= 1000; // convert time to sec

//                    long screenOff = sp.getLong("screenOff", System.currentTimeMillis()) + durationScreenOff;
                    SharedPreferences.Editor editor = sp.edit();
                    long stationary = System.currentTimeMillis();
                    editor.putLong("tmpStationary", stationary);
                    stationary = sp.getLong("stationary", 0) + durationStationary;
                    editor.putLong("stationary", stationary);
                    editor.commit();

                    String str = "Duration stationary: " + stationary + " sec";

                    if (BuildConfig.DEBUG) Log.d("DebugStepCounter", str);
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
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", sensor.getName() + " accuracy changed: " + accuracy);
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
            Database db = new Database(this);
            //Database db = Database.getInstance(this);
            if (db.getSteps(Util.getToday()) == Integer.MIN_VALUE) {
                int pauseDifference = steps -
                        getSharedPreferences("pedometer", Context.MODE_PRIVATE)
                                .getInt("pauseCount", steps);
                db.insertNewDay(Util.getToday(), steps - pauseDifference);
                if (pauseDifference > 0) {
                    // update pauseCount for the new day
                    getSharedPreferences("pedometer", Context.MODE_PRIVATE).edit()
                            .putInt("pauseCount", steps).commit();
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

        sp = this.getSharedPreferences("TempData", MODE_PRIVATE);

        reRegisterSensor();
        registerBroadcastReceiver();
        if (!updateIfNecessary()) {
            showNotification();
        }

        // restart service every hour to save the current step count
        long nextUpdate = Math.min(Util.getTomorrow(),
                System.currentTimeMillis() + AlarmManager.INTERVAL_HOUR);
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "next update: " + new Date(nextUpdate).toLocaleString());
        AlarmManager am =
                (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
        PendingIntent pi = PendingIntent
                .getService(getApplicationContext(), 2, new Intent(this, StepCounterListener.class),
                        PendingIntent.FLAG_UPDATE_CURRENT);
        if (Build.VERSION.SDK_INT >= 23) {
            //am.setAndAllowWhileIdle(AlarmManager.RTC, nextUpdate, pi);
            am.setExact(AlarmManager.RTC, nextUpdate, pi);
        } else {
            am.set(AlarmManager.RTC, nextUpdate, pi);
        }

        // calculate sleeping time and add to the db
//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(System.currentTimeMillis());
//        now.set(Calendar.HOUR_OF_DAY, debugHour);
//        now.set(Calendar.MINUTE, debugMinute);
//        now.set(Calendar.SECOND, 0);
        // need to add fix, because if we are not been restarted at 8:00 AM the sleeping time of the current day
        // won't be save in the db
        if (isTimeBetweenTwoHours(8, 20, debugMinute) && !sp.getBoolean("saveInDb", false)) {
            double sleepingTime = calculateSleepingTime();
            Log.d("DebugStepCounter", "SleepingTime: " + sleepingTime);
            Database db = new Database(this);
            //Database db = Database.getInstance(this);
            db.insertSleepingTime(Util.getToday(), sleepingTime);
            editor.putBoolean("saveInDb", true);
            editor.commit();
            //db.close();
        }



//        Log.d("DebugStepCounter", "TimeWhenRestart: " + new Date(Calendar.getInstance().getTimeInMillis()));
//        Log.d("DebugStepCounter", "Time1: " + (int)(Calendar.getInstance().getTimeInMillis()/1000));
//        Log.d("DebugStepCounter", "Time2: " + (int)(now.getTimeInMillis()/1000));
//        if ((int)(Calendar.getInstance().getTimeInMillis())/1000 == (int)(now.getTimeInMillis())/1000) {
//            double sleepingTime = calculateSleepingTime();
//            Log.d("DebugStepCounter", "SleepingTime: " + sleepingTime);
//            Database db = new Database(this);
//            //Database db = Database.getInstance(this);
//            db.insertSleepingTime(Util.getToday(), sleepingTime);
//            //db.close();
//        }

        return START_STICKY;
    }

    // calculate sleeping time in sec
    public static double calculateSleepingTime() {

        final double LIGHT_COEFFICIENT = 0.0626;
        final double LOCK_SCREEN_COEFFICIENT = 0.0772;
        final double CHARGE_COEFFICIENT = 0.0707;
        final double STATIONARY_COEFFICIENT = 0.8218;

        double sleepingTime = 0;

        sleepingTime += sp.getLong("darkRoom", 0) * LIGHT_COEFFICIENT;
        sleepingTime += sp.getLong("screenOff", 0) * LOCK_SCREEN_COEFFICIENT;
        sleepingTime += sp.getLong("charge", 0) * CHARGE_COEFFICIENT;
        sleepingTime += sp.getLong("stationary", 0) * STATIONARY_COEFFICIENT;

        return Math.round(sleepingTime);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","SensorListener onCreate");
    }

//    @Override
//    public void onTaskRemoved(final Intent rootIntent) {
//        super.onTaskRemoved(rootIntent);
//        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","sensor service task removed");
//        // Restart service in 500 ms
//        ((AlarmManager) getSystemService(Context.ALARM_SERVICE))
//                .set(AlarmManager.RTC, System.currentTimeMillis() + 500, PendingIntent
//                        .getService(this, 3, new Intent(this, StepCounterListener.class), 0));
//    }

    @Override
    public void onTaskRemoved(Intent rootIntent){
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","sensor service task removed");
        Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
        restartServiceIntent.setPackage(getPackageName());
        PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 1, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
        AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);
//        alarmService.set(
//                AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);

        if (Build.VERSION.SDK_INT >= 23) {
            //alarmService.setAndAllowWhileIdle(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
            alarmService.setExact(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
        } else {
            alarmService.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 500, restartServicePendingIntent);
        }

        super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","SensorListener onDestroy");
        try {
            SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            sm.unregisterListener(this);
            flagAccStart = false;
            this.unregisterReceiver(shutdownReceiver);
            this.unregisterReceiver(screenReceiver);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("error", e.toString());
            e.printStackTrace();
        }
    }


    public static Notification getNotification(final Context context) {
        if (BuildConfig.DEBUG) Log.d("debug", "getNotification");
        SharedPreferences prefs = context.getSharedPreferences("pedometer", Context.MODE_PRIVATE);
        int goal = prefs.getInt("goal", 10000);

        Database db = new Database(context);
        //Database db = Database.getInstance(context);
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
                        .getActivity(context, 0, new Intent(context, PedometerActivity.class),
                                PendingIntent.FLAG_UPDATE_CURRENT))
                .setSmallIcon(R.drawable.icon)
                .setOngoing(true);
        return notificationBuilder.build();
    }


    private void registerBroadcastReceiver() {
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","register broadcastreceiver");
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_SHUTDOWN);
        registerReceiver(shutdownReceiver, filter);

//        filter.addAction(Intent.ACTION_SCREEN_ON);
//        filter.addAction(Intent.ACTION_SCREEN_OFF);
//        registerReceiver(screenReceiver, filter);

//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(System.currentTimeMillis());

        if (isTimeBetweenTwoHours(20, debugHour, debugMinute)) {
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

    private void reRegisterSensor() {
        if (BuildConfig.DEBUG) Log.d("DebugStepCounter","re-register sensor listener");
        SensorManager sm = (SensorManager) getSystemService(SENSOR_SERVICE);
        try {
            sm.unregisterListener(this);
        } catch (Exception e) {
            if (BuildConfig.DEBUG) Log.d("error",e.toString());
            e.printStackTrace();
        }

        if (BuildConfig.DEBUG) {
            Log.d("DebugStepCounter","step sensors: " + sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size());
            if (sm.getSensorList(Sensor.TYPE_STEP_COUNTER).size() < 1) return; // emulator
            Log.d("DebugStepCounter","default: " + sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER).getName());
            if (sm.getSensorList(Sensor.TYPE_LIGHT).size() < 1) return; // emulator
        }

        // enable batching with delay of max 5 min
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_STEP_COUNTER),
                SensorManager.SENSOR_DELAY_NORMAL, (int) (5 * MICROSECONDS_IN_ONE_MINUTE));

//        Calendar now = Calendar.getInstance();
//        now.setTimeInMillis(System.currentTimeMillis());

        //================= REMOVE ====================
        sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LIGHT), SensorManager.SENSOR_DELAY_NORMAL);
        //Toast.makeText(getApplicationContext(), "Accelerometer Sensor On", Toast.LENGTH_LONG).show();
//        flagAccStart = true;

        sp = this.getSharedPreferences("TempData", MODE_PRIVATE);
        editor = sp.edit();

        if (isTimeBetweenTwoHours(20, debugHour, debugMinute)) {

            editor.putBoolean("saveInDb", true);

            // enable light sensor
            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_LIGHT),
                    SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getApplicationContext(), "Light Sensor On", Toast.LENGTH_LONG).show();

            sm.registerListener(this, sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), SensorManager.SENSOR_DELAY_NORMAL);
            //Toast.makeText(getApplicationContext(), "Accelerometer Sensor On", Toast.LENGTH_LONG).show();
            flagAccStart = true;


            // set alarm to disable the light sensor at upper bound sleeping time
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 3, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            Calendar calendarEnd = Calendar.getInstance();

            calendarEnd.setTimeInMillis(System.currentTimeMillis());

            // if it's after or equal 8 am schedule for next day
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= debugHour &&
                    Calendar.getInstance().get(Calendar.MINUTE) >= debugMinute ) {
                calendarEnd.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
                Log.d("DebugStepCounter", "Alarm will schedule for next day at 8!");
            }
            else{
                Log.d("DebugStepCounter", "Alarm will schedule for today!");
            }
//            calendarEnd.set(Calendar.HOUR_OF_DAY, 8);
//            calendarEnd.set(Calendar.MINUTE, 0);
//            calendarEnd.set(Calendar.SECOND, 0);

            calendarEnd.set(Calendar.HOUR_OF_DAY, debugHour);
            calendarEnd.set(Calendar.MINUTE, debugMinute);
            calendarEnd.set(Calendar.SECOND, 0);

            if (BuildConfig.DEBUG) Log.d("DebugStepCounter", "next alarm: " + new Date(calendarEnd.getTimeInMillis()).toLocaleString());




            if (Build.VERSION.SDK_INT >= 23) {
                alarmService.setExact(AlarmManager.RTC_WAKEUP ,calendarEnd.getTimeInMillis(), restartServicePendingIntent);
            }
            else {
                alarmService.set(AlarmManager.RTC_WAKEUP ,calendarEnd.getTimeInMillis(), restartServicePendingIntent);
            }
        }
        else {
//            sp = this.getSharedPreferences("TempData", MODE_PRIVATE);
//            editor = sp.edit();
            editor.putLong("screenOff", 0);
            editor.putLong("tmpScreenOff", System.currentTimeMillis());
            editor.putLong("stationary", 0);
            editor.putLong("tmpStationary", System.currentTimeMillis());
            editor.putLong("light", 0);
            editor.putLong("tmpLight", System.currentTimeMillis());
            editor.putLong("charge", 0);
            editor.putLong("tmpCharge", System.currentTimeMillis());
           //editor.commit();

            // set alarm to enable the light sensor at lower bound sleeping time
            Intent restartServiceIntent = new Intent(getApplicationContext(), this.getClass());
            restartServiceIntent.setPackage(getPackageName());
            PendingIntent restartServicePendingIntent = PendingIntent.getService(getApplicationContext(), 4, restartServiceIntent, PendingIntent.FLAG_ONE_SHOT);
            AlarmManager alarmService = (AlarmManager) getApplicationContext().getSystemService(Context.ALARM_SERVICE);

            Calendar calendarStart = Calendar.getInstance();

            calendarStart.setTimeInMillis(System.currentTimeMillis());

            // if it's after or equal 8 am schedule for next day
            if (Calendar.getInstance().get(Calendar.HOUR_OF_DAY) >= 20) {
                calendarStart.add(Calendar.DAY_OF_YEAR, 1); // add, not set!
            }
            calendarStart.set(Calendar.HOUR_OF_DAY, 20);
            calendarStart.set(Calendar.MINUTE, 0);
            calendarStart.set(Calendar.SECOND, 0);

            Log.d("DebugStepCounter", "Next Alarm: " + new Date(calendarStart.getTimeInMillis()));

            if (Build.VERSION.SDK_INT >= 23) {
                alarmService.setExact(AlarmManager.RTC_WAKEUP ,calendarStart.getTimeInMillis(), restartServicePendingIntent);
            }
            else {
                alarmService.set(AlarmManager.RTC_WAKEUP ,calendarStart.getTimeInMillis(), restartServicePendingIntent);
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
     * @param toHour Stop Time
     * @return true if Current Time is between fromHour and toHour
     */
    private boolean isTimeBetweenTwoHours(int fromHour, int toHour, int toMinute) {
        //Start Time
        Calendar from = Calendar.getInstance();
        from.set(Calendar.HOUR_OF_DAY, fromHour);
        from.set(Calendar.MINUTE, 0);
        //Stop Time
        Calendar to = Calendar.getInstance();
        to.set(Calendar.HOUR_OF_DAY, toHour);
        to.set(Calendar.MINUTE, toMinute);

        Calendar now = Calendar.getInstance();
        now.getTimeInMillis();
        //now.setTimeInMillis(System.currentTimeMillis());

        if(to.before(from)) {
            if (now.after(to)){
                to.add(Calendar.DATE, 1);
            }
            else {
                from.add(Calendar.DATE, -1);
            }
        }
        return now.after(from) && now.before(to);
    }
}
