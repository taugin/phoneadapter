package com.android.phoneadapter.floatview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.android.phoneadapter.EventSender;
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
    private RectF mOutRectF;
    private boolean mDrawCircleAnimation = false;
    private int sweepDegree = 0;
    private Handler mHandler;

    private Rect mSweepRect;
    private int mSweepLength = 100;

    public MaskView(Context context, WindowManager manager,
            WindowManager.LayoutParams params) {
        super(context);
        getHolder().addCallback(this);
        getHolder().setFormat(PixelFormat.TRANSPARENT);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        mOutRectF = new RectF();
        mHandler = new Handler();
        mSweepRect = new Rect();

        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mViewRect = new Rect();
        DisplayMetrics displayMetrics = getResources().getDisplayMetrics();
        mViewRect.left = 0;
        mViewRect.top = 0;
        mViewRect.right = displayMetrics.widthPixels;
        mViewRect.bottom = displayMetrics.heightPixels;

        mPointerX = mViewRect.width() / 2;
        mPointerY = mViewRect.height() / 2;
    }

    public void updateTouchPosition(int x, int y) {
        mPointerX = x;
        mPointerY = y;
        if (!mSweepRect.contains(mPointerX, mPointerY)) {
            startSweepAnimation();
        }
    }

    private void startSweepAnimation() {
        sweepDegree = 0;
        mDrawCircleAnimation = false;
        mHandler.removeCallbacks(mSweepRunnable);
        mHandler.postDelayed(mSweepRunnable, 1000);
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

    private void setOutRect() {
        if (mBitmap != null) {
            int w = mBitmap.getWidth();
            int h = mBitmap.getHeight();
            int radius = w > h ? w : h;
            int cx = mPointerX + w / 2;
            int cy = mPointerY + h / 2;
            mOutRectF.left = cx - radius;
            mOutRectF.top = cy - radius;
            mOutRectF.right = cx + radius;
            mOutRectF.bottom = cy + radius;
        }
    }

    private void sweepAnimationEnd() {
        sweepDegree = 0;
        mDrawCircleAnimation = false;
        mSweepRect.setEmpty();
    }

    private void drawCircleAnimation(Canvas canvas) {
        setOutRect();
        int color = mPaint.getColor();
        float strokeWidth = mPaint.getStrokeWidth();
        mPaint.setColor(Color.WHITE);
        mPaint.setStrokeWidth(5);
        sweepDegree += 5;
        if (sweepDegree >= 360) {
            sweepAnimationEnd();
        }
        canvas.drawArc(mOutRectF, 0, sweepDegree, false, mPaint);
        mPaint.setColor(color);
        mPaint.setStrokeWidth(strokeWidth);
    }

    private void draw() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return ;
        }
        canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
        canvas.drawBitmap(mBitmap, mPointerX, mPointerY, null);
        if (mDrawCircleAnimation && false) {
            drawCircleAnimation(canvas);
        }
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

    private Runnable mSweepRunnable = new Runnable() {
        @Override
        public void run() {
            mDrawCircleAnimation = true;
            mSweepRect.left = mPointerX - mSweepLength;
            mSweepRect.top = mPointerY - mSweepLength;
            mSweepRect.right =  mPointerX + mSweepLength;
            mSweepRect.bottom =  mPointerY + mSweepLength;
        }
    };
}
