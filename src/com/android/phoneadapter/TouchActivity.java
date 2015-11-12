package com.android.phoneadapter;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.UnknownHostException;

import org.json.JSONException;
import org.json.JSONObject;

import com.android.phoneadapter.TouchActivity2.TouchEvent;
import com.android.phoneadapter.TouchActivity2.WorkHandler;

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

public class TouchActivity extends Activity {

    private WorkHandler mWorkHandler;
    private static int TID = 5000;

    private DatagramSocket mDatagramSocket;
    private TouchEvent mTouchEvent;
    private DatagramPacket mDatagramPacket;
    private InetAddress mInetAddress;

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
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        Log.d(Log.TAG, "keyCode : " + keyCode);
        if (keyCode == KeyEvent.KEYCODE_BACK) {
            mTouchEvent.sendEvent(1, 0, 0, 0, 0, 0, 0, 2);
            return true;
        }
        if (keyCode == KeyEvent.KEYCODE_MENU) {
            mTouchEvent.sendEvent(1, 0, 0, 0, 0, 0, 1, 2);
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
        try {
            if (mDatagramSocket != null) {
                mDatagramSocket.close();
            }
        } catch(Exception e) {
        }
    }


    private void init() {
        try {
            String serverIp = PreferenceManager
                    .getDefaultSharedPreferences(TouchActivity.this)
                    .getString("server_ip", null);
            // Log.d(Log.TAG, "serverIp : " + serverIp);
            if (!TextUtils.isEmpty(serverIp)) {
                mInetAddress = InetAddress.getByName(serverIp);
            }

            mDatagramSocket = new DatagramSocket(8990);
            mDatagramPacket = new DatagramPacket(new byte[512], 512);
            mDatagramPacket.setPort(8990);
            mDatagramPacket.setAddress(mInetAddress);

        } catch (SocketException e) {
            e.printStackTrace();
        } catch (UnknownHostException e) {
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
                mDatagramPacket.setData(sendData.getBytes(), 0, sendData.getBytes().length);
                if (mDatagramSocket != null) {
                    mDatagramSocket.send(mDatagramPacket);
                }
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
            switch(event.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                mTouchEvent.sendEvent(0, slot, 0, 1, (int)event.getX(pointerIndex), (int)event.getY(pointerIndex), 0, 0);
                break;
            case MotionEvent.ACTION_POINTER_DOWN:
                Log.d(Log.TAG, "ACTION_POINTER_DOWN -> slot : " + slot + " , pointerIndex : " + pointerIndex + " , pointerCount : " + event.getPointerCount());
                mTouchEvent.sendEvent(0, slot, 0, 1, (int)event.getX(pointerIndex), (int)event.getY(pointerIndex), 0, 0);
                break;
            case MotionEvent.ACTION_POINTER_UP:
                Log.d(Log.TAG, "ACTION_POINTER_UP -> slot : " + slot + " , pointerIndex : " + pointerIndex);
                mTouchEvent.sendEvent(0, slot, 0, 0, (int)event.getX(pointerIndex), (int)event.getY(pointerIndex), 0, 0);
                break;
            case MotionEvent.ACTION_UP:
                Log.d(Log.TAG, "ACTION_UP -> slot : " + slot + " , pointerIndex : " + pointerIndex);
                mTouchEvent.sendEvent(0, slot, 0, 0, (int)event.getX(pointerIndex), (int)event.getY(pointerIndex), 0, 0);
                break;

            case MotionEvent.ACTION_MOVE:
                for (int i = 0; i < event.getPointerCount(); i++) {
                    mTouchEvent.sendEvent(0, event.getPointerId(i), 1, 2, (int)event.getX(i), (int)event.getY(i), 0, 0);
                }
                break;
            }
            return true;
        }
    }

    class TouchEvent {
        public void sendEvent(int type, int slot, int pressed, int action, int x, int y, int key, int state) {
            JSONObject jobject = new JSONObject();
            try {
                jobject.put("type", type);
                jobject.put("slot", slot);
                jobject.put("pressed", pressed);
                jobject.put("action", action);
                jobject.put("x", x);
                jobject.put("y", y);
                jobject.put("key", key);
                jobject.put("state", state);
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