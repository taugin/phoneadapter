package com.android.phoneadapter;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.view.MotionEvent;
import android.view.View;

import com.android.phoneadapter.floatview.FloatService;

public class MainActivity extends Activity {

    private boolean running = false;
    private WorkHandler mWorkHandler;
    private static int TID = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TouchView touchView = new TouchView(this);
        touchView.setBackgroundColor(getResources().getColor(
                android.R.color.transparent));
        setContentView(touchView);
        startService(new Intent(this, FloatService.class));
        // finish();
        // init();
    }

    private void init() {
        HandlerThread handlerThread = new HandlerThread("handler");
        handlerThread.start();
        mWorkHandler = new WorkHandler(handlerThread.getLooper());
    }

    class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            Log.d(Log.TAG, "msg : " + msg);
        }
    }

    class TouchView extends View {
        private boolean pressed = false;
        public TouchView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                pressed = true;
                TouchEvent.sendEvent("", "3", "47", String.valueOf(TID++));
                TouchEvent.sendEvent("", "3", "47", "0");
                TouchEvent.sendEvent("", "3", "47", "0");
                TouchEvent.sendEvent("", "3", "47", "0");
                TouchEvent.sendEvent("", "3", "47", "0");
                TouchEvent.sendEvent("", "3", "47", "0");
                break;
            case MotionEvent.ACTION_MOVE:
                break;
            case MotionEvent.ACTION_UP:
                pressed = false;
                break;
            }
            return true;
        }
    }

    static class TouchEvent {
        public static void sendEvent(String d, String t, String c, String v) {
        }
    }
}