package com.example.KGraph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

/**
 * Created by yangj on 2015/2/2.
 */
public class Painter {
    public android.graphics.Canvas Canvas;
    private Paint mPaint;
    private int mWidth,mHeight;

    public Painter(){
        mPaint = new Paint();
    }

    public void fillTopText(){}
    public void paintChart(){}
    public void paintxAxis(){}
    public void fillBottomText(){}
    public void paintVolumn(){}

    public void paintBound(){
        mPaint.setColor(Color.LTGRAY);
        mPaint.setStyle(Paint.Style.STROKE); //设置填充
        Canvas.drawRect(5, 5, mWidth-5, mHeight-5, mPaint); //绘制矩形
        mPaint.setColor(Color.DKGRAY);

        //绘制水平分割
        int h = (mHeight-10)/6;
        for(int i =1;i<6;i++)
        {
            Canvas.drawLine(5,i*h,mWidth-5,i*h,mPaint);
        }

        //绘制垂直分割
        int v = (mWidth-10)/4;
        for(int i=1;i<4;i++){
            Canvas.drawLine(i*v,5,i*v,mHeight-5,mPaint);
        }
    }

    public void paint(){
        mWidth = Canvas.getWidth();
        mHeight = Canvas.getHeight();

        paintBound();
    }
}
