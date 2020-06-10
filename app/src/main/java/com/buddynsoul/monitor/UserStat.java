package com.buddynsoul.monitor;

public class UserStat {
    private String date;
    private int steps;
    private int sleepingTime;
    private String morning_location;
    private String night_location;

    public UserStat(String date, int steps, int sleepingTime, String morning_location, String night_location) {
        this.date = date;
        this.steps = steps;
        this.sleepingTime = sleepingTime;
        this.morning_location = morning_location;
        this.night_location = night_location;
    }

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public int getSteps() {
        return steps;
    }

    public void setSteps(int steps) {
        this.steps = steps;
    }

    public int getSleepingTime() {
        return sleepingTime;
    }

    public void setSleepingTime(int sleepingTime) {
        this.sleepingTime = sleepingTime;
    }

    public String getMorning_location() {
        return morning_location;
    }

    public void setMorning_location(String morning_location) {
        this.morning_location = morning_location;
    }

    public String getNight_location() {
        return night_location;
    }

    public void setNight_location(String night_location) {
        this.night_location = night_location;
    }
}
