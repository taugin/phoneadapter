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
import com.android.phoneadapter.floatview.PointerView.OnLongPressListener;
import com.android.phoneadapter.floatview.PointerView.OnPressListener;
import com.android.phoneadapter.socket.EventSocket;

public class FloatService extends Service {
    private static final String TAG = "FloatService";

    private WindowManager mWindowManager = null;

    private PointerView mPointerView = null;
    private EventSocket mEventSocket;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        createPointerView();

        Log.d(Log.TAG, "");
        mEventSocket = new EventSocket(this, mPointerView);
        mEventSocket.listenOn();
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
        mWindowManager.removeView(mPointerView);
    }

    private WindowManager.LayoutParams getLayoutParams(boolean full) {
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

        DisplayMetrics dm = new DisplayMetrics();
        dm = getResources().getDisplayMetrics();
        if (full) {
            params.width = dm.widthPixels;
            params.height = dm.heightPixels;
        } else {
            params.width = LayoutParams.WRAP_CONTENT;
            params.height = LayoutParams.WRAP_CONTENT;
        }
        return params;
    }

    private void createPointerView() {
        PointerView floatView = null;
        WindowManager.LayoutParams params = getLayoutParams(false);
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
        mPointerView = floatView;
        mWindowManager.addView(mPointerView, params);
    }

    @SuppressLint("DefaultLocale") @Override
    public void onConfigurationChanged(Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        Log.d(Log.TAG, "newConfig : " + newConfig.orientation + " , mEventSocket : " + mEventSocket);
        if (mEventSocket != null) {
            DisplayMetrics metrics = getResources().getDisplayMetrics();
            JSONObject object = new JSONObject();
            try {
                object.put("command", "response_screensize");
                object.put("w", metrics.widthPixels);
                object.put("h", metrics.heightPixels);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            mEventSocket.sendData(object.toString());
        }
    }
}
