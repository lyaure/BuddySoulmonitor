package com.buddynsoul.monitor;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;


public class SleepGraphView extends View {
    private final int PIXELS_FOR_HOUR = 100;
    private final int MILLISECONDS_IN_HOUR = 3600000;
    private int canvasHeight, canvasWidth;
    private int screenWidth, screenHeight, width, height, graphHeight;
    private ArrayList<long[]> data;
    private Paint pWhite, pPrimary;

    public SleepGraphView(Context context, AttributeSet attrs) {
        super(context, attrs);

        pPrimary = new Paint();
        pPrimary.setColor(Color.parseColor("#3eabb8"));

        pWhite = new Paint();
        pWhite.setColor(Color.WHITE);
        pWhite.setTextAlign(Paint.Align.CENTER);
        pWhite.setStrokeWidth(5);

        loadData();
    }


    @Override
    protected void onDraw(Canvas canvas) {
        canvas.drawColor(Color.parseColor("#9ed4d5"));
        canvas.drawRect(0, canvasHeight - (canvasHeight/10), canvasWidth, canvasHeight, pPrimary);

        float sum = 0, space;

        for(int i=0; i<1000; i++){
            canvas.drawCircle(i*100, 200, 15, pPrimary);
        }

//        canvas.drawRect(0, canvasHeight - (canvasHeight/10) - canvasHeight/3, 100, canvasHeight - (canvasHeight/10), pWhite);


        for(int i=0; i<data.size(); i++){
            if(i != 0) {
                sum += getHours(data.get(i)[0] - data.get(i - 1)[1]);
            }
            float tmp = getHours(data.get(i)[1] - data.get(i)[0]);
            canvas.drawRect(sum*PIXELS_FOR_HOUR, canvasHeight - (canvasHeight/10) - canvasHeight/3, sum + tmp*PIXELS_FOR_HOUR, canvasHeight - (canvasHeight/10), pWhite);
//            canvas.drawCircle(0, canvasHeight - (canvasHeight/10), 5, pWhite);
            sum += tmp;
        }
    }


    private float getHours(long period){
        return period / MILLISECONDS_IN_HOUR;
    }

    public void loadData(/*long date*/){
        // TODO ----
        long[][] s = { {1592950800879L, 1592952382532L}, {1592952384096L, 1592979971096L}, {1592979972279L,1592979982897L}, {1592980096590L, 1592980101161L}, {1592980103281L, 1592980119522L}, {1592980139561L, 1592980144923L}, {1592980145456L, 1592980146456L}, {1592980147028L, 1592980150028L}, {1592980150957L, 1592980157957L}, {1592980158272L, 1592980175272L}, {1592980195034L, 1592980199034L}, {1592980199356L, 1592980200480L} };
        data = new ArrayList<>(Arrays.asList(s));
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh){
        canvasWidth = w;
        canvasHeight = h;
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        height = (screenHeight - 350)/3;


        width = (int)Math.ceil(getHours(data.get(data.size() - 1)[1] - data.get(0)[0])) * PIXELS_FOR_HOUR;

        setMeasuredDimension(width, height);
        setSize(screenWidth/30);
    }

    public void setScreenDimensions(int width, int height) {
        this.screenWidth = width;
        this.screenHeight = height;
    }

    private void setSize(int size){
//        pBlack.setTextSize(size);
//        pWhite.setTextSize(size);
//        pRED.setTextSize(size);
//        radius = size/2;
    }
}
