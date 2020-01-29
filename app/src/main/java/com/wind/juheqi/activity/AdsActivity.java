package com.wind.juheqi.activity;

import android.content.Intent;
import android.os.CountDownTimer;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.wind.juheqi.R;

public class AdsActivity extends AppCompatActivity {

    private TextView tvAds;
    private CountDownTimer countDownTimer;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ads);
        tvAds = findViewById(R.id.cross);
        countDownTimer = new CountDownTimer(5000+100, 1000) {//每过1000毫秒执行一次onTick()
            @Override
            public void onTick(long millisUntilFinished) {
                if(millisUntilFinished<1000){
                    tvAds.setVisibility(View.GONE);
                }else {
                    tvAds.setText("跳过" + (millisUntilFinished / 1000) );
                    Log.d("onTick",millisUntilFinished+"");

                }


            }

            @Override
            public void onFinish() {
                Intent intent = new Intent(AdsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        }.start();

        /**
         * 跳过
         */
        tvAds.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                countDownTimer.cancel();
                Intent intent = new Intent(AdsActivity.this, MainActivity.class);
                startActivity(intent);
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(countDownTimer!=null){
            countDownTimer.cancel();
        }
    }
}



