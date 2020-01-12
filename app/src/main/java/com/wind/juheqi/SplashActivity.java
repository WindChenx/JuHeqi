package com.wind.juheqi;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;

public class SplashActivity extends Activity {
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

                Intent intent=new Intent(SplashActivity.this,AdsActivity.class);
                startActivity(intent);
                finish();

    }
}
