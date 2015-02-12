package com.example.KGraph;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;

import java.util.List;

/**
 * Created by yangj on 2015/2/2.
 */
public class Painter {
    protected android.graphics.Canvas Canvas;
    protected Paint mPaint;
    protected int mWidth,mHeight;
    public Object Data;
    public GraphType mGraphType;

    public Painter(){
        mPaint = new Paint();
    }

    public void paint(){

    }

}
