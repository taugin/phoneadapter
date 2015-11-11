package com.android.phoneadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.FrameLayout;

public class TouchActivity2 extends Activity implements OnClickListener {

    private WorkHandler mWorkHandler;
    private static int TID = 5000;

    private DatagramSocket mDatagramSocket;
    private TouchEvent mTouchEvent;
    private int mLastX;
    private int mLastY;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.touch_layout2);
        FrameLayout frameLayout = (FrameLayout) findViewById(R.id.touch_layout);
        TouchView touchView = new TouchView(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(-1, -1);
        frameLayout.addView(touchView, params);
        findViewById(R.id.exit).setOnClickListener(this);
        init();
    }

    public void onClick(View v) {
        if (v.getId() == R.id.exit) {
            finish();
        }
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(Log.TAG, "keyCode : " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mTouchEvent.sendEvent(1, 0, 0, 0, 0, 0, 0, 2, 0);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mTouchEvent.sendEvent(1, 0, 0, 0, 0, 0, 1, 2, 0);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN || keyCode == KeyEvent.KEYCODE_VOLUME_UP) {
            finish();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mWorkHandler != null && mWorkHandler.getLooper() != null) {
            mWorkHandler.getLooper().quit();
        }
    }


    private void init() {
        try {
            mDatagramSocket = new DatagramSocket(8990);
        } catch (SocketException e) {
            e.printStackTrace();
        }
        mTouchEvent = new TouchEvent();
        HandlerThread handlerThread = new HandlerThread("handler");
        handlerThread.start();
        mWorkHandler = new WorkHandler(handlerThread.getLooper());
    }

    class WorkHandler extends Handler {
        public WorkHandler(Looper looper) {
            super(looper);
        }
        public void handleMessage(Message msg) {
            String sendData = (String) msg.obj;
            try {
                String serverIp = PreferenceManager.getDefaultSharedPreferences(TouchActivity2.this).getString("server_ip", null);
                // Log.d(Log.TAG, "serverIp : " + serverIp);
                if (TextUtils.isEmpty(serverIp)) {
                    return;
                }
                InetAddress serverAddress = InetAddress.getByName(serverIp);
                DatagramPacket packet = new DatagramPacket(sendData.getBytes(), sendData.getBytes().length, serverAddress, 8990);
                if (mDatagramSocket != null) {
                    mDatagramSocket.send(packet);
                }
            } catch (UnknownHostException e) {
                Log.d(Log.TAG, "error : " + e);
            } catch (IOException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
    }

    @SuppressLint("ClickableViewAccessibility")
    class TouchView extends View {
        @SuppressLint("UseSparseArrays")
        public TouchView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mLastX = (int) event.getX();
                mLastY = (int) event.getY();
                mTouchEvent.sendEvent(0, 0, 0, 1, 0, 0, 0, 0);
                break;
            case MotionEvent.ACTION_MOVE:
                int x = (int) event.getX();
                int y = (int) event.getY();
                int dx = x - mLastX;
                int dy = y - mLastY;
                mLastX = x;
                mLastY = y;
                mTouchEvent.sendEvent(0, event.getPointerId(0), 1, 2, dx, dy, 0, 0);
                break;
            case MotionEvent.ACTION_UP:
                mTouchEvent.sendEvent(0, 0, 0, 0, 0, 0, 0, 0);
                break;
            }
            return true;
        }
    }

    class TouchEvent {
        public void sendEvent(int type, int slot, int pressed, int action, int x, int y, int key, int state) {
            sendEvent(type, slot, pressed, action, x, y, key, state, 2);
        }
        public void sendEvent(int type, int slot, int pressed, int action, int x, int y, int key, int state, int source) {
            JSONObject jobject = new JSONObject();
            try {
                jobject.put("type", type);
                jobject.put("slot", slot);
                jobject.put("pressed", pressed);
                jobject.put("action", action);
                jobject.put("dx", x);
                jobject.put("dy", y);
                jobject.put("key", key);
                jobject.put("state", state);
                jobject.put("source", source);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            String sendData = jobject.toString();
            Message message = Message.obtain();
            message.obj = sendData;
            Log.d(Log.TAG, "sendData : " + sendData);
            mWorkHandler.sendMessage(message);
        }
    }
}