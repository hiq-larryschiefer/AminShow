package com.hiqes.animshow;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.util.AttributeSet;
import android.view.View;

public class GraphView extends View {
    private static final String TAG = GraphView.class.getName();
    private static final int    AXIS_STEP = 10;

    private float               mXMin = 0.0f;
    private float               mXMax = 1.0f;
    private float               mYMin = -1.5f;
    private float               mYMax = 1.5f;
    private Bitmap              mBitmap;

    public GraphView(Context context) {
        super(context);
    }

    public GraphView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public GraphView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        if (mBitmap == null) {
            mBitmap = Bitmap.createBitmap(getWidth(),
                                          getHeight(),
                                          Bitmap.Config.ARGB_4444);
            resetGraph();
        }

        canvas.drawBitmap(mBitmap, 0.0f, 0.0f, null);
    }

    private void resetGraph() {
        if (mBitmap != null) {
            mBitmap.eraseColor(Color.BLACK);

            //  Draw our Y axis as well as the +/- 1.0f points
            int yHigh = translateY(1.0f);
            int yLow = translateY(-1.0f);
            int yMid = translateY(0.0f);

            for (int x = 0; x < getWidth(); x += AXIS_STEP) {
                mBitmap.setPixel(x, yHigh, Color.LTGRAY);
                mBitmap.setPixel(x, yMid, Color.LTGRAY);
                mBitmap.setPixel(x, yLow, Color.LTGRAY);
            }
        }
    }

    public void clear() {
        resetGraph();
        invalidate();
    }

    private int translateX(float x) {
        int   width = getWidth();
        float tmpX =  (x - mXMin) / (mXMax - mXMin);

        tmpX = tmpX * (float)width;
        int ret = Math.round(tmpX);

        ret = ret < width ? ret : (width - 1);
        return ret;
    }

    private int translateY(float y) {
        int   height = getHeight();
        float tmpY =  (y - mYMin) / (mYMax - mYMin);

        tmpY = tmpY * (float)height;

        //  Our graph is actually flipped with the axis in the middle
        //  or the bottom, so we have to adjust y.
        float centerY = ((0.0f - mYMin) / (mYMax - mYMin) * (float)height);
        int ret = Math.round(centerY + (centerY - tmpY));
        ret = ret < height ? ret : (height - 1);

        return ret;
    }

    public void setXYRange(float xMin, float xMax, float yMin, float yMax) {
        mXMin = xMin;
        mXMax = xMax;
        mYMin = yMin;
        mYMax = yMax;
    }

    public void addPoint(float x, float y) {
        //  Pre-scale these before we invalidate.  Normalize
        //  the passed in value for the range
        int nextX = translateX(x);
        int nextY = translateY(y);

        mBitmap.setPixel(nextX, nextY, Color.GREEN);

        invalidate();
    }
}
