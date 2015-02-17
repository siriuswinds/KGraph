package com.example.KGraph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.Typeface;

import java.util.List;

/**
 * Created by yangj on 2015/2/9.
 */
public class MinsPainter extends Painter {
    float mlastClose,maxDiff;
    List<StockDayDeal> Deals;
    int backColor = Color.rgb(0,0,0);
    int fallColor = Color.rgb(7,244,7);
    int riseColor = Color.rgb(255,61,1);
    int levelColor = Color.rgb(255,255,255);
    int timeColor = Color.rgb(255,255,255);
    int maxDotsCount = 241;
    boolean needPaintAvgPriceLine = true;
    Typeface topTextfont = Typeface.create("宋体",Typeface.NORMAL);
    int topTextSize = 12;
    int topTextColor = Color.rgb(233,233,233);
    int topTextcodeColor = Color.rgb(185,185,0);
    String topTextBaseLine = "top";
    boolean topTextShow = true;
    RectF topTextRegion;

    public MinsPainter(float lastClose){
        super();
        mlastClose = lastClose;
    }

    @Override
    public void paint(){
        init();
        paintBound();
        fillTopText();
        paintChart();
        paintxAxis();
        fillBottomText();
        paintVolume();
    }

    private void init() {
        mWidth = Canvas.getWidth();
        mHeight = Canvas.getHeight();
    }

    private void paintVolume(){

    }

    private void line(){

    }

    private void DrawHLine(){

    }

    public void DrawVLine(){

    }

    private void paintBound(){
        mPaint.setColor(Color.rgb(200,34,34));
        mPaint.setStyle(Paint.Style.STROKE); //设置填充
        Canvas.drawRect(5, 5, mWidth-5, mHeight-5, mPaint); //绘制矩形
        mPaint.setColor(Color.DKGRAY);

        //绘制水平分割
        int h = (mHeight-10)/4;
        for(int i =1;i<4;i++)
        {
            Canvas.drawLine(5,i*h,mWidth-5,i*h,mPaint);
        }

        //绘制垂直分割
        int v = (mWidth-10)/4;
        for(int i=1;i<4;i++){
            Canvas.drawLine(i*v,5,i*v,mHeight-5,mPaint);
        }
        //http://data.gtimg.cn/flashdata/hushen/minute/sh601988.js?0.5876284902915359
    }

    private float getX(int i){
        return (i+1)*((mWidth-10)/241.0f);
    }

    private float getY(int i){
        float diff = Deals.get(i).Price - mlastClose;
        float result = 0 - diff * mHeight / 2 / this.maxDiff;
        return result;
    }

    private void paintItem(int i,float x,float y) {

    }

    private void fillBottomText(){

    }

    private void paintxAxis(){

    }

    private void paintChart(){
        if(Deals == null||Deals.size() < 2) return;

        int size = Deals.size();

        Canvas.save();
        Canvas.translate(5,(mHeight-10)/2);

        mPaint.setColor(Color.LTGRAY);

        for(int i = 1;i<Deals.size();i++){
            float x1 = getX(i-1);
            float x2 = getX(i);
            float y1 = getY(i-1);
            float y2 = getY(i);
            Canvas.drawLine(x1,y1,x2,y2,mPaint);
        }
    }

    private void fillTopText(){

    }

    @Override
    public void setData(Object data){
        Deals = (List<StockDayDeal>)data;

        for(int i = 0;i<Deals.size();i++){
            float diff = Math.abs(mlastClose - Deals.get(i).Price)*1.1f;
            maxDiff = Math.max(diff,maxDiff);
        }
    }
}
