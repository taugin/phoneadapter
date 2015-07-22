package com.android.phoneadapter;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.WindowManager;

import com.android.phoneadapter.floatview.FloatService;


public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        getWindow().setType(WindowManager.LayoutParams.TYPE_TOAST);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE, 0);
        setContentView(R.layout.activity_main);
        startService(new Intent(this, FloatService.class));
    }
}
