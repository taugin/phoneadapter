package com.android.phoneadapter.socket;

import java.io.IOException;
import java.io.InputStream;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.phoneadapter.Log;
import com.android.phoneadapter.floatview.FloatView;
import com.google.gson.Gson;

public class EventSocket {

    private boolean running = false;
    private FloatView mFloatView;
    private OutputStream mOutputStream;
    private Context mContext;

    public EventSocket(Context context, FloatView floatView) {
        mContext = context;
        mFloatView = floatView;
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

    private void processCmdData(String data) {
        Log.d(Log.TAG, data);
        Gson gson = new Gson();
        Packet packet = gson.fromJson(data, Packet.class);
        if (packet != null) {
            if (Packet.REQUEST_SCREENSIZE.equals(packet.command)) {
                DisplayMetrics metrics = mContext.getResources().getDisplayMetrics();
                JSONObject object = new JSONObject();
                try {
                    object.put("command", "response_screensize");
                    object.put("w", metrics.widthPixels);
                    object.put("h", metrics.heightPixels);
                } catch (JSONException e) {
                    Log.d(Log.TAG, "error : " + e);
                }
                sendData(object.toString());
                return;
            }

            if (Packet.REQUEST_UDPSERVER.equals(packet.command)) {
                JSONObject object = new JSONObject();
                try {
                    object.put("command", "response_udpserver");
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
        int x = 0;
        int y = 0;
        JSONObject object;
        try {
            object = new JSONObject(data);
            if (object.has("x")) {
                x = object.getInt("x");
            }
            if (object.has("y")) {
                y = object.getInt("y");
            }
            mFloatView.updatePositionFromOuter(x, y);
        } catch (JSONException e) {
            Log.d(Log.TAG, "error : " + e);
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
            DatagramSocket datagramSocket = new  DatagramSocket(8990);
            byte data[] = new byte[1024];
            DatagramPacket packet = new DatagramPacket(data, data.length);
            running = true;
            String packetData = null;
            while(running) {
                Log.d(Log.TAG, "waiting for data ...");
                datagramSocket.receive(packet);
                packetData = new String(packet.getData(), 0, packet.getLength());
                processPosData(packetData);
            }
            datagramSocket.close();
        } catch (SocketException e) {
            Log.d(Log.TAG, "error : " + e);
        } catch (IOException e) {
            Log.d(Log.TAG, "error : " + e);
        }
    }

    private void createTcp() {
        try {
            ServerSocket serverSocket = new ServerSocket(8989);
            running = true;
            while(running) {
                Log.d(Log.TAG, "waiting for connection ...");
                Socket socket = serverSocket.accept();
                InputStream inputStream = null;
                try {
                    Log.d(Log.TAG, "Accept a connection ...");
                    inputStream = socket.getInputStream();
                    mOutputStream = socket.getOutputStream();
                    byte buf[] = new byte[512];
                    int read = 0;
                    String packetData = null;
                    while((read = inputStream.read(buf)) > 0) {
                        Log.d(Log.TAG, "read : " + read);
                        packetData = new String(buf, 0, read);
                        String allData[] = packetData.split("#");
                        for (String s : allData) {
                            if (!TextUtils.isEmpty(s)) {
                                processCmdData(s);
                            }
                        }
                    }
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
        public String command;
        public int x;
        public int y;
    }
}
