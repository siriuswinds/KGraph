package com.example.KGraph;

import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

/**
 * Created by yangj on 2015/2/9.
 */
public class MinsPainter extends Painter {
    public MinsPainter(){
        super();
    }

    @Override
    public void paint(){
        mWidth = Canvas.getWidth();
        mHeight = Canvas.getHeight();

        fillTopText();
        paintChart();
        paintxAxis();
        fillBottomText();
        paintVolume();
    }

    private void PaintMinuteGraph() {
        paintBound();

        if(Data == null) return;

        List<StockDayDeal> deals = (List<StockDayDeal>)Data;

        int size = deals.size();

        Canvas.save();
        Canvas.translate(5,2*(mHeight-10)/6);

        for(Object obj : deals)
        {
            StockDayDeal deal = (StockDayDeal)obj;
            paintItem(deal);
        }
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
        //http://data.gtimg.cn/flashdata/hushen/minute/sh601988.js?0.5876284902915359
    }

    private float getX(int i){
        return (i+1)*((mWidth-10)/240);
    }

    private float getY(){
        return 0;
    }

    private void paintItem(StockDayDeal deal) {

    }

    private void paintVolume(){

    }

    private void fillBottomText(){

    }

    private void paintxAxis(){

    }

    private void paintChart(){

    }

    private void fillTopText(){

    }
}
