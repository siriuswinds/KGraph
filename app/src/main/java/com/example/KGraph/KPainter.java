package com.example.KGraph;

import android.graphics.Color;
import android.graphics.Paint;
import android.util.Log;

import java.util.List;

/**
 * Created by yangj on 2015/2/27.
 */
public class KPainter extends Painter {
    List<StockDay> mKDatas;
    float maxPrice,minPrice,maxVolumn;
    int maxDotsCount = 40;

    @Override
    public void paint(){
        init();
        paintChart();
        paintVolume();
        paintBound();
    }

    private void paintChart() {
        if(mKDatas == null||mKDatas.size() < 1) return;

        int size = mKDatas.size();

        Canvas.save();
        Canvas.translate(5,5);

        mPaint.setColor(Color.LTGRAY);
        for(int i = 0;i<mKDatas.size();i++){
            StockDay stock = mKDatas.get(i);
            float x1 = getX(i);
            float x2 = x1;
            float low = getY(stock.LOW);
            float high = getY(stock.HIGH);
            float open = getY(stock.TOPEN);
            float close = getY(stock.TCLOSE);
            float width = 8.0f;

            if(stock.TCLOSE > stock.TOPEN){
                mPaint.setColor(Color.RED);
                mPaint.setStyle(Paint.Style.FILL);
                Canvas.drawRect(x1-width/2.0f,close,x1+width/2.0f,open,mPaint);
            }
            //if(stock.TOPEN < stock.LCLOSE) mPaint.setColor(Color.GREEN);
            if(stock.TCLOSE < stock.TOPEN) {
                mPaint.setColor(Color.GREEN);
                mPaint.setStyle(Paint.Style.FILL);
                Canvas.drawRect(x1-width/2.0f,open,x1+width/2.0f,close,mPaint);
            }
            if(stock.TOPEN == stock.TCLOSE) {
                mPaint.setColor(Color.WHITE);
                //mPaint.setStyle(Paint.Style.FILL);
                //Canvas.drawRect(x1-width/2.0f,open,x1+width/2.0f,close,mPaint);
                Canvas.drawLine(x1-width/2.0f,open,x1+width/2.0f,close,mPaint);
            }

            Canvas.drawLine(x1,low,x2,high,mPaint);

        }

        Canvas.restore();
    }

    private float getY(float price) {
        float result = (maxPrice - price) *(((mHeight-10)*5.0f)/(6.0f*(this.maxPrice-this.minPrice)));
        return result;
    }

    private void init() {
        mWidth = Canvas.getWidth();
        mHeight = Canvas.getHeight();
    }

    private void paintBound(){
        mPaint.setColor(Color.rgb(200, 34, 34));
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
        /*
        float v = (mWidth-10)/8.0f;
        for(int i=1;i<8;i++){
            Canvas.drawLine(i*v,5,i*v,mHeight-5,mPaint);
        }
        */
        //http://data.gtimg.cn/flashdata/hushen/minute/sh601988.js?0.5876284902915359
    }

    private void paintVolume(){
        if(mKDatas == null||mKDatas.size() < 1) return;

        int size = mKDatas.size();

        Canvas.save();
        Canvas.translate(5,(mHeight-5));

        mPaint.setColor(Color.LTGRAY);
        for(int i = 0;i<mKDatas.size();i++){
            float x1 = getX(i);
            float x2 = x1;
            float y1 = 0;
            float y2 = getVolumnY(i);
            Canvas.drawLine(x1,y1,x2,y2,mPaint);
        }

        Canvas.restore();
    }

    private float getX(int i){
        return (i+1)*((mWidth-10)/(maxDotsCount*1.0f));
    }

    private float getVolumnY(int i) {
        float result = 0 - mKDatas.get(i).VOTURNOVER * (mHeight-10) / 6.0f / this.maxVolumn;
        return result;
    }

    @Override
    public void setData(Object data){
        mKDatas = (List<StockDay>)data;

        maxPrice = 0;
        minPrice = 0;
        maxVolumn = 0;

        for(int i = 0;i<mKDatas.size();i++){
            maxPrice = Math.max(mKDatas.get(i).TCLOSE*1.03f,maxPrice);

            if(i==0)
                minPrice = mKDatas.get(i).TCLOSE*0.97f;
            else
                minPrice = Math.min(mKDatas.get(i).TCLOSE*0.97f,minPrice);

            maxVolumn = Math.max(mKDatas.get(i).VOTURNOVER*1.03f,maxVolumn);
        }
    }
}
