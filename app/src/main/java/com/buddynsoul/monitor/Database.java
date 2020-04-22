package com.buddynsoul.monitor;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

public class Database {
    private SQLiteDatabase db;
    private final String DB_NAME = "monitor";

    public Database(Context context){
        db = context.openOrCreateDatabase(DB_NAME, Context.MODE_PRIVATE, null);

        String query = "CREATE TABLE IF NOT EXISTS " + DB_NAME + "( date INTEGER, steps INTEGER, sleepingTime DOUBLE," +
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
            cv.put("sleepingTime", 0.0);
            cv.put("morning_location", "");
            cv.put("night_location", "");


            db.insert(DB_NAME, null, cv);

            cursor.close();

            if (BuildConfig.DEBUG) {
                Log.d("debug","insertDay " + date + " / " + steps);
            }
        }
    }

    public void insertSleepingTime(long date, double sleepingTime){
        Log.d("DebugStepCounter: ", "Db Update Sleeping Time");
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " WHERE date = " + date, null );

        if(cursor.moveToFirst()){
            double tmpSleepingTime = cursor.getDouble(cursor.getColumnIndex("sleepingTime"));
            if(sleepingTime > tmpSleepingTime){
                ContentValues cv = new ContentValues();
                cv.put("sleepingTime", sleepingTime);
                db.update(DB_NAME, cv, "date = ?", new String[]{String.valueOf(date)});
            }
        }

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

        if (BuildConfig.DEBUG) {
            Log.d("debug","insertLocation " + date + " / " + location);
        }
    }

    public void addToLastEntry(int steps){
        Cursor cursor = db.rawQuery("SELECT * FROM " + DB_NAME + " ORDER BY date DESC", null );

        if(cursor.moveToFirst()){
            ContentValues cv = new ContentValues();
            cv.put("steps", steps);

            int date = cursor.getInt(cursor.getColumnIndex("date"));

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

        double res = 0.0;

        if(cursor.moveToFirst())
            res = cursor.getDouble(cursor.getColumnIndex("sleepingTime"));

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
        Cursor cursor = db.rawQuery("SELECT COUNT(*) FROM " + DB_NAME + " WHERE steps < 0", null);
        cursor.moveToFirst();

        int t = cursor.getInt(0);

        cursor = db.rawQuery("SELECT * FROM " + DB_NAME, null);
        long[] dates = new long[t];
        int index = 0;

        if(cursor.moveToFirst()){
            do{
                long date = cursor.getLong(cursor.getColumnIndex("date"));
                if(date != -1){
                    dates[index] = date;
                    index++;
                }

            }while (cursor.moveToNext());
        }

        cursor.close();
        return dates;
    }
}
