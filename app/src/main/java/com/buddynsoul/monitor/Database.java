package com.buddynsoul.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import com.buddynsoul.monitor.Utils.Util;

import java.util.Calendar;

public class Database extends SQLiteOpenHelper {
//    private SQLiteDatabase db;
    private static final String DB_NAME = "monitor";
    private static final int DB_VERSION = 1;
    public static Database instance;


    @Override
    public void onCreate(SQLiteDatabase db) {
        String query = "CREATE TABLE IF NOT EXISTS " + DB_NAME + "(date INTEGER, steps INTEGER, " +
                "morning_location STRING, night_location STRING, " +
                "sleepDuration INTEGER, asleep INTEGER, wokeUp INTEGER, " +
                "deepSleep INTEGER, lightSleep INTEGER)";
        db.execSQL(query);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
//        if(oldVersion == 1){
//            db.execSQL("ALTER TABLE " + DB_NAME + " ADD COLUMN asleep INTEGER DEFAULT -1");
//            db.execSQL("ALTER TABLE " + DB_NAME + " ADD COLUMN wokeUp INTEGER DEFAULT -1");
//        }
    }

    public Database(Context context){
        super(context, DB_NAME, null, DB_VERSION);
    }

    // ensures that only one Database will ever exist at any given time
    public static synchronized Database getInstance(Context context){
        if(instance == null)
            instance = new Database(context.getApplicationContext());

        return instance;
    }

    public void insertNewDay(long date, int steps){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(!cursor.moveToFirst() && steps >= 0){
            addToLastEntry(steps);

            ContentValues cv = new ContentValues();
            cv.put("date", date);
            cv.put("steps", -steps);
            cv.put("morning_location", "");
            cv.put("night_location", "");
            cv.put("sleepDuration", -1);
            cv.put("asleep", -1);
            cv.put("wokeUp", -1);
            cv.put("deepSleep", -1);
            cv.put("lightSleep", -1);


            db.insert(DB_NAME, null, cv);

            cursor.close();

            if (BuildConfig.DEBUG) {
                Log.d("debug","insertDay " + date + " / " + steps);
            }
        }
    }

    public void insertBackupDay(long date, int steps, String morningLocation, String nightLocation,
                                int sleepDuration, int asleep, int wokeUp,
                                int deepSleep, int lightSleep){

        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(!cursor.moveToFirst()){

            ContentValues cv = new ContentValues();
            cv.put("date", date);
            cv.put("steps", steps);
            cv.put("morning_location", morningLocation);
            cv.put("night_location", nightLocation);
            cv.put("sleepDuration", sleepDuration);
            cv.put("asleep", asleep);
            cv.put("wokeUp", wokeUp);
            cv.put("deepSleep", deepSleep);
            cv.put("lightSleep", lightSleep);

            db.insert(DB_NAME, null, cv);

            cursor.close();

            if (BuildConfig.DEBUG) {
                Log.d("debug","insertBackUpDay " + date + " / " + steps);
            }
        }

    }

    public void insertSleepingTime(long date, int deepSleep, long asleep, long wokeUp){
        SQLiteDatabase db = getWritableDatabase();
        Log.d("DebugStepCounter: ", "Db Update Sleeping Time");
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(cursor.moveToFirst()){
            int duration = (int)(wokeUp - asleep)/1000;
            int tmpSleep = cursor.getInt(cursor.getColumnIndex("deepSleep"));
//            if(deepSleep > tmpSleep){
                ContentValues cv = new ContentValues();
                cv.put("sleepDuration", duration);
                cv.put("asleep", asleep);
                cv.put("wokeUp", wokeUp);
                cv.put("deepSleep", deepSleep);
                cv.put("lightSleep", duration - deepSleep);
                db.update(DB_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
//            }
        }

        cursor.close();

        if (BuildConfig.DEBUG) {
            Log.d("debug","insertSleepingTime " + date + " / " + deepSleep);
        }
    }

    public void insertLocation(long date, String location, String params){
        SQLiteDatabase db = getWritableDatabase();
        Log.d("DebugStepCounter: ", "Db Update Sleeping Time");
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(cursor.moveToFirst()){
            String tmpSleepingTime = cursor.getString(cursor.getColumnIndex(params));
            if(!location.equals(tmpSleepingTime)){
                ContentValues cv = new ContentValues();
                cv.put(params, location);
                db.update(DB_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
            }
        }

        cursor.close();

        if (BuildConfig.DEBUG) {
            Log.d("debug","insertLocation " + date + " / " + location);
        }
    }

    public void addToLastEntry(int steps){
        SQLiteDatabase db = getWritableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " ORDER BY date DESC", null );

        if(cursor.moveToFirst()){
            steps += cursor.getInt(cursor.getColumnIndex("steps"));
            ContentValues cv = new ContentValues();
            cv.put("steps", steps);

            long date = cursor.getLong(cursor.getColumnIndex("date"));

            db.update(DB_NAME, cv, "date = ?", new String[]{String.valueOf(date)});

            cursor.close();
        }
    }

    public int getTotalWithoutToday(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(steps) FROM " + DB_NAME + " WHERE steps > 0 AND date > 0 AND date < " + Util.getToday(), null);

        cursor.moveToFirst();
        int res = cursor.getInt(0);
        cursor.close();

        return res;
    }

    public int getSteps(final long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = Integer.MIN_VALUE;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("steps"));

        cursor.close();
        return res;
    }

    public int getSleepDuration(final long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("sleepDuration"));

        cursor.close();
        return res;
    }

    public int getAsleep(long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("asleep"));

        cursor.close();
        return res;
    }

    public int getWokeUp(long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("wokeUp"));

        cursor.close();
        return res;
    }

    public int getDeepSleep(long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("deepSleep"));

        cursor.close();
        return res;
    }

    public int getLightSleep(long date){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("lightSleep"));

        cursor.close();
        return res;
    }


    public String getLocation(final long date, String columnName){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        String res = "";

        if(cursor.moveToFirst())
            res = cursor.getString(cursor.getColumnIndex(columnName));

        cursor.close();
        return res;
    }

    public void removeNegativeEntries(){
        SQLiteDatabase db = getWritableDatabase();
        db.delete(DB_NAME, "steps < ?", new String[]{"0"});
    }

    public int getDaysWithoutToday(){
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DB_NAME + " WHERE steps > 0 AND date > 0 AND date < " + Util.getToday(), null);
        cursor.moveToFirst();

        int res = cursor.getInt(0);
        cursor.close();

        return res < 0 ? 0 : res;
    }

    public int getDays(){
        return getDaysWithoutToday() + 1;
    }

    public void saveCurrentSteps(int steps){
        SQLiteDatabase db = getWritableDatabase();

        ContentValues cv = new ContentValues();
        cv.put("steps", steps);

        if(db.update(DB_NAME, cv, "date = ?", new String[]{"-1"}) == 0){
            cv.put("date", -1);
            db.insert(DB_NAME, null, cv);
        }

        if (BuildConfig.DEBUG) {
            Log.d("debug", "saving steps in db: " + steps);
        }
    }

    public int getCurrentSteps() {
        int res = getSteps(-1);
        return res == Integer.MIN_VALUE ? 0 : res;
    }

    public long[] getStepsDates(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DB_NAME + " WHERE steps >= 0 AND date > 0", null);
        cursor.moveToFirst();

        int t = cursor.getInt(0);

        cursor = db.rawQuery("SELECT * FROM " + DB_NAME, null);
        long[] dates = new long[t];
        int index = 0;

        if(cursor.moveToFirst()){
            do{
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                int steps = cursor.getInt(cursor.getColumnIndex("steps"));
                if(date != -1 && steps >=0){
                    dates[index] = date;
                    index++;
                }

            }while (cursor.moveToNext());
        }

        cursor.close();
        return dates;
    }

    public long[] getSleepingTimeDates(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DB_NAME + " WHERE sleepDuration >= 0 AND date > 0", null);
        cursor.moveToFirst();

        int t = cursor.getInt(0);

        cursor = db.rawQuery("SELECT * FROM " + DB_NAME, null);
        long[] dates = new long[t];
        int index = 0;

        if(cursor.moveToFirst()){
            do{
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                int sleep = cursor.getInt(cursor.getColumnIndex("sleepDuration"));
                if(date != -1 && sleep >=0){
                    dates[index] = date;
                    index++;
                }

            }while (cursor.moveToNext());
        }

        cursor.close();
        return dates;
    }

    public int getSleepingTimeDatesCount(){
        SQLiteDatabase db = getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DB_NAME + " WHERE sleepDuration >= 0 AND date > 0", null);
        cursor.moveToFirst();

        return cursor.getInt(0);
    }

    /**
     * Get the maximum of steps walked in one day
     *
     * @return the maximum number of steps walked in one day
     */
    public int getRecord() {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT MAX(steps) FROM " + DB_NAME + " WHERE steps >= 0 AND date > 0", null);

        cursor.moveToFirst();
        int re = cursor.getInt(0);
        cursor.close();
        return re;
    }

    /**
     * Get the number of steps taken between 'start' and 'end' date
     * <p/>
     * Note that todays entry might have a negative value, so take care of that
     * if 'end' >= Util.getToday()!
     *
     * @param start start date in ms since 1970 (steps for this date included)
     * @param end   end date in ms since 1970 (steps for this date included)
     * @return the number of steps from 'start' to 'end'. Can be < 0 as todays
     * entry might have negative value
     */
    public int getSteps(final long start, final long end) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(steps) FROM " + DB_NAME + " WHERE steps >= 0 AND date >= " + start +
                " AND date <= "+ end, null);
        int res = 0;
        cursor.moveToFirst();
        res = cursor.getInt(0);
        cursor.close();
        return res;
    }

    public int getSleepingTimes(final long start, final long end) {
        SQLiteDatabase db = getReadableDatabase();
        Cursor cursor = db.rawQuery("SELECT SUM(sleepDuration) FROM " + DB_NAME + " WHERE sleepDuration >= 0 AND date >= " + start +
                " AND date <= "+ end, null);
        int res = 0;
        cursor.moveToFirst();
        res = cursor.getInt(0);
        cursor.close();
        return res;
    }

    public boolean isTableEmpty(Cursor cursor) {
        return !(cursor.getCount() > 0);
    }

}
