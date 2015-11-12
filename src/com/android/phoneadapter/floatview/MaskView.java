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
import android.graphics.Path;
import android.graphics.Rect;
import android.graphics.RectF;
import android.os.Handler;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.R;

public class MaskView extends View {

    private Paint mPaint;
    private Rect mViewRect;
    private int mPointerX;
    private int mPointerY;
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
    private Path mPath;
    private int mCursorHeight;
    private int mPacketHandled = 0;
    private String mShowMsg;

    public MaskView(Context context, WindowManager manager,
            WindowManager.LayoutParams params) {
        super(context);
        mBitmap = BitmapFactory.decodeResource(getResources(), R.drawable.cursor);
        mCursorHeight = mBitmap.getHeight();
        // setBackgroundColor(Color.parseColor("#88FFFF00"));
        mOutRectF = new RectF();
        mHandler = new Handler();
        mSweepRect = new Rect();
        mPath = new Path();

        mPaint = new Paint();
        mPaint.setStyle(Style.STROKE);
        mPaint.setColor(Color.RED);
        mPaint.setAntiAlias(true);
        mPaint.setTextSize(30f);
        mViewRect = new Rect();

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
        mPacketHandled++;
        mPressed = pressed;
        mPointerX = x;
        mPointerY = y;
        if (mPointerX > mViewRect.right) {
            mPointerX = mViewRect.right;
        }
        if (mPointerX < 0) {
            mPointerX = 0;
        }
        if (mPointerY > mViewRect.bottom - mCursorHeight) {
            mPointerY = mViewRect.bottom - mCursorHeight;
        }
        if (mPointerY < 0) {
            mPointerY = 0;
        }
        if (!mSweepRect.contains(mPointerX, mPointerY)) {
            startSweepAnimation();
        }
        postInvalidate();
    }

    public void setShowMsg(String msg) {
        mShowMsg = msg;
    }

    public void getPosition(int []pos) {
        if (pos != null) {
            pos[0] = mPointerX;
            pos[1] = mPointerY;
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
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        Log.d(Log.TAG, "w : " + w  +" , h : " + h);
        getWindowVisibleDisplayFrame(mClientRect);
        mViewRect.left = 0;
        mViewRect.top = 0;
        mViewRect.right = w;
        mViewRect.bottom = h;
        mPointerX = mViewRect.width() / 2;
        mPointerY = mViewRect.height() / 2;
        Log.d(Log.TAG, "mViewRect : " + mViewRect);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        // drawCrossLine(canvas);
        drawHandledPacket(canvas);
        drawPointer(canvas);
        // drawPosition(canvas);
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
        /*
        mPaint.setColorFilter(mPressed ? mColorMatrixColorFilter : null);
        canvas.drawBitmap(mBitmap, mPointerX, mPointerY, mPaint);
        mPaint.setColorFilter(null);
        */
        if (mDrawCircleAnimation && false) {
            drawCircleAnimation(canvas);
        }
        drawPointerView(canvas);
    }

    private void drawPointerView(Canvas canvas) {
        mPath.reset();
        final int x = mPointerX;
        final int y = mPointerY;
        mPath.moveTo(x, y);
        mPath.lineTo(x, y + 13);
        mPath.lineTo(x + 3, y + 10);
        mPath.lineTo(x + 6, y + 16);
        mPath.lineTo(x + 7, y + 14);
        mPath.lineTo(x + 5, y + 9);
        mPath.lineTo(x + 9, y + 9);
        mPath.lineTo(x, y);
        mPaint.setColor(Color.parseColor("#66666666"));
        mPaint.setStyle(Style.STROKE);
        float w =  mPaint.getStrokeWidth();
        mPaint.setStrokeWidth(2);
        canvas.save();
        canvas.clipPath(mPath);
        canvas.drawColor(mPressed ? Color.RED : Color.WHITE);
        canvas.drawPath(mPath, mPaint);
        canvas.restore();
        mPaint.setColor(Color.RED);
        mPaint.setStyle(Style.STROKE);
        mPaint.setStrokeWidth(w);
    }

    private void drawHandledPacket(Canvas canvas) {
        canvas.drawText("HandledPacket : " + mPacketHandled, 10, mViewRect.height() / 2, mPaint);
        if (!TextUtils.isEmpty(mShowMsg)) {
            canvas.drawText(mShowMsg, 10, mViewRect.height() / 2 + 30, mPaint);
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
