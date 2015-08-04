package com.android.phoneadapter;

public class EventSender {

    public static int mFd = -1;
    static {
        System.loadLibrary("event");
    }

    public static void openDevice(String device) {
        mFd = open(device);
        Log.d(Log.TAG, "mFd : " + mFd);
    }
    public static void sendEvent(String type, String code, String value) {
        sendevent(mFd, type, code, value);
    }
    public static void closeDevice() {
        Log.d(Log.TAG, "mFd : " + mFd);
        close(mFd);
    }
    private static native int open(String device);
    private static native void sendevent(int fd, String type, String code, String value);
    private static native void close(int fd);
}
