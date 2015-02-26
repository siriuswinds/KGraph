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
    float mlastClose,maxDiff,maxVolumn;
    List<StockDayDeal> Deals;
    int maxDotsCount = 241;
    boolean needPaintAvgPriceLine = true;

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
        if(Deals == null||Deals.size() < 2) return;

        int size = Deals.size();

        Canvas.save();
        Canvas.translate(5,(mHeight-5));

        mPaint.setColor(Color.LTGRAY);
        for(int i = 0;i<Deals.size();i++){
            float x1 = getX(i);
            float x2 = x1;
            float y1 = 0;
            float y2 = getVolumnY(i);
            Canvas.drawLine(x1,y1,x2,y2,mPaint);
        }

        Canvas.restore();
    }

    private float getVolumnY(int i) {
        float result = 0 - Deals.get(i).DealCount * mHeight / 3.0f / this.maxVolumn;
        return result;
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
        float h = (mHeight-10)/6.0f;
        for(int i =1;i<6;i++)
        {
            Canvas.drawLine(5,i*h,mWidth-5,i*h,mPaint);
        }

        //绘制垂直分割
        float v = (mWidth-10)/8.0f;
        for(int i=1;i<8;i++){
            Canvas.drawLine(i*v,5,i*v,mHeight-5,mPaint);
        }
        //http://data.gtimg.cn/flashdata/hushen/minute/sh601988.js?0.5876284902915359
    }

    private float getX(int i){
        return (i+1)*((mWidth-10)/241.0f);
    }

    private float getY(int i){
        float diff = Deals.get(i).Price - mlastClose;
        float result = 0 - diff * mHeight / 3.0f / this.maxDiff;
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
        Canvas.translate(5,(mHeight-5)/3);

        mPaint.setColor(Color.LTGRAY);

        for(int i = 1;i<Deals.size();i++){
            float x1 = getX(i-1);
            float x2 = getX(i);
            float y1 = getY(i-1);
            float y2 = getY(i);
            Canvas.drawLine(x1,y1,x2,y2,mPaint);
        }
        Canvas.restore();
    }

    private void fillTopText(){

    }

    @Override
    public void setData(Object data){
        Deals = (List<StockDayDeal>)data;

        for(int i = 0;i<Deals.size();i++){
            float diff = Math.abs(mlastClose - Deals.get(i).Price)*1.1f;
            maxDiff = Math.max(diff,maxDiff);

            maxVolumn = Math.max(Deals.get(i).DealCount*1.1f,maxVolumn);
        }
    }
}
