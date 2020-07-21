package com.buddynsoul.monitor.Objects;

public class UserStat {
    private String date;
    private int steps;
    private int stepGoal;
    private long asleepTime;
    private long wokeUpTime;
    private int deepSleep;
    private int sleepGoal;
    private long duration;
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
        this.duration = wokeUpTime - asleepTime;
    }

    public UserStat(int steps, long asleepTime, long wokeUpTime){
        this.steps = steps;
        this.asleepTime = asleepTime;
        this.wokeUpTime = wokeUpTime;
        this.duration = wokeUpTime - asleepTime;

        this.date = null;
        this.stepGoal = 0;
        this.sleepGoal = 0;
        this.morning_location = null;
        this.night_location = null;
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

    public long getAsleepTime() {
        return asleepTime;
    }

    public long getWokeUpTime() {
        return wokeUpTime;
    }

    public int getDeepSleep() {
        return deepSleep;
    }

    public int getSleepGoal() {
        return sleepGoal;
    }

    public String getMorning_location() {
        return morning_location;
    }

    public String getNight_location() {
        return night_location;
    }

    public long getDuration() {
        return this.duration;
    }
}
