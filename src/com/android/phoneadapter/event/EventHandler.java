package com.android.phoneadapter.event;

import android.annotation.SuppressLint;
import android.hardware.input.InputManager;
import android.os.SystemClock;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.KeyEvent;
import android.view.MotionEvent;

import com.android.phoneadapter.EventSender;
import com.android.phoneadapter.Log;
import com.android.phoneadapter.floatview.FloatService;
import com.android.phoneadapter.utils.Utils;


public class EventHandler {
    private static boolean useInject = false;
    public static int TRACKID = 1;

    public static final String EV_TOUCH_DEVICE = "/dev/input/event0";
    public static final String EV_KEY_DEVICE = "/dev/input/event1";

    public static final String ABS_MT_SLOT = Integer.toString(0x2f);
    public static final String ABS_MT_POSITION_X = Integer.toString(0x35);
    public static final String ABS_MT_POSITION_Y = Integer.toString(0x36);
    
    public static final String ABS_MT_TRACKING_ID = Integer.toString(0x39);
    public static final String ABS_MT_PRESSURE = Integer.toString(0x3a);
    public static final String ABS_MT_TOUCH_MAJOR = Integer.toString(0x30);
    
    public static final String EV_ABS = Integer.toString(0x3);
    public static final String EV_KEY = Integer.toString(0x1);
    public static final String KEY_BACK = Integer.toString(0x9e);
    public static final String KEY_HOMEPAGE = Integer.toString(0xac);
    public static final String KEY_MENU = Integer.toString(0x8b);
    public static final String KEY_VOLUME_DOWN = Integer.toString(0x72);
    public static final String KEY_VOLUME_UP = Integer.toString(0x73);
    
    public static final String DOWN = Integer.toString(0x1);
    public static final String UP = Integer.toString(0x0);
    
    public static final String EV_SYN = Integer.toString(0x0);
    public static final String SYN_REPORT = Integer.toString(0x0);
    
    public static final String BTN_TOUCH = Integer.toString(0x14a);

    public static void print() {
        Log.d(Log.TAG, " : " + ABS_MT_SLOT);
    }

    private FloatService mFloatService;
    private int mWidth = 0;
    private int mHeight = 0;
    private EventSender mTouchSender;
    private EventSender mKeySender;
    private int[] mPos = new int[2];

    public EventHandler(FloatService service) {
        Log.d(Log.TAG, "");
        mFloatService = service;
        mWidth = Utils.getDisplayWidth(mFloatService);
        mHeight = Utils.getDisplayHeight(mFloatService);
    }

    public void updateScreen() {
        mWidth = Utils.getDisplayWidth(mFloatService);
        mHeight = Utils.getDisplayHeight(mFloatService);
    }

    
    public boolean openTouchDevice() {
        String touchDevice = PreferenceManager.getDefaultSharedPreferences(mFloatService).getString("touch_device", EV_TOUCH_DEVICE);
        String keyDevice = PreferenceManager.getDefaultSharedPreferences(mFloatService).getString("key_device", EV_TOUCH_DEVICE);
        if (TextUtils.isEmpty(touchDevice) || TextUtils.isEmpty(keyDevice)) {
            return false;
        }
        if (touchDevice.equalsIgnoreCase(keyDevice)) {
            mTouchSender = new EventSender();
            mTouchSender.openDevice(touchDevice);
            mKeySender = mTouchSender;
        } else {
            mTouchSender = new EventSender();
            mTouchSender.openDevice(touchDevice);
            mKeySender = new EventSender();
            mKeySender.openDevice(keyDevice);
        }
        return true;
    }

    public void closeTouchDevice() {
        String touchDevice = PreferenceManager.getDefaultSharedPreferences(mFloatService).getString("touch_device", EV_TOUCH_DEVICE);
        String keyDevice = PreferenceManager.getDefaultSharedPreferences(mFloatService).getString("key_device", EV_TOUCH_DEVICE);
        if (TextUtils.isEmpty(touchDevice) || TextUtils.isEmpty(keyDevice)) {
            return;
        }
        if (touchDevice.equalsIgnoreCase(keyDevice)) {
            mTouchSender.closeDevice();
            mTouchSender = null;
            mKeySender = null;
        } else {
            mTouchSender.closeDevice();
            mTouchSender = null;
            mKeySender.closeDevice();
            mKeySender = null;
        }
    }

    private void updatePointer(Motion motion) {
        if (mFloatService != null) {
            int realX = getPointerX(motion);
            int realY = getPointerY(motion);
            mFloatService.updatePointerPosition(motion.pressed == 1, realX, realY);
        }
    }

    private int getPointerX(Motion motion) {
        if (mFloatService.getResources().getConfiguration().orientation == 2) {
            if (motion.source == 0) {
                return motion.y;
            }
            return motion.x;
        }
        return motion.x;
    }
    
    private int getPointerY(Motion motion) {
        if (mFloatService.getResources().getConfiguration().orientation == 2) {
            if (motion.source == 0) {
                return mHeight - motion.x;
            }
            return motion.y;
        }
        return motion.y;
    }

    private int getTouchX(Motion motion) {
        if (mFloatService.getResources().getConfiguration().orientation == 2
                && motion.source != 0) {
            return mHeight - motion.y;
        }
        return motion.x;
    }
    
    private int getTouchY(Motion motion) {
        if (mFloatService.getResources().getConfiguration().orientation == 2
                && motion.source != 0) {
            return motion.x;
        }
        return motion.y;
    }

    private void handleTouchMotion(Motion motion) {
        mFloatService.getPosition(mPos);
        Log.d(Log.TAG, "motion : " + motion.action);
        int realX = mPos[0];
        int realY = mPos[1];
        if (motion.action == 1) {
            // For huawei
            mTouchSender.sendEvent(EV_KEY, BTN_TOUCH, String.valueOf(1));

            mTouchSender.sendEvent(EV_ABS, ABS_MT_SLOT, String.valueOf(motion.slot));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_TRACKING_ID, String.valueOf(TRACKID++));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_POSITION_X, String.valueOf(realX));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_POSITION_Y, String.valueOf(realY));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_PRESSURE, Integer.toString(0x350));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_TOUCH_MAJOR, Integer.toString(0x6));
            mTouchSender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
        } else if (motion.action == 2 && motion.pressed == 1) {
            mTouchSender.sendEvent(EV_ABS, ABS_MT_SLOT, String.valueOf(motion.slot));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_POSITION_X, String.valueOf(realX));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_POSITION_Y, String.valueOf(realY));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_PRESSURE, Integer.toString(0x350));
            mTouchSender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
        } else if (motion.action == 0) {
            mTouchSender.sendEvent(EV_ABS, ABS_MT_SLOT, String.valueOf(motion.slot));
            mTouchSender.sendEvent(EV_ABS, ABS_MT_TRACKING_ID, String.valueOf(-1));
            mTouchSender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));

            // For huawei
            mTouchSender.sendEvent(EV_KEY, BTN_TOUCH, String.valueOf(1));
            mTouchSender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
        }
    }

    private void handleKeyMotion(Motion motion) {
        if (motion.state == 2) {
            if (motion.key == 0) {
                mKeySender.sendEvent(EV_KEY, KEY_BACK, DOWN);
                mKeySender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
                mKeySender.sendEvent(EV_KEY, KEY_BACK, UP);
                mKeySender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
            } else if (motion.key == 1) {
                mKeySender.sendEvent(EV_KEY, KEY_MENU, DOWN);
                mKeySender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
                mKeySender.sendEvent(EV_KEY, KEY_MENU, UP);
                mKeySender.sendEvent(EV_SYN, SYN_REPORT, String.valueOf(0));
            }
        }
    }

    public void processMotion(Motion motion) {
        if (motion != null) {
            if (motion.type == 0) {
                updatePointer(motion);
                if (useInject) {
                    handleTouchMotionViaInject(motion);
                } else {
                    handleTouchMotion(motion);
                }
            } else if (motion.type == 1) {
                if (useInject) {
                    handleKeyMotionViaInject2(motion);
                } else {
                    handleKeyMotion(motion);
                }
            }
        }
    }

    private void handleKeyMotionViaInject2(Motion motion) {
        if (motion.key == 0) {
            EventInject.injectKeyDownUp(KeyEvent.KEYCODE_BACK);
        } else if (motion.key == 1) {
            EventInject.injectKeyDownUp(KeyEvent.KEYCODE_MENU);
        }
    }

    private void handleTouchMotionViaInject(Motion motion) {
        int realX = getTouchX(motion);
        int realY = getTouchY(motion);
        MotionEvent event = null;
        int action = 0;
        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        if (motion.action == 1) {
            action = MotionEvent.ACTION_DOWN;
            event = MotionEvent.obtain(downTime, eventTime, action, realX, realY, 0);
            Log.d(Log.TAG, "event : " + event);
            InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        } else if (motion.action == 2 && motion.pressed == 1) {
            action = MotionEvent.ACTION_MOVE;
            event = MotionEvent.obtain(downTime, eventTime, action, realX, realY, 0);
            Log.d(Log.TAG, "event : " + event);
            InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        } else if (motion.action == 0) {
            action = MotionEvent.ACTION_UP;
            event = MotionEvent.obtain(downTime, eventTime, action, realX, realY, 0);
            Log.d(Log.TAG, "event : " + event);
            InputManager.getInstance().injectInputEvent(event, InputManager.INJECT_INPUT_EVENT_MODE_ASYNC);
        }
        if (event != null) {
            event.recycle();
        }
    }
}
