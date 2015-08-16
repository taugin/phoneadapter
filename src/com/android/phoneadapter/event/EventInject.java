package com.android.phoneadapter.event;

import com.android.phoneadapter.Log;

import android.os.RemoteException;
import android.os.ServiceManager;
import android.view.IWindowManager;
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
}
