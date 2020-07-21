package com.buddynsoul.monitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import com.buddynsoul.monitor.Objects.Database;
import com.buddynsoul.monitor.Objects.GraphObject;

import java.util.ArrayList;

// custom view for steps and sleep graph
public class GraphChartView extends View {
    private final int PEDOMETER = 0, SLEEP = 1, TWELVE_IN_SEC = 43200;
    private final Paint pWhite, pRED, pPrimary, pBlack;
    private int canvasHeight, canvasWidth;
    private int screenWidth, screenHeight;
    private int scrollPosition;
    private int type;
    private ArrayList<GraphObject> objects;
    private int width, height, graphHeight, barWidth, space, radius, position;
    private boolean bars;
    private int goal;
    private Context context;

    public GraphChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        this.context = context;
        objects = new ArrayList<>();

        bars = true;

        barWidth = 50;
        space = 250;

        pRED = new Paint();
        pRED.setColor(Color.RED);
        pRED.setTextAlign(Paint.Align.CENTER);

        pWhite = new Paint();
        pWhite.setColor(Color.WHITE);
        pWhite.setTextAlign(Paint.Align.CENTER);
        pWhite.setStrokeWidth(5);

        pBlack = new Paint();
        pBlack.setColor(Color.BLACK);
        pBlack.setTextAlign(Paint.Align.CENTER);

        pPrimary = new Paint();
        pPrimary.setColor(Color.parseColor("#3eabb8"));
    }

    @Override
    protected void onDraw(Canvas canvas){
        canvas.drawColor(context.getResources().getColor(R.color.colorLightBlue));
        pWhite.setTextAlign(Paint.Align.CENTER);

        graphHeight = canvasHeight - (canvasHeight/10) * 3;

        int index = 0;

        // calculates the coordinates of the object to draw according to data
        for(GraphObject o : objects){
            int tmp = Math.round((((float)o.getData()/goal)*graphHeight));
            if(tmp > graphHeight)
                tmp = graphHeight;

            o.setPoint(screenWidth/2 + index*space, tmp);
            index ++;
        }

        // for bottom dates
        canvas.drawRect(0, canvasHeight - (canvasHeight/10), canvasWidth, canvasHeight, pPrimary);

        // draw bars or points according to boolean bars
        if(!objects.isEmpty()){
           if(bars)
               drawBars(canvas);
           else
               drawPoints(canvas);
        }
        else
            canvas.drawText("No data yet", canvasWidth/2, canvasHeight/2, pBlack);

        // draws top goal/limit points
        for(int i=0; i<width; i++){
            if(i%20 == 0)
                canvas.drawCircle(i, canvasHeight/10, 4, pWhite);
        }

        pWhite.setTextAlign(Paint.Align.RIGHT);
        if(this.type == PEDOMETER)
            canvas.drawText("Goal: " + goal, scrollPosition + screenWidth - 50, canvasHeight / 15, pWhite);
        else
            canvas.drawText("12 hours", scrollPosition + screenWidth - 50, canvasHeight / 15, pWhite);
        pWhite.setTextAlign(Paint.Align.CENTER);

        invalidate();
    }

    public void setType(int type){
        this.type = type;
        loadData();

        // change goal if it's sleeping graph
        if(type == SLEEP)
            this.goal = TWELVE_IN_SEC;
    }

    // load data from SQLite db
    private void loadData(){
        Database db = Database.getInstance(context);
        long[] dates;

        // pedometer graph
        if(this.type == PEDOMETER){
            dates = db.getStepsDates();
            for(int i=0; i<dates.length; i++)
                objects.add(new GraphObject(dates[i], (db.getSteps(dates[i]))));
        }
        else{ // sleep graph
            dates = db.getSleepingTimeDates();
            for(int i=0; i<dates.length; i++)
                objects.add(new GraphObject(dates[i], (db.getSleepDuration(dates[i]))));
        }
    }

    // draws bars
    private void drawBars(Canvas canvas){
        for(GraphObject o: objects) {

            // according to scrollview position - draws in red
            if (position == objects.indexOf(o)) {
                canvas.drawRect(o.getPoint().x - barWidth, (canvasHeight/10) + (graphHeight - o.getPoint().y),
                        o.getPoint().x + barWidth, canvasHeight - (canvasHeight / 10) * 2, pRED);
                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pRED);
                if(type == PEDOMETER)
                    canvas.drawText(Integer.toString((int)o.getData()), o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y) - 5, pBlack);
            }
            else { // draws in white
                canvas.drawRect(o.getPoint().x - barWidth, (canvasHeight/10) +(graphHeight - o.getPoint().y),
                        o.getPoint().x + barWidth, canvasHeight - (canvasHeight / 10) * 2, pWhite);
                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pWhite);
            }
        }
    }

    // draws points and lines
    private void drawPoints(Canvas canvas){
        Point[] points = new Point[objects.size()];

        //to recover last points and draw line between two points
        for(GraphObject o : objects){
            points[objects.indexOf(o)] = new Point(o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y));
        }

        // draws line
        for(int i=0; i<points.length; i++){
            if(i != 0)
                canvas.drawLine(points[i-1].x, points[i-1].y, points[i].x, points[i].y, pWhite);
        }

        // draws points
        for(int i=0; i<points.length; i++){

            // according to scrollview position - draws in red
            if (position == i) {
                canvas.drawCircle(points[i].x, points[i].y, radius, pRED);
                canvas.drawText(objects.get(i).getDate(), points[i].x, canvasHeight - (canvasHeight / 30), pRED);
                if(type == PEDOMETER)
                    canvas.drawText(Integer.toString((int)objects.get(i).getData()), points[i].x, points[i].y + canvasHeight/10, pBlack);
            }
            else {// draws in white
                canvas.drawCircle(points[i].x, points[i].y, radius, pWhite);
                canvas.drawText(objects.get(i).getDate(), points[i].x, canvasHeight - (canvasHeight / 30), pWhite);
            }
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        canvasWidth = w;
        canvasHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        height = (screenHeight - 350)/3;

        if(objects.size() < 2)
            width = screenWidth;
        else
            width = screenWidth + (objects.size() - 1)/2 + (objects.size() - 1)*space;

        if(width < screenWidth)
            width = screenWidth;

        setMeasuredDimension(width, height);
        setSize(screenWidth/30);
    }

    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    private void setSize(int size){
        pBlack.setTextSize(size);
        pWhite.setTextSize(size);
        pRED.setTextSize(size);
        radius = size/2;
    }

    public void setScrollPosition(int position){
        this.scrollPosition = position;
        this.position = (int) Math.round(position / space);

    }

    public long getDatePosition(){
        if(!objects.isEmpty())
        return objects.get(position).getDateInMillis();
        else
            return 0;
    }

    public void changeGraph(){
        bars = !bars;
    }

    public void setGoal(int goal){
        this.goal = goal;
    }
}
