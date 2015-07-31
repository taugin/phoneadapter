package com.android.phoneadapter.floatview;

import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.os.Handler;
import android.os.Message;
import android.view.MotionEvent;
import android.view.ViewConfiguration;
import android.view.WindowManager;
import android.widget.ImageView;

import com.android.phoneadapter.Log;

public class PointerView extends ImageView {
    private static final String TAG = "FloatView";
    private static final int MODE_NOTHING = -1;
    private static final int MODE_SHORT_PRESS = 0;
    private static final int MODE_LONG_PRESS = 1;
    private static final int MODE_DRAGGING = 2;
    
    private OnPressListener mOnPressListener;
    private OnLongPressListener mOnLongPressListener;
    private int mDeltaX;
    private int mDeltaY;
    private int mPreX;
    private int mPreY;
    private int mDownX;
    private int mDownY;
    private int mTouchSlop;
    private WindowManager mWindowManager = null;
    private WindowManager.LayoutParams mLayoutParams = null;
    private int mMode = MODE_NOTHING;
    private PrivateHandler mHandler;
    private static int LONG_PRESS_TIME_OUT = 0;

    // 此wmParams为获取的全局变量，用以保存悬浮窗口的属性

    public PointerView(Context context, WindowManager manager,
            WindowManager.LayoutParams params) {
        super(context);
        mWindowManager = manager;
        mLayoutParams = params;
        mTouchSlop = ViewConfiguration.get(context).getScaledTouchSlop();
        LONG_PRESS_TIME_OUT = ViewConfiguration.getLongPressTimeout();
        mHandler = new PrivateHandler(context);
        log("mTouchSlop = " + mTouchSlop);
    }

    
    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {

        // 获取相对屏幕的坐标，即以屏幕左上角为原点
        Rect outRect = new Rect();
//        getWindowVisibleDisplayFrame(outRect);
//        int statusHeight = outRect.top;
        int rawX;
        int rawY;
        switch (event.getAction()) {
        case MotionEvent.ACTION_DOWN:
            // 获取相对View的坐标，即以此View左上角为原点
            rawX = (int) event.getRawX();
            rawY = (int) event.getRawY();
            mDownX = rawX;
            mDownY = rawY;
            mPreX = rawX;
            mPreY = rawY;
            mMode = MODE_SHORT_PRESS;
            mHandler.sendEmptyMessageDelayed(MODE_LONG_PRESS, LONG_PRESS_TIME_OUT);
            break;
        case MotionEvent.ACTION_MOVE:
            rawX = (int) event.getRawX();
            rawY = (int) event.getRawY();
            //log("rawY = " + rawY);
            //log("scrollDeltaX = " + (rawX - mDownX) + " , scrollDeltaY = " + (rawY - mDownY));
            mDeltaX = rawX - mPreX;
            mDeltaY = rawY - mPreY;
            if(Math.abs(rawX - mDownX) >= mTouchSlop || Math.abs(rawY - mDownY) >= mTouchSlop){
                if(mMode != MODE_LONG_PRESS){
                    mMode = MODE_DRAGGING;
                    if(mHandler.hasMessages(MODE_LONG_PRESS)){
                        mHandler.removeMessages(MODE_LONG_PRESS);
                    }
                    updateViewPosition();
                }
            }
            mPreX = rawX;
            mPreY = rawY;
            break;

        case MotionEvent.ACTION_UP:
            if(mMode != MODE_DRAGGING){
                if(mHandler.hasMessages(MODE_LONG_PRESS)){
                    mHandler.removeMessages(MODE_LONG_PRESS);
                    if(mMode == MODE_SHORT_PRESS){
                        log("short press mode");
                        if(mOnPressListener != null){
                            mOnPressListener.onShortPress();
                        }
                    }
                }
            }
            break;
        }
        return true;
    }

    private void updateViewPosition() {
        // 更新浮动窗口位置参数
        mLayoutParams.x = mLayoutParams.x + mDeltaX;
        mLayoutParams.y = mLayoutParams.y + mDeltaY;
        // log("x = " + mLayoutParams.x + " , y = " + mLayoutParams.y);
        mWindowManager.updateViewLayout(this, mLayoutParams);
    }

    public void updatePositionFromOuter(int curX, int curY) {
        mLayoutParams.x = curX;
        mLayoutParams.y = curY;
        mHandler.post(new Runnable() {
            @Override
            public void run() {
                try {
                    mWindowManager.updateViewLayout(PointerView.this, mLayoutParams);
                } catch(IllegalArgumentException e) {
                    mWindowManager.addView(PointerView.this, mLayoutParams);
                }
            }
        });
    }

    class PrivateHandler extends Handler{
        private Context mContext;
        public PrivateHandler(Context context){
            mContext = context;
        }
        @Override
        public void handleMessage(Message msg) {
            int what = msg.what;
            switch(what){
            case MODE_LONG_PRESS:
                mMode = MODE_LONG_PRESS;
                log("mode long press ");
                if(mOnLongPressListener != null){
                    mOnLongPressListener.onLongPress();
                }
                break;
            default:
                break;
            }
        }
        
    }
    private void log(String out) {
        Log.d(TAG, out);
    }
    
    public void setOnPressListener(OnPressListener listener){
        mOnPressListener = listener;
    }
    
    public void setOnLongPressListener(OnLongPressListener listener){
        mOnLongPressListener = listener;
    }

    interface OnPressListener{
        public void onShortPress();
    }
    interface OnLongPressListener{
        public void onLongPress();
    }
}
