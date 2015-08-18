package com.android.phoneadapter.floatview;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.WindowManager;
import android.view.WindowManager.LayoutParams;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.R;
import com.android.phoneadapter.event.EventHandler;
import com.android.phoneadapter.floatview.PointerView.OnLongPressListener;
import com.android.phoneadapter.floatview.PointerView.OnPressListener;
import com.android.phoneadapter.socket.EventSocket;

public class FloatService extends Service {
    private static final String TAG = "FloatService";

    private WindowManager mWindowManager = null;

    private EventSocket mEventSocket;
    private MaskView mMaskView;
    private EventHandler mEventHandler;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.d(Log.TAG, "");
        createFloatView();
        mEventHandler = new EventHandler(this);
        mEventSocket = new EventSocket(this, mEventHandler);
        mEventSocket.listenOn();
    }

    private void createFloatView() {
        mMaskView = createMaskView();
    }

    private void removeFloatView() {
        if (mMaskView != null) {
            mWindowManager.removeView(mMaskView);
            mMaskView = null;
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) {
            stopSelf(startId);
            startService(new Intent(this, FloatService.class));
            return Service.START_STICKY;
        }
        return Service.START_STICKY;
    }

    @Override
    public void onDestroy() {
        Log.d(Log.TAG, "");
        removeFloatView();
        if (mEventSocket != null) {
            mEventSocket.destroy();
        }
    }

    private WindowManager.LayoutParams getPointerLayoutParams() {
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = LayoutParams.TYPE_SYSTEM_ERROR; // Can be drag to
                                                             // statusbar
        params.format = PixelFormat.RGBA_8888;
        params.alpha = 1.0f;

        params.flags = LayoutParams.FLAG_NOT_TOUCH_MODAL
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        /**
         * type = LayoutParams.TYPE_SYSTEM_ERROR and flags =
         * LayoutParams.FLAG_LAYOUT_IN_SCREEN can be drag to statusbar
         */
        /*
         * mLayoutParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
         * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
         */
        params.gravity = Gravity.LEFT | Gravity.TOP;
        // mLayoutParams.gravity = Gravity.RIGHT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        DisplayMetrics dm = getResources().getDisplayMetrics();
        params.width = LayoutParams.WRAP_CONTENT;
        params.height = LayoutParams.WRAP_CONTENT;
        return params;
    }

    private PointerView createPointerView() {
        Log.d(Log.TAG, "");
        PointerView floatView = null;
        WindowManager.LayoutParams params = getPointerLayoutParams();
        floatView = new PointerView(getApplicationContext(), mWindowManager,
                params);
        floatView.setImageResource(R.drawable.cursor);
        // floatView.setBackgroundColor(Color.RED);

        floatView.setOnPressListener(new OnPressListener() {

            @Override
            public void onShortPress() {
                Log.d(TAG, "HaHa, onShortPress");
            }
        });
        floatView.setOnLongPressListener(new OnLongPressListener() {

            @Override
            public void onLongPress() {
                Log.d(TAG, "HaHa, onLongPress");
            }
        });
        mWindowManager.addView(floatView, params);
        return floatView;
    }

    private MaskView createMaskView() {
        Log.d(Log.TAG, "");
        MaskView maskView = null;
        WindowManager.LayoutParams params = getMaskLayoutParams();
        maskView = new MaskView(getApplicationContext(), mWindowManager,
                params);
        // maskView.setBackgroundColor(Color.BLACK);

        mWindowManager.addView(maskView, params);
        return maskView;
    }

    private WindowManager.LayoutParams getMaskLayoutParams() {
        mWindowManager = (WindowManager) getSystemService(Service.WINDOW_SERVICE);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams();
        params.type = LayoutParams.TYPE_SYSTEM_ERROR; // Can be drag to
                                                             // statusbar
        // params.format = PixelFormat.RGBA_8888;
        params.format = PixelFormat.TRANSPARENT;
        params.alpha = 1.0f;

        params.flags = LayoutParams.FLAG_FULLSCREEN
                | LayoutParams.FLAG_NOT_FOCUSABLE
                | LayoutParams.FLAG_NOT_TOUCHABLE
                | LayoutParams.FLAG_LAYOUT_IN_SCREEN;

        /**
         * type = LayoutParams.TYPE_SYSTEM_ERROR and flags =
         * LayoutParams.FLAG_LAYOUT_IN_SCREEN can be drag to statusbar
         */
        /*
         * mLayoutParams.flags=LayoutParams.FLAG_NOT_TOUCH_MODAL |
         * LayoutParams.FLAG_NOT_FOCUSABLE | LayoutParams.FLAG_NOT_TOUCHABLE;
         */
        params.gravity = Gravity.LEFT | Gravity.TOP;
        params.x = 0;
        params.y = 0;

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        params.width = dm.widthPixels;
        params.height = dm.heightPixels;
        return params;
    }

    @SuppressLint("DefaultLocale") 
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        removeFloatView();
        createFloatView();
        Log.d(Log.TAG, "newConfig : " + newConfig.orientation + " , mEventSocket : " + mEventSocket);
        if (mEventSocket != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            JSONObject object = new JSONObject();
            try {
                object.put("cmd", "response_screensize");
                object.put("w", metrics.widthPixels);
                object.put("h", metrics.heightPixels);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            mEventSocket.sendData(object.toString());
        }
        if (mEventHandler != null) {
            mEventHandler.updateScreen();
        }
    }

    public void updatePointerPosition(boolean pressed, int x, int y) {
        if (mMaskView != null) {
            mMaskView.updateTouchPosition(pressed, x, y);
        }
    }
}
