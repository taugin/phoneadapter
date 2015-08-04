package com.android.phoneadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONArray;
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

    @SuppressLint("ClickableViewAccessibility")
    class TouchView extends View {
        @SuppressLint("UseSparseArrays")
        public TouchView(Context context) {
            super(context);
        }

        @Override
        public boolean onTouchEvent(MotionEvent event) {
            int pointerIndex = event.getActionIndex();
            int slot = event.getPointerId(pointerIndex);
            String curX = String.valueOf(event.getX(pointerIndex));
            String curY = String.valueOf(event.getY(pointerIndex));
            switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                Log.d(Log.TAG, "ACTION_DOWN -> slot : " + slot + " , pointerIndex : " + pointerIndex);
                mTouchEvent.addTouchEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.addTouchEvent("3", "57", String.valueOf(TID++));
                mTouchEvent.addTouchEvent("3", "53", curX);  // x
                mTouchEvent.addTouchEvent("3", "54", curY);  // y
                mTouchEvent.addTouchEvent("3", "58", "848"); // pressure
                mTouchEvent.addTouchEvent("3", "48", "6"); // major
                mTouchEvent.addTouchEvent("0", "0", "0"); // syc
                mTouchEvent.sendEvent();
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(Log.TAG, "ACTION_POINTER_DOWN -> slot : " + slot + " , pointerIndex : " + pointerIndex + " , pointerCount : " + event.getPointerCount());
                mTouchEvent.addTouchEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.addTouchEvent("3", "57", String.valueOf(TID++));
                mTouchEvent.addTouchEvent("3", "53", curX);  // x
                mTouchEvent.addTouchEvent("3", "54", curY);  // y
                mTouchEvent.addTouchEvent("3", "58", "848"); // pressure
                mTouchEvent.addTouchEvent("3", "48", "6"); // major
                mTouchEvent.addTouchEvent("0", "0", "0"); // syc
                mTouchEvent.sendEvent();
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(Log.TAG, "ACTION_POINTER_UP -> slot : " + slot + " , pointerIndex : " + pointerIndex);
                mTouchEvent.addTouchEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.addTouchEvent("3", "57", String.valueOf(-1));
                mTouchEvent.addTouchEvent("0", "0", "0"); // syc
                mTouchEvent.sendEvent();
                break;
            case MotionEvent.ACTION_UP:
                Log.d(Log.TAG, "ACTION_UP -> slot : " + slot + " , pointerIndex : " + pointerIndex);
                mTouchEvent.addTouchEvent("3", "47", String.valueOf(slot)); // index
                mTouchEvent.addTouchEvent("3", "57", String.valueOf(-1));
                mTouchEvent.addTouchEvent("0", "0", "0"); // syc
                mTouchEvent.sendEvent();
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mTouchEvent.addTouchEvent("3", "47", String.valueOf(event.getPointerId(i))); // index
                    curX = String.valueOf(event.getX(i));
                    mTouchEvent.addTouchEvent("3", "53", curX);  // x
                    curY = String.valueOf(event.getY(i));
                    mTouchEvent.addTouchEvent("3", "54", curY);  // y
                    mTouchEvent.addTouchEvent("3", "58", "848"); // pressure
                    mTouchEvent.addTouchEvent("0", "0", "0"); // syc
                }
                mTouchEvent.sendEvent();
                break;
            }
            return true;
        }
    }

    class TouchEvent {
        private JSONArray mEventArray;
        public void addTouchEvent(String t, String c, String v) {
            if (mEventArray == null) {
                mEventArray = new JSONArray();
            }
            JSONObject jobject = new JSONObject();
            try {
                jobject.put("device", "");
                jobject.put("type", t);
                jobject.put("code", c);
                jobject.put("value", v);
                mEventArray.put(jobject);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
        public void sendEvent() {
            JSONObject jobject = new JSONObject();
            try {
                jobject.put("cmd", "touch");
                jobject.put("touch", mEventArray);
            } catch (JSONException e) {
                Log.d(Log.TAG, "error : " + e);
            }
            String sendData = jobject.toString();
            Message message = Message.obtain();
            message.obj = sendData;
            Log.d(Log.TAG, "sendData : " + sendData);
            mWorkHandler.sendMessage(message);
            mEventArray = null;
        }
    }
}