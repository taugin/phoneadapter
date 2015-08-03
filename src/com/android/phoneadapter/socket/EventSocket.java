package com.android.phoneadapter.socket;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
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
import android.text.TextUtils;
import android.util.DisplayMetrics;

import com.android.phoneadapter.EventSender;
import com.android.phoneadapter.Log;
import com.android.phoneadapter.floatview.PointerView;
import com.google.gson.Gson;

public class EventSocket {

    private boolean running = false;
    private PointerView mFloatView;
    private OutputStream mOutputStream;
    private Context mContext;
    private FileOutputStream mDeviceOutputStream = null;
    private ServerSocket mServerSocket;
    private DatagramSocket mDatagramSocket;

    public EventSocket(Context context, PointerView floatView) {
        mContext = context;
        mFloatView = floatView;
        
        try {
            mDeviceOutputStream = new FileOutputStream("/dev/input/event0");
        } catch (FileNotFoundException e) {
            Log.d(Log.TAG, "error : " + e);
        }
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
    }

    private void processCmdData(String data) {
        // Log.d(Log.TAG, data);
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
            
            if (Packet.REQUEST_POSITION.equals(packet.command)) {
                mFloatView.updatePositionFromOuter(packet.x, packet.y);
                // Log.d(Log.TAG, "pressed : " + packet.pressed);
                if (packet.pressed) {
                    // EventSender.sendevent(packet.device, packet.type, packet.code, packet.value);
                }
                return;
            }
        }
    }

    public String getLocalHostIp() {
        String ipaddress = "";
        try {
            Enumeration<NetworkInterface> en = NetworkInterface
                    .getNetworkInterfaces();
            // �������õ�����ӿ�
            while (en.hasMoreElements()) {
                NetworkInterface nif = en.nextElement();// �õ�ÿһ������ӿڰ󶨵�����ip
                Enumeration<InetAddress> inet = nif.getInetAddresses();
                // ����ÿһ���ӿڰ󶨵�����ip
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
        Log.d(Log.TAG, "data : " + data);
        Gson gson = new Gson();
        Packet packet = gson.fromJson(data, Packet.class);
        if (packet != null) {
            if (Packet.REQUEST_POSITION.equals(packet.command)) {
                mFloatView.updatePositionFromOuter(packet.x, packet.y);
                return;
            }
            if (Packet.REQUEST_TOUCH.equals(packet.command)) {
                EventSender.sendEvent(packet.type, packet.code, packet.value);
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
            EventSender.openDevice("/dev/input/event0");
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
        public static final String REQUEST_TOUCH = "request_touch";
        public String command;
        public int x;
        public int y;
        public boolean pressed;
        public String device;
        public String type;
        public String code;
        public String value;
    }
}
