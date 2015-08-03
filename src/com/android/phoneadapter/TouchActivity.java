package com.android.phoneadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.MotionEvent;
import android.view.View;

public class TouchActivity extends Activity {

    private WorkHandler mWorkHandler;
    private static int TID = 5000;

    private DatagramSocket mDatagramSocket;
    private TouchEvent mTouchEvent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TouchView touchView = new TouchView(this);
        touchView.setBackgroundColor(getResources().getColor(
                android.R.color.white));
        setContentView(touchView);
        init();
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
                String serverIp = PreferenceManager.getDefaultSharedPreferences(TouchActivity.this).getString("server_ip", null);
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

    class TouchView extends View {
        public TouchView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int x = (int) event.getRawX();
            int y = (int) event.getRawY();
            int pointerIndex = event.getActionIndex();
            int slot = event.getPointerId(pointerIndex);
            String curX = String.valueOf(event.getX(pointerIndex));
            String curY = String.valueOf(event.getY(pointerIndex));
            Log.d(Log.TAG, "slot : " + slot + " , pointerIndex : " + pointerIndex);
            switch(event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                mTouchEvent.sendEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.sendEvent("3", "57", String.valueOf(TID++));
                mTouchEvent.sendEvent("3", "53", curX);  // x
                mTouchEvent.sendEvent("3", "54", curY);  // y
                mTouchEvent.sendEvent("3", "58", "848"); // pressure
                mTouchEvent.sendEvent("3", "48", "6"); // major
                mTouchEvent.sendEvent("0", "0", "0"); // syc
                break;
            case MotionEvent.ACTION_MOVE:
                mTouchEvent.sendEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.sendEvent("3", "53", curX);  // x
                mTouchEvent.sendEvent("3", "54", curY);  // y
                mTouchEvent.sendEvent("3", "58", "848"); // pressure
                mTouchEvent.sendEvent("0", "0", "0"); // syc
                break;
            case MotionEvent.ACTION_UP:
                mTouchEvent.sendEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.sendEvent("3", "57", String.valueOf(-1));
                mTouchEvent.sendEvent("0", "0", "0"); // syc
                break;
            }
            return true;
        }
    }

    class TouchEvent {
        public void sendEvent(String t, String c, String v) {
            JSONObject jobject = new JSONObject();
            try {
                jobject.put("command", "request_touch");
                jobject.put("device", "");
                jobject.put("type", t);
                jobject.put("code", c);
                jobject.put("value", v);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            String sendData = jobject.toString();
            Message message = Message.obtain();
            message.obj = sendData;
            mWorkHandler.sendMessage(message);
        }
    }
}