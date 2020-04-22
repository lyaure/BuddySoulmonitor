package com.buddynsoul.monitor;

import android.graphics.Point;

import java.util.Calendar;

public class MyObject {
    private long date;
    private double data;
    private Point point;

    public MyObject(long date, double data){
        this.date = date;
        this.data = data;
        this.point = new Point();
    }

    public void setPoint(int x, int y) {
        this.point.set(x, y);
    }

    public String getDate() {
        Calendar c = Calendar.getInstance();
        c.setTimeInMillis(this.date);

        String d = c.get(Calendar.DATE) + "/" + c.get(Calendar.MONTH);
        return d;
    }

    public double getData(){
        return this.data;
    }

    public Point getPoint() {
        return this.point;
    }
}
