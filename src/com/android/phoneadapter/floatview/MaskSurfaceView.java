package com.android.phoneadapter.floatview;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorMatrix;
import android.graphics.ColorMatrixColorFilter;
import android.graphics.Paint;
import android.graphics.Paint.Style;
import android.graphics.PixelFormat;
import android.graphics.PorterDuff.Mode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.WindowManager;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.R;
import com.android.phoneadapter.utils.Utils;

public class MaskSurfaceView extends SurfaceView implements SurfaceHolder.Callback,
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
    private Rect mTextRect;
    private Rect mClientRect;

    private Rect mSweepRect;
    private int mSweepLength = 100;
    private ColorMatrixColorFilter mColorMatrixColorFilter;
    private boolean mPressed;
    private int mTextPadding;
    private float mRefreshFrenquency;

    public MaskSurfaceView(Context context, WindowManager manager,
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
        mPaint.setTextSize(30f);
        mViewRect = new Rect();
        mViewRect.left = 0;
        mViewRect.top = 0;
        mViewRect.right = Utils.getDisplayWidth(context);
        mViewRect.bottom = Utils.getDisplayHeight(context);

        mPointerX = mViewRect.width() / 2;
        mPointerY = mViewRect.height() / 2;
        mRefreshFrenquency = manager.getDefaultDisplay().getRefreshRate();
        Log.d(Log.TAG, "mRefreshFrenquency : " + mRefreshFrenquency);

        ColorMatrix cm = new ColorMatrix();
        cm.set(new float[]{
                1, 0, 0, 0, 0,  // Red
                0, 0, 0, 0, 0,  // Green
                0, 0, 0, 0, 0,  // Blue
                0, 0, 0, 1, 0   // Alpha
        });
        mColorMatrixColorFilter = new ColorMatrixColorFilter(cm);
        mTextRect = new Rect();
        mClientRect = new Rect();
        mTextPadding = dp2px(context, 4);
    }

    public void updateTouchPosition(boolean pressed, int x, int y) {
        mPressed = pressed;
        mPointerX = x;
        mPointerY = y;
        if (!mSweepRect.contains(mPointerX, mPointerY)) {
            startSweepAnimation();
        }
    }
    
    public void addPointerEvent(MotionEvent event) {
        
    }

    private void startSweepAnimation() {
        sweepDegree = 0;
        mDrawCircleAnimation = false;
        mHandler.removeCallbacks(mSweepRunnable);
        mHandler.postDelayed(mSweepRunnable, 1000);
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
        getWindowVisibleDisplayFrame(mClientRect);
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

    private void drawPointer(Canvas canvas) {
        mPaint.setColorFilter(mPressed ? mColorMatrixColorFilter : null);
        canvas.drawBitmap(mBitmap, mPointerX, mPointerY, mPaint);
        mPaint.setColorFilter(null);
        if (mDrawCircleAnimation && false) {
            drawCircleAnimation(canvas);
        }
    }

    private void drawPosition(Canvas canvas) {
        int statusBarHeight = mClientRect.top;
        int textSize = (int) mPaint.getTextSize();
        String text = "x : " + mPointerX + " | y : " + mPointerY + " | w : " + mViewRect.width() + " | h : " + mViewRect.height();
        // Log.d(Log.TAG, "mTextRect left : " + mTextRect.left + " , top : " + mTextRect.top + " ,  statusBarHeight : " + statusBarHeight + " , textSize : " + textSize);
        mPaint.getTextBounds(text, 0, text.length(), mTextRect);
        mTextRect.top = statusBarHeight;
        mTextRect.bottom = statusBarHeight + textSize + mTextPadding;
        int color = mPaint.getColor();
        mPaint.setColor(Color.WHITE);
        canvas.drawRect(mTextRect, mPaint);
        mPaint.setColor(color);
        canvas.drawText(text, 0, statusBarHeight + textSize, mPaint);
    }

    private void drawCrossLine(Canvas canvas) {
        canvas.drawLine(0, mViewRect.height() / 2, mViewRect.width(), mViewRect.height() / 2, mPaint);
        canvas.drawLine(mViewRect.width() / 2, 0, mViewRect.width() / 2, mViewRect.height(), mPaint);
    }

    private void draw() {
        Canvas canvas = getHolder().lockCanvas();
        if (canvas == null) {
            return ;
        }
        canvas.drawColor(Color.TRANSPARENT,Mode.CLEAR);
        drawCrossLine(canvas);
        drawPointer(canvas);
        drawPosition(canvas);
        if (canvas != null) {
            getHolder().unlockCanvasAndPost(canvas);
        }
    }

    public void run() {
        int sleepTime = 50;
        sleepTime = Math.round(1000 / mRefreshFrenquency);
        while(mRunning) {
            try {
                draw();
                Thread.sleep(sleepTime);
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

    public int dp2px(Context context, float dp) {
        final float scale = context.getResources().getDisplayMetrics().density;
        return (int) (dp * scale + 0.5f);
    }

    public int px2dp(Context context, float px) {
        final float scale = context.getResources().getDisplayMetrics().density;
        Log.d(Log.TAG, "scale = " + scale);
        return (int) (px / scale + 0.5f);
    }
}
