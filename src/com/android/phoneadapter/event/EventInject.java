package com.android.phoneadapter.event;

import com.android.phoneadapter.Log;

import android.hardware.input.InputManager;
import android.os.RemoteException;
import android.os.ServiceManager;
import android.os.SystemClock;
import android.view.IWindowManager;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.MotionEvent;

public class EventInject {

    private static final IWindowManager sIWindowManager;
    static {
        sIWindowManager = (IWindowManager.Stub.asInterface(ServiceManager
                .getService("window")));
    }

    public static void sendPointerSync(MotionEvent event) {
        try {
            sIWindowManager.injectPointerEvent(event, true);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void sendKeySync(KeyEvent event) {
        try {
            boolean ret = sIWindowManager.injectKeyEvent(event, true);
            Log.d(Log.TAG, "event : " + event.getKeyCode() + " , ret : " + ret);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    public static void injectKeyDownUp(int keyCode) {
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        KeyEvent eventd = new KeyEvent(downTime, eventTime,
                KeyEvent.ACTION_DOWN, keyCode, 0, 0, 0, 158,
                KeyEvent.FLAG_FROM_SYSTEM, InputDevice.SOURCE_KEYBOARD);
        InputManager.getInstance().injectInputEvent(eventd, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT);

        KeyEvent eventu = new KeyEvent(downTime, eventTime, KeyEvent.ACTION_UP,
                keyCode, 0, 0, 0, 158, KeyEvent.FLAG_FROM_SYSTEM,
                InputDevice.SOURCE_KEYBOARD);
        InputManager.getInstance().injectInputEvent(eventu, InputManager.INJECT_INPUT_EVENT_MODE_WAIT_FOR_RESULT);
    }

    public static void injectPointer() {
    }
}
