package com.android.phoneadapter;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.android.phoneadapter.floatview.FloatService;


public class MainActivity extends Activity {

    private TextView mTextView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mTextView = new TextView(this);
        setContentView(mTextView);
        startService(new Intent(this, FloatService.class));
        finish();
    }

    class ReadEventThread extends Thread {
        public void run() {
            try {
                FileInputStream fis = new FileInputStream("/dev/input/event0");
                Log.d(Log.TAG, "begin read");
                byte [] buffer = new byte[16];
                int read = 0;
                while((read = fis.read(buffer)) > 0) {
                    Log.d(Log.TAG, "line : " + new String(buffer, 0, read));
                }
                Log.d(Log.TAG, "end read");
            } catch (FileNotFoundException e) {
                Log.d(Log.TAG, "error : " + e);
            } catch (IOException e) {
                Log.d(Log.TAG, "error : " + e);
            }
        }
    }
}