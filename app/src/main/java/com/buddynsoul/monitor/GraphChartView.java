package com.buddynsoul.monitor;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;


public class GraphChartView extends View {
    private final Paint pGray, pRED, pWhite, pPrimary, pBlack;
    private int canvasHeight, canvasWidth;
    private int screenWidth, screenHeight;
    private double scrollPosition;
    private ArrayList<Point> points;
    private ArrayList<MyObject> objects;
    private int graphHeight, barWidth, space;
    private boolean bars;
    private int goal;




    public GraphChartView(Context context, AttributeSet attrs) {
        super(context, attrs);

        points = new ArrayList<Point>();
        objects = new ArrayList<MyObject>();

        bars = true;

        Database db = new Database(context);
        long[] dates = db.getDates();

        for(int i=0; i<dates.length-1; i++)
            objects.add(new MyObject(dates[i], (db.getSteps(dates[i+1]) - db.getSteps(dates[i])) * -1));

        barWidth = 50;
        space = 250;

        pGray = new Paint();
        pGray.setColor(Color.LTGRAY);
        pGray.setTextAlign(Paint.Align.CENTER);
        pGray.setStrokeWidth(5);

        pRED = new Paint();
        pRED.setColor(Color.RED);
        pRED.setTextAlign(Paint.Align.CENTER);
        pRED.setTextSize(50);

        pWhite = new Paint();
        pWhite.setColor(Color.WHITE);
        pWhite.setTextAlign(Paint.Align.CENTER);
        pWhite.setTextSize(50);

        pBlack = new Paint();
        pBlack.setColor(Color.BLACK);
        pBlack.setTextAlign(Paint.Align.CENTER);
        pBlack.setTextSize(50);



        pPrimary = new Paint();
        pPrimary.setColor(Color.parseColor("#3eabb8"));

    }

    @Override
    protected void onDraw(Canvas canvas){
        pGray.setTextAlign(Paint.Align.CENTER);
        int x = screenWidth/2, y = canvasHeight / 2;
//        canvas.drawText("hiii", x, y, pText);

        graphHeight = canvasHeight - (canvasHeight/10) * 3;

//        for(Point p: points) {
//            int pos = (int)Math.round(scrollPosition/300);
//
//            if(pos == points.indexOf(p))
////                canvas.drawCircle(p.x, p.y, 50, pText2);
//                canvas.drawRect(p.x -50, 0, p.x + 50, canvasHeight - (canvasHeight/10) * 2, pRED);
//            else
//
////                canvas.drawCircle(p.x, p.y, 50, pText);
//                canvas.drawRect(p.x -50, 0, p.x + 50, canvasHeight - (canvasHeight/10) * 2, pGray);
//        }

        int index = 0;

        for(MyObject o : objects){
            int tmp = Math.round((float)((o.getData()/goal)*graphHeight));
            if(tmp > graphHeight)
                tmp = graphHeight;

            o.setPoint(screenWidth/2 + index*space, tmp);
            index ++;
        }

        canvas.drawRect(0, canvasHeight - (canvasHeight/10), canvasWidth, canvasHeight, pPrimary);

        if(!objects.isEmpty()){
           if(bars)
               drawBars(canvas);
           else
               drawPoints(canvas);
        }
        else
            canvas.drawText("No data yet", canvasWidth/2, canvasHeight/2, pBlack);

        invalidate();
    }

    private void drawBars(Canvas canvas){
        for(MyObject o: objects) {
            int pos = (int) Math.round(scrollPosition / space);

            if (pos == objects.indexOf(o)) {
                canvas.drawRect(o.getPoint().x - barWidth, (canvasHeight/10) + (graphHeight - o.getPoint().y),
                        o.getPoint().x + barWidth, canvasHeight - (canvasHeight / 10) * 2, pRED);
                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pRED);
                canvas.drawText(Integer.toString((int)o.getData()), o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y) - 5, pBlack);
            }
            else {
                canvas.drawRect(o.getPoint().x - barWidth, (canvasHeight/10) +(graphHeight - o.getPoint().y),
                        o.getPoint().x + barWidth, canvasHeight - (canvasHeight / 10) * 2, pGray);
                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pWhite);
//                canvas.drawText(Integer.toString((int)o.getData()), o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y) - 5, pBlack);
            }

        }
    }

    private void drawPoints(Canvas canvas){
        Point[] points = new Point[objects.size()];

        for(MyObject o : objects){
            points[objects.indexOf(o)] = new Point(o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y));
        }

        for(int i=0; i<points.length; i++){
            if(i != 0)
                canvas.drawLine(points[i-1].x, points[i-1].y, points[i].x, points[i].y, pGray);
        }

        for(int i=0; i<points.length; i++){
            int pos = (int) Math.round(scrollPosition / space);

            if (pos == i) {
                canvas.drawCircle(points[i].x, points[i].y, 25, pRED);
                canvas.drawText(objects.get(i).getDate(), points[i].x, canvasHeight - (canvasHeight / 30), pRED);
                canvas.drawText(Integer.toString((int)objects.get(i).getData()), points[i].x, points[i].y + canvasHeight/10, pBlack);
            }
            else {
                canvas.drawCircle(points[i].x, points[i].y, 25, pGray);
                canvas.drawText(objects.get(i).getDate(), points[i].x, canvasHeight - (canvasHeight / 30), pWhite);
            }
        }

//        for(MyObject o : objects) {
//            int pos = (int) Math.round(scrollPosition / space);
//
//            if (pos == objects.indexOf(o)) {
//                if (objects.indexOf(o) != 0 && objects.indexOf(o) != objects.size())
//                    canvas.drawLine(prev.x, prev.y, o.getPoint().x, (canvasHeight / 10) + (graphHeight - o.getPoint().y), pGray);
//                canvas.drawCircle(o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y), 25, pRED);
//                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pRED);
//                canvas.drawText(Integer.toString((int)o.getData()), o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y) + canvasHeight/10, pBlack);
//            }
//            else {
//                if (objects.indexOf(o) != 0 && objects.indexOf(o) != objects.size())
//                    canvas.drawLine(prev.x, prev.y, o.getPoint().x, (canvasHeight / 10) + (graphHeight - o.getPoint().y), pGray);
//                canvas.drawCircle(o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y), 25, pGray);
//                canvas.drawText(o.getDate(), o.getPoint().x, canvasHeight - (canvasHeight / 30), pWhite);
////                canvas.drawText(Integer.toString((int)o.getData()), o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y) + canvasHeight/10, pBlack);
//            }
//
//            prev.set(o.getPoint().x, (canvasHeight/10) + (graphHeight - o.getPoint().y));
//        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        canvasWidth = w;
        canvasHeight = h;

    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        int height = (screenHeight - 350)/3;
        int width = screenWidth + barWidth*(objects.size()-1) + space * (objects.size()-2);

        if(width < screenWidth)
            width = screenWidth;

        setMeasuredDimension(width, height);
    }

    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    public void setScrollPosition(int position){
        this.scrollPosition = position;
    }

    public void changeGraph(){
        bars = !bars;
    }

    public void setGoal(int goal){
        this.goal = goal;
    }
}
