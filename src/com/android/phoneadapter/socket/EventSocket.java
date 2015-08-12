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
import java.util.List;

import org.apache.http.conn.util.InetAddressUtils;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.phoneadapter.EventSender;
import com.android.phoneadapter.Log;
import com.android.phoneadapter.floatview.FloatService;
import com.android.phoneadapter.floatview.MaskView;
import com.android.phoneadapter.socket.EventSocket.Packet.TouchEvent;
import com.google.gson.Gson;

public class EventSocket {

    private boolean running = false;
    private OutputStream mOutputStream;
    private Context mContext;
    private ServerSocket mServerSocket;
    private DatagramSocket mDatagramSocket;
    private String mEventFile;
    private FloatService mFloatService;

    public EventSocket(Context context, FloatService floatService, String eventFile) {
        mContext = context;
        mFloatService = floatService;
        if (TextUtils.isEmpty(eventFile)) {
            eventFile = "/dev/input/event0";
        }
        mEventFile = eventFile;
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
        Gson gson = new Gson();
        Packet packet = gson.fromJson(data, Packet.class);
        if (packet != null) {
            if (Packet.REQUEST_SCREENSIZE.equals(packet.cmd)) {
                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                JSONObject object = new JSONObject();
                try {
                    object.put("cmd", "response_screensize");
                    object.put("w", metrics.widthPixels);
                    object.put("h", metrics.heightPixels);
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

    private void processPosData(String data) {
        // Log.d(Log.TAG, "data : " + data);
        Gson gson = new Gson();
        Packet packet = gson.fromJson(data, Packet.class);
        if (packet != null) {
            if (Packet.REQUEST_POSITION.equals(packet.cmd)) {
                if (mFloatService != null) {
                    mFloatService.updatePointerPosition(packet.x, packet.y);
                }
                return;
            }
            if (Packet.REQUEST_TOUCH.equals(packet.cmd)) {
                if (packet.touch != null) {
                    for (TouchEvent event : packet.touch) {
                        // Log.d(Log.TAG, EventSender.mFd + " " + mEventFile + " " + event.type + " " + event.code + " " + event.value);
                        EventSender.sendEvent(event.type, event.code, event.value);
                    }
                }
                return;
            }
        }
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
            EventSender.openDevice(mEventFile);
            while(running) {
                mDatagramSocket.receive(packet);
                packetData = new String(packet.getData(), 0, packet.getLength());
                processPosData(packetData);
            }
            mDatagramSocket.close();
            EventSender.closeDevice();
        } catch (SocketException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
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
                        processByThread(line);
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

    private void processByThread(final String str) {
        new Thread(){
            public void run() {
                processCmdData(str);
            }
        }.start();
    }

    public void sendData(final String data) {
        new Thread(){
            public void run() {
                    try {
                        if (mOutputStream != null) {
                            Log.d(Log.TAG, "send data ...");
                            mOutputStream.write(data.getBytes());
                        }
                    } catch (IOException e) {
                        Log.d(Log.TAG, "error : " + e);
                    }
            }
        }.start();
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
        public List<TouchEvent> touch;
        class TouchEvent {
            public String device;
            public String type;
            public String code;
            public String value;
        }
    }
}
