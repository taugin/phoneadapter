package com.android.phoneadapter.floatview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.Path;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.R;

public class MaskView extends SurfaceView implements SurfaceHolder.Callback,
        Runnable {

    private Paint mPaint;
    private Rect mViewRect;
    private int mPointerX;
    private int mPointerY;
    private boolean mRunning = false;
    private Bitmap mBitmap;
    private Path mPointerPath;

    public MaskView(Context context, WindowManager manager,
            WindowManager.LayoutParams params) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        mPointerPath = new Path();
        mPaint = new Paint();
        // mPaint.setStyle(Style.STROKE);
        mPaint.setColor(Color.RED);
        mViewRect = new Rect();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mViewRect.left = 0;
        mViewRect.top = 0;
        mViewRect.right = displayMetrics.widthPixels;
        mViewRect.bottom = displayMetrics.heightPixels;
    }

    public void updateTouchPosition(int x, int y) {
        mPointerX = x;
        mPointerY = y;
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        // canvas.drawCircle(mPointerX, mPointerY, 5.0f, mPaint);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {
        Log.d(Log.TAG, "holder : " + holder);
        mRunning = true;
        new Thread(this).start();
    }

    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width,
            int height) {
        Log.d(Log.TAG, "holder : " + holder);
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {
        Log.d(Log.TAG, "holder : " + holder);
        mRunning = false;
    }

    private void drawMouseIcon(Canvas canvas) {
        mPointerPath.reset();
        mPointerPath.moveTo(mPointerX, mPointerY);
        mPointerPath.lineTo(mPointerX, mPointerY + 50);
        mPointerPath.lineTo(mPointerX + 30, mPointerY - 30);
        mPointerPath.lineTo(mPointerX, mPointerY);
        canvas.drawPath(mPointerPath, mPaint);
    }

    private void draw() {
        Canvas canvas = getHolder().lockCanvas();
        Log.d(Log.TAG, "canvas : " + canvas);
        if (canvas == null) {
            return ;
        }
        canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
        canvas.drawBitmap(mBitmap, mPointerX, mPointerY, null);
        // canvas.drawCircle(mPointerX, mPointerY, 10, mPaint);
        // drawMouseIcon(canvas);
        if (canvas != null) {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }
    
    public void run() {
        while(mRunning) {
            try {
                draw();
                Thread.sleep(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }
}
