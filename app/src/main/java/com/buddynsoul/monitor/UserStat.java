package com.buddynsoul.monitor;

public class UserStat {
    private String date;
    private int steps;
    private int stepGoal;
    private long asleepTime;
    private long wokeUpTime;
    private int deepSleep;
    private int sleepGoal;
    private String morning_location;
    private String night_location;

    public UserStat(String date, int steps, int stepGoal,
                    long asleepTime, long wokeUpTime, int deepSleep,
                    int sleepGoal, String morning_location, String night_location) {
        this.date = date;
        this.steps = steps;
        this.stepGoal = stepGoal;
        this.asleepTime = asleepTime;
        this.wokeUpTime = wokeUpTime;
        this.deepSleep = deepSleep;
        this.sleepGoal = sleepGoal;
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

    public int getStepGoal() {
        return stepGoal;
    }

    public void setStepGoal(int stepGoal) {
        this.stepGoal = stepGoal;
    }

    public long getAsleepTime() {
        return asleepTime;
    }

    public void setAsleepTime(long asleepTime) {
        this.asleepTime = asleepTime;
    }

    public long getWokeUpTime() {
        return wokeUpTime;
    }

    public void setWokeUpTime(long wokeUpTime) {
        this.wokeUpTime = wokeUpTime;
    }

    public int getDeepSleep() {
        return deepSleep;
    }

    public void setDeepSleep(int deepSleep) {
        this.deepSleep = deepSleep;
    }

    public int getSleepGoal() {
        return sleepGoal;
    }

    public void setSleepGoal(int sleepGoal) {
        this.sleepGoal = sleepGoal;
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
