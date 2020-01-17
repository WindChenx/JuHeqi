package com.wind.juheqi;

import android.app.Activity;
import android.content.Intent;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.MediaStore;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.MediaController;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import com.wind.juheqi.uitls.Utils;

public class SystemVideoPlayer extends Activity implements View.OnClickListener {
    private final int PROGRESS=0;
    private VideoView videoView;
    private Uri uri;
    private LinearLayout llTop;
    private TextView tvName;
    private TextView tvTime;
    private Button btnVoice;
    private SeekBar seekbarVoice;
    private ImageView iv_battery;
    private Button btnSwitchPlayer;
    private LinearLayout llBottom;
    private TextView tvCurrentTime;
    private SeekBar seekbarVideo;
    private TextView tvDuration;
    private Button btnVideoExit;
    private Button btnVideoPre;
    private Button btnVideoStartPause;
    private Button btnVideoNext;
    private Button btnVideoSwitchScreen;
    private Utils utils;
    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS:
                    //得到当前播放进度
                    int currentPosition=videoView.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));
                    //每一秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;

            }
        }
    };


    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       initData();
        findViews();
        videoView=findViewById(R.id.videoview);

        uri=getIntent().getData();
        videoView.setVideoURI(uri);
        setListener();
//        videoView.setMediaController(new MediaController(this));

    }



    private void initData() {
        utils=new Utils();
    }

    private void findViews() {
        setContentView(R.layout.activity_system_video_player);
        llTop = findViewById( R.id.ll_top );
        tvName = findViewById( R.id.tv_name );
        tvTime = findViewById( R.id.tv_time );
        btnVoice = findViewById( R.id.btn_voice );
        seekbarVoice = findViewById( R.id.seekbar_voice );
        iv_battery = findViewById(R.id.iv_battery);
        btnSwitchPlayer = findViewById( R.id.btn_switch_player );
        llBottom = findViewById( R.id.ll_bottom );
        tvCurrentTime = findViewById( R.id.tv_current_time );
        seekbarVideo = findViewById( R.id.seekbar_video );
        tvDuration = findViewById( R.id.tv_duration );
        btnVideoExit = findViewById( R.id.btn_video_exit );
        btnVideoPre = findViewById( R.id.btn_video_pre );
        btnVideoStartPause = findViewById( R.id.btn_video_start_pause );
        btnVideoNext = findViewById( R.id.btn_video_next );
        btnVideoSwitchScreen = findViewById( R.id.btn_video_switch_screen );

        btnVoice.setOnClickListener( this );
        btnSwitchPlayer.setOnClickListener( this );
        btnVideoExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSwitchScreen.setOnClickListener( this );
    }

    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
            // Handle clicks for btnVoice
        } else if ( v == btnSwitchPlayer ) {
            // Handle clicks for btnSwitchPlayer
        } else if ( v == btnVideoExit ) {
            // Handle clicks for btnVideoExit
        } else if ( v == btnVideoPre ) {
            // Handle clicks for btnVideoPre
        } else if ( v == btnVideoStartPause ) {
            if(videoView.isPlaying()){
                //暂停
                videoView.pause();
                //按钮设置播放状态
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_play_selector);
            }else{
                //播放
                videoView.start();
                //按钮设置暂停状态
                btnVideoStartPause.setBackgroundResource(R.drawable.btn_video_pause_selector);
            }
            // Handle clicks for btnVideoStartPause
        } else if ( v == btnVideoNext ) {
            // Handle clicks for btnVideoNext
        } else if ( v == btnVideoSwitchScreen ) {
            // Handle clicks for btnVideoSwitchScreen
        }
    }
    private void setListener() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                int duration=videoView.getDuration();
                seekbarVideo.setMax(duration);
                tvDuration.setText(utils.stringForTime(duration));
                handler.sendEmptyMessage(PROGRESS);
                videoView.start();
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
                Toast.makeText(SystemVideoPlayer.this,"播放出错了",Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                Toast.makeText(SystemVideoPlayer.this,"播放完成",Toast.LENGTH_SHORT).show();

                finish();
            }
        });
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
    }
    class VideoOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{
        /**
         *
         * @param seekBar
         * @param progress
         * @param fromUser 是否由用户引起
         */
        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                videoView.seekTo(progress);
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }
}
