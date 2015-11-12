package com.android.phoneadapter.socket;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.os.Message;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.event.EventHandler;
import com.android.phoneadapter.event.Motion;
import com.android.phoneadapter.utils.Utils;
import com.google.gson.Gson;

public class EventSocket {

    private static final int MSG_TOUCH_DATA = 0;
    private static final int MSG_SENDOUT_DATA = 1;

    private boolean running = false;
    private OutputStream mOutputStream;
    private Context mContext;
    private ServerSocket mServerSocket;
    private DatagramSocket mDatagramSocket;
    private EventHandler mEventHandler;
    private Gson mGson;
    private PrivateHandler mPrivateHandler;

    public EventSocket(Context context, EventHandler handler) {
        mContext = context;
        mEventHandler = handler;
        mGson = new Gson();
        HandlerThread thread = new HandlerThread("event");
        thread.start();
        mPrivateHandler = new PrivateHandler(thread.getLooper());
    }

    public void listenOn() {
        Log.d(Log.TAG, "running : " + running);
        if (!running) {
            TcpThread tcpThread = new TcpThread();
            tcpThread.start();
            UdpThread udpThread = new UdpThread();
            udpThread.start();
        }
    }

    public void destroy() {
        running = false;
        try {
            if (mDatagramSocket != null) {
                mDatagramSocket.close();
            }
            if (mServerSocket != null) {
                mServerSocket.close();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void processCmdData(String data) {
        // Log.d(Log.TAG, data);
        Packet packet = mGson.fromJson(data, Packet.class);
        if (packet != null) {
            if (Packet.REQUEST_SCREENSIZE.equals(packet.cmd)) {
                JSONObject object = new JSONObject();
                try {
                    object.put("cmd", "response_screensize");
                    object.put("w", Utils.getDisplayWidth(mContext));
                    object.put("h", Utils.getDisplayHeight(mContext));
                } catch (JSONException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
                sendData(object.toString());
                return;
            }

            if (Packet.REQUEST_UDPSERVER.equals(packet.cmd)) {
                JSONObject object = new JSONObject();
                try {
                    object.put("cmd", "response_udpserver");
                    object.put("addr", getLocalHostIp());
                    object.put("port", 8990);
                } catch (JSONException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
                sendData(object.toString());
                return;
            }

            if (Packet.REQUEST_TOUCH.equals(packet.cmd)) {
                Message msg = mPrivateHandler.obtainMessage(MSG_TOUCH_DATA, packet.touch);
                mPrivateHandler.sendMessage(msg);
                return;
            }
        }
    }

    private void passEventToHandler(String data) {
        Motion motion = mGson.fromJson(data, Motion.class);
        mEventHandler.processMotion(motion);
    }

    class TcpThread extends Thread {
        public void run() {
            createTcp();
        }
    }

    class UdpThread extends Thread {
        public void run() {
            createUdp();
        }
    }

    private void createUdp() {
        try {
            if (mDatagramSocket == null) {
                mDatagramSocket = new  DatagramSocket(8990);
                mDatagramSocket.setReuseAddress(true);
            }
            byte data[] = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            running = true;
            String packetData = null;
            mEventHandler.openTouchDevice();
            while(running) {
                mDatagramSocket.receive(packet);
                packetData = new String(packet.getData(), 0, packet.getLength());
                Message msg = mPrivateHandler.obtainMessage(MSG_TOUCH_DATA, packetData);
                mPrivateHandler.sendMessage(msg);
            }
            mDatagramSocket.close();
        } catch (SocketException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        mEventHandler.closeTouchDevice();
    }

    private void createTcp() {
        try {
            if (mServerSocket == null) {
                mServerSocket = new ServerSocket(8989);
                mServerSocket.setReuseAddress(true);
            }
            running = true;
            while(running) {
                Log.d(Log.TAG, "waiting for connection ...");
                Socket socket = mServerSocket.accept();
                InputStream inputStream = null;
                try {
                    Log.d(Log.TAG, "Accept a connection ...");
                    inputStream = socket.getInputStream();
                    mOutputStream = socket.getOutputStream();
                    BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
                    String line = null;
                    while((line = br.readLine()) != null) {
                        // Log.d(Log.TAG, "read : " + line);
                        processCmdData(line);
                    }
                    br.close();
                } catch(IOException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
                if (inputStream != null) {
                    inputStream.close();
                }
                if (mOutputStream != null) {
                    mOutputStream.close();
                    mOutputStream = null;
                }
            }
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private void sendOutData(String data) {
        try {
            if (mOutputStream != null) {
                Log.d(Log.TAG, "send data ...");
                mOutputStream.write(data.getBytes());
            }
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
}

    public void sendData(final String data) {
        Message msg = mPrivateHandler.obtainMessage(MSG_SENDOUT_DATA, data);
        mPrivateHandler.sendMessage(msg);
    }

    class PrivateHandler extends Handler {
        public PrivateHandler(Looper looper) {
            super(looper);
        }

        @Override
        public void handleMessage(Message msg) {
            switch(msg.what) {
            case MSG_SENDOUT_DATA:
                sendOutData((String)msg.obj);
                break;
            case MSG_TOUCH_DATA:
                passEventToHandler((String)msg.obj);
                break;
            }
        }
    }

    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // 遍历所用的网络接口
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// 得到每一个网络接口绑定的所有ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // 遍历每一个接口绑定的所有ip
                while (inet.hasMoreElements()) {
                    InetAddress ip = inet.nextElement();
                    if (!ip.isLoopbackAddress()
                            && InetAddressUtils.isIPv4Address(ip
                                    .getHostAddress())) {
                        ipaddress = ip.getHostAddress();
                    }
                }

            }
        } catch (SocketException e) {
            Log.d(Log.TAG, "error : " + e);
        }
        return ipaddress;

    }

    class Packet {
        public static final String REQUEST_POSITION = "request_position";
        public static final String REQUEST_SCREENSIZE = "request_screensize";
        public static final String REQUEST_UDPSERVER = "request_udpserver";
        public static final String REQUEST_TOUCH = "touch";
        public String cmd;
        public int x;
        public int y;
        public boolean pressed;
        public String touch;
    }
}
