package com.android.phoneadapter;


public class EventSender {

    public int mFd = -1;
    static {
        System.loadLibrary("event");
    }

    public void openDevice(String device) {
        mFd = open(device);
        Log.d(Log.TAG, "mFd : " + mFd);
    }
    public void sendEvent(String type, String code, String value) {
        sendevent(mFd, type, code, value);
        // Log.d(Log.TAG, type + " " + code + " " + value);
    }
    public void closeDevice() {
        Log.d(Log.TAG, "mFd : " + mFd);
        close(mFd);
    }
    private static native int open(String device);
    private static native void sendevent(int fd, String type, String code, String value);
    private static native void close(int fd);
}
