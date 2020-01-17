package com.wind.juheqi;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.widget.TextView;

public class Test extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        TextView textView=new TextView(this);
        textView.setText("测试页面");
        textView.setGravity(Gravity.CENTER);
        setContentView(textView);
        Log.d("Tag","onCreate");
    }
    @Override
    protected void onStart() {
        super.onStart();
        Log.d("Tag","onStart");

    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.d("Tag","OnResuem");


    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.d("Tag","onPause");

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d("Tag","onStop");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("Tag","onRestart");
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Log.d("Tag","onDestroy");
    }
}
