package com.example.KGraph;

import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;

import java.util.List;


/**
 * TODO: document your custom view class.
 */
public class MyGraph extends View {
    private String mText; // TODO: use a default from R.string...
    private int mTextColor = Color.WHITE; // TODO: use a default from R.color...
    private float mTextSize = 20; // TODO: use a default from R.dimen...
    private Drawable mDrawable;
    private Paint mPaint;
    private Painter mPainter;

    public MyGraph(Context context) {
        super(context);
        init(null, 0);
    }

    public MyGraph(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPainter = new Painter();
        mPaint = new Paint();
        init(attrs, 0);
    }

    public MyGraph(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        init(attrs, defStyle);
    }

    private void init(AttributeSet attrs, int defStyle) {
        // Load attributes
        final TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.MyGraph, defStyle, 0);

        mText = a.getString(R.styleable.MyGraph_Text);
        mTextColor = a.getColor(R.styleable.MyGraph_TextColor,mTextColor);
        // Use getDimensionPixelSize or getDimensionPixelOffset when dealing with
        // values that should fall on pixel boundaries.
        mTextSize = a.getDimension(R.styleable.MyGraph_TextSize,mTextSize);

        if (a.hasValue(R.styleable.MyGraph_Drawable)) {
            mDrawable = a.getDrawable(R.styleable.MyGraph_Drawable);
            mDrawable.setCallback(this);
        }

        a.recycle();

        mPaint.setColor(mTextColor);
        mPaint.setTextSize(mTextSize);
        // Update TextPaint and text measurements from attributes
        invalidateTextPaintAndMeasurements();
    }

    private void invalidateTextPaintAndMeasurements() {

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        mPainter.Canvas = canvas;
        mPainter.paint();
    }

    /**
     * Gets the example string attribute value.
     *
     * @return The example string attribute value.
     */
    public String getText() {
        return mText;
    }

    /**
     * Sets the view's example string attribute value. In the example view, this string
     * is the text to draw.
     *
     * @param text The example string attribute value to use.
     */
    public void setText(String text) {
        mText = text;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example color attribute value.
     *
     * @return The example color attribute value.
     */
    public int getTextColor() {
        return mTextColor;
    }

    /**
     * Sets the view's example color attribute value. In the example view, this color
     * is the font color.
     *
     * @param color The example color attribute value to use.
     */
    public void setTextColor(int color) {
        mTextColor = color;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example dimension attribute value.
     *
     * @return The example dimension attribute value.
     */
    public float getTextSize() {
        return mTextSize;
    }

    /**
     * Sets the view's example dimension attribute value. In the example view, this dimension
     * is the font size.
     *
     * @param size The example dimension attribute value to use.
     */
    public void setTextSize(float size) {
        mTextSize = size;
        invalidateTextPaintAndMeasurements();
    }

    /**
     * Gets the example drawable attribute value.
     *
     * @return The example drawable attribute value.
     */
    public Drawable getDrawable() {
        return mDrawable;
    }

    /**
     * Sets the view's example drawable attribute value. In the example view, this drawable is
     * drawn above the text.
     *
     * @param drawable The example drawable attribute value to use.
     */
    public void setDrawable(Drawable drawable) {
        mDrawable = drawable;
    }

    public void initMinuteGraph(float lastClose) {
        mPainter = new MinsPainter(lastClose);
        this.postInvalidate();
    }

    /**
     * 绘制分时图
     * @param mMinuteData
     */
    public void DrawMinuteGraph(List<StockDayDeal> mMinuteData) {
        if(mMinuteData==null||mMinuteData.size()==0)return;

        mPainter.setData(mMinuteData);
        this.postInvalidate();
    }

    /**
     * 绘制K线图
     * @param mKDatas
     */
    public void DrawKGraph(List<StockDay> mKDatas) {
        if(mKDatas == null||mKDatas.size()==0)return;

        mPainter.setData(mKDatas);
        this.postInvalidate();
    }

    public void initKGraph(float mLastClose) {
        mPainter = new KPainter();
    }
}
