package com.android.phoneadapter;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.net.SocketException;
import java.util.Enumeration;

import org.apache.http.conn.util.InetAddressUtils;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.TextUtils;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.android.phoneadapter.floatview.FloatService;

public class MainActivity extends Activity implements OnClickListener {

    private TextView mClientIp;
    private EditText mServerIP;
    private EditText mTouchDeviceNode;
    private EditText mKeyDeviceNode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = null;
        mServerIP = (EditText) findViewById(R.id.server_ip);
        mClientIp = (TextView) findViewById(R.id.client_ip);
        mTouchDeviceNode = (EditText) findViewById(R.id.touch_device_node);
        mKeyDeviceNode = (EditText) findViewById(R.id.key_device_node);
        button = (Button) findViewById(R.id.start_server);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.start_touch);
        button.setOnClickListener(this);
        button = (Button) findViewById(R.id.set_server_ip);
        button.setOnClickListener(this);
        
        button = (Button) findViewById(R.id.set_rw);
        button.setOnClickListener(this);
        
        mClientIp.setText(getLocalHostIp());
        String serverIp = PreferenceManager.getDefaultSharedPreferences(this).getString("server_ip", null);
        mServerIP.setText(serverIp);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.start_server) {
            Intent intent = new Intent(this, FloatService.class);
            stopService(intent);
            startService(intent);
            finish();
        } else if (v.getId() == R.id.start_touch) {
            startActivity(new Intent(this, TouchActivity.class));
            finish();
        } else if (v.getId() == R.id.set_server_ip) {
            String ip = mServerIP.getText().toString();
            if (!TextUtils.isEmpty(ip)) {
                boolean ret = PreferenceManager.getDefaultSharedPreferences(this).edit().putString("server_ip", ip).commit();
                Toast.makeText(this, "Set success", Toast.LENGTH_SHORT).show();
            }
        } else if (v.getId() == R.id.set_rw) {
            String touchDeviceNode = mTouchDeviceNode.getText().toString();
            String keyDeviceNode = mKeyDeviceNode.getText().toString();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("touch_device", touchDeviceNode).commit();
            PreferenceManager.getDefaultSharedPreferences(this).edit().putString("key_device", keyDeviceNode).commit();
            try {
                Process p = Runtime.getRuntime().exec("su");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                String cmd1 = "chmod 666 " + touchDeviceNode + "\n";
                String cmd2 = "chmod 666 " + keyDeviceNode + "\n";
                Log.d(Log.TAG, "cmd1 : " + cmd1);
                Log.d(Log.TAG, "cmd2 : " + cmd2);
                bw.write(cmd1);
                bw.write(cmd2);
                bw.write("exit\n");
                bw.flush();
                bw.close();
            } catch (IOException e) {
                e.printStackTrace();
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
}