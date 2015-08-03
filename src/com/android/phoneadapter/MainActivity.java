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
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
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
    private EditText mDeviceNode;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Button button = null;
        mServerIP = (EditText) findViewById(R.id.server_ip);
        mClientIp = (TextView) findViewById(R.id.client_ip);
        mDeviceNode = (EditText) findViewById(R.id.device_node);
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
            String deviceNode = mDeviceNode.getText().toString();
            intent.putExtra("device_node", deviceNode);
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
            String deviceNode = mDeviceNode.getText().toString();
            try {
                Process p = Runtime.getRuntime().exec("su");
                BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));
                String cmd = "chmod 666 " + deviceNode + "\n";
                Log.d(Log.TAG, "cmd : " + cmd);
                bw.write(cmd);
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