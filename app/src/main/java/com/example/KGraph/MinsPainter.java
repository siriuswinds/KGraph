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

        float Mc = 45;
        float Nc = 0;
        float Oc = 45;
        float Pc = mWidth - Oc;
        float Qc = Nc + Mc + 2.5f;
        float Rc = Pc - Qc - 2.5f;
        float Sc = 20;
        float Tc = 3.5f;
        float Uc = Sc - 4.5f;
        float Vc = Sc - 0.5f;
        boolean Wc = true;
        float Xc = 5;
        if (!Wc) {
            Vc = Xc;
        }
        float Yc = (float)Math.ceil((mHeight - Vc - Sc * 2.0f) * 2.0f / 3.0f);
        float Zc = (float)Math.floor(Yc / 80) * 2 + 1;
        float $c = Vc + Yc + 3;
        float ad = Sc - 3;
        float bd = Vc + Yc + Sc;
        float cd = (float)Math.ceil(mHeight - bd - Sc);
        float dd = bd + cd + 4.5f;
        float ed = Sc - 4.5f;
        boolean fd = false;

        if (!fd) {
            cd =(float)Math.floor(cd + Sc - Xc);
        }

        int gd = Color.rgb(0, 0, 0);
        int hd = Color.rgb(255,61,1);
        int jd = Color.rgb(7,244,7);
        int kd = Color.rgb(255,255,255);
        int ld = Color.rgb(255,255,255);
        int md = Color.rgb(233,233,233);
        int nd = Color.rgb(185,185,0);
        int od = Color.rgb(233,233,233);
        int pd = Color.rgb(192,192,0);
        int qd = Color.rgb(200,34,34);
        int rd = Color.rgb(185,185,0);
        int sd = 241;
        String[] td = new String[]{"09:30", "11:30/13:00", "15:00"};
        float[] ud = new float[]{0f, 0.5f, 1f};
        int vd = td.length - 2;

        topTextRegion = new RectF(Qc,Tc,Rc,Uc);
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
        return (i+1)*((mWidth-10)/241);
    }

    private float getY(int i, List<StockDayDeal> datas){
        float diff = datas.get(i).Price - mlastClose;
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
        if(Data == null) return;

        List<StockDayDeal> deals = (List<StockDayDeal>)Data;

        int size = deals.size();

        Canvas.save();
        Canvas.translate(5,2*(mHeight-10)/6);

        float pb,qb,rb=0,sb=0;

        for(int i = 0;i<deals.size();i++){
            float diff = Math.abs(mlastClose - deals.get(i).Price);
            maxDiff = Math.max(diff,maxDiff);
        }

        float maxRangePrc = Math.max(Math.abs(mlastClose - rb), Math.abs(mlastClose - sb));

        mPaint.setColor(Color.LTGRAY);

        for(int i = 1;i<deals.size();i++){
            float x1 = getX(i-1);
            float x2 = getX(i);
            float y1 = getY(i-1,deals);
            float y2 = getY(i,deals);
            Canvas.drawLine(x1,y1,x2,y2,mPaint);
        }
    }

    private void fillTopText(){

    }
}
