package com.buddynsoul.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import com.buddynsoul.monitor.Utils.Util;

public class Database {
    private SQLiteDatabase db;
    private final String DB_NAME = "monitor";

    public Database(Context context){
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        String query = "CREATE TABLE IF NOT EXISTS " + DB_NAME + "(date INTEGER, steps INTEGER, sleepingTime INTEGER, " +
                "morning_location STRING, night_location STRING)";
        db.execSQL(query);
    }

    public void insertNewDay(long date, int steps){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(!cursor.moveToFirst() && steps >= 0){
            addToLastEntry(steps);

            ContentValues cv = new ContentValues();
            cv.put("date", date);
            cv.put("steps", -steps);
            cv.put("sleepingTime", 0);
            cv.put("morning_location", "");
            cv.put("night_location", "");

            db.insert(DB_NAME, null, cv);

            cursor.close();

            if (BuildConfig.DEBUG) {
                Log.d("debug","insertDay " + date + " / " + steps);
            }
        }
    }

    public void insertSleepingTime(long date, int sleepingTime){
        Log.d("DebugStepCounter: ", "Db Update Sleeping Time");
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(cursor.moveToFirst()){
            int tmpSleepingTime = cursor.getInt(cursor.getColumnIndex("sleepingTime"));
            if(sleepingTime > tmpSleepingTime){
                ContentValues cv = new ContentValues();
                cv.put("sleepingTime", sleepingTime);
                
                db.update(DB_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
            }
        }

        cursor.close();

        if (BuildConfig.DEBUG) {
            Log.d("debug","insertSleepingTime " + date + " / " + sleepingTime);
        }
    }

    public void insertLocation(long date, String location, String params){
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
        Cursor cursor = db.rawQuery("SELECT SUM(steps) FROM " + DB_NAME + " WHERE steps > 0 AND date > 0 AND date < " + Util.getToday(), null);

        cursor.moveToFirst();
        int res = cursor.getInt(0);
        cursor.close();

        return res;
    }

    public int getSteps(final long date){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = Integer.MIN_VALUE;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("steps"));

        cursor.close();
        return res;
    }

    public double getSleepingTime(final long date){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        int res = 0;

        if(cursor.moveToFirst())
            res = cursor.getInt(cursor.getColumnIndex("sleepingTime"));

        cursor.close();
        return res;
    }

    public double getLocation(final long date, String columnName){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null);

        double res = 0.0;

        if(cursor.moveToFirst())
            res = cursor.getDouble(cursor.getColumnIndex(columnName));

        cursor.close();
        return res;
    }

    public void removeNegativeEntries(){
        db.delete(DB_NAME, "steps < ?", new String[]{"0"});
    }

    public int getDaysWithoutToday(){
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

    public long[] getDates(){
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

    /**
     * Get the maximum of steps walked in one day
     *
     * @return the maximum number of steps walked in one day
     */
    public int getRecord() {
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
        Cursor cursor = db.rawQuery("SELECT SUM(steps) FROM " + DB_NAME + " WHERE steps >= 0 AND date >= " + start +
                " AND date <= "+ end, null);
        int res = 0;
        cursor.moveToFirst();
        res = cursor.getInt(0);
        cursor.close();
        return res;
    }
}
