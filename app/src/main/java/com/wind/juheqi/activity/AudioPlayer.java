package com.wind.juheqi.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.graphics.drawable.AnimationDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wind.juheqi.IMediaService;
import com.wind.juheqi.R;

import com.wind.juheqi.service.MediaService;
import com.wind.juheqi.uitls.Utils;

public class AudioPlayer extends Activity implements View.OnClickListener {

    private static final int PROGRESS = 1;
    private ImageView iv_icon;
    private ImageView ivIcon;
    private TextView tvName;
    private TextView tvArtist;
    private TextView tvTime;
    private SeekBar seekbarAudio;
    private Button btnAudioPlaymode;
    private Button btnAudioPre;
    private Button btnAudioStartPause;
    private Button btnAudioNext;

    private int position;
    private IMediaService mediaService;
    private boolean notification;
    private  MyReceiver receiver;
    private Utils utils;

    private ServiceConnection con=new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mediaService=IMediaService.Stub.asInterface(service);
            try {
                if(!notification){
                    //就可以操作服务了
                    mediaService.openAudio(position);
                }else{
                    //获取数据-要服务发广播
                    mediaService.notifyChange(MediaService.OPEN_AUDIO);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mediaService=null;

        }
    };

    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initData();
        findViews();
        getData();

        bindAndStartService();


    }

    private void initData() {
        utils=new Utils();
        receiver =new MyReceiver();
        IntentFilter intentfilter = new IntentFilter();
        intentfilter.addAction(MediaService.OPEN_AUDIO);//监听打开音乐成功的动作
        registerReceiver(receiver, intentfilter);
    }

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS://更新时间

                    //得到当前的进度

                    try {
                        int currentPosition = mediaService.getCurrentPosition();
                        int duration = mediaService.getDuration();

                        tvTime.setText(utils.stringForTime(currentPosition)+"/"+utils.stringForTime(duration));


                        //跟新进度
                        seekbarAudio.setProgress(currentPosition);

                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }

                    handler.removeMessages(PROGRESS);
                    handler.sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
            }
        }
    };


    class MyReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            //获取视频的名称和演唱者的信息--主线程
            setViewData();
            try {
                seekbarAudio.setMax(mediaService.getDuration());
            } catch (RemoteException e) {
                e.printStackTrace();
            }


            handler.sendEmptyMessage(PROGRESS);
        }
    }

    /**
     * 设置歌曲名称和演唱者
     */
    private void setViewData() {
        try {
            tvName.setText(mediaService.getName());
            tvArtist.setText(mediaService.getArtist());



        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if(con != null){
            unbindService(con);
            con = null;
        }


        //取消注册广播
        if(receiver != null){
            unregisterReceiver(receiver);
            receiver = null;
        }
    }

    private void findViews() {
        setContentView(R.layout.activity_audioplayer);
        ivIcon = findViewById( R.id.iv_icon );
        tvName = findViewById( R.id.tv_name );
        tvArtist = findViewById( R.id.tv_artist );
        tvTime = findViewById( R.id.tv_time );
        seekbarAudio = findViewById( R.id.seekbar_audio );
        btnAudioPlaymode = findViewById( R.id.btn_audio_playmode );
        btnAudioPre = findViewById( R.id.btn_audio_pre );
        btnAudioStartPause = findViewById( R.id.btn_audio_start_pause );
        btnAudioNext = findViewById( R.id.btn_audio_next );


        iv_icon=findViewById(R.id.iv_icon);
        iv_icon.setBackgroundResource(R.drawable.animation_list);
        AnimationDrawable animationDrawable= (AnimationDrawable) iv_icon.getBackground();
        animationDrawable.start();

        btnAudioPlaymode.setOnClickListener(this);
        btnAudioPre.setOnClickListener( this );
        btnAudioStartPause.setOnClickListener( this );
        btnAudioNext.setOnClickListener( this );

        seekbarAudio.setOnSeekBarChangeListener(new MyOnSeekBarChangeListener());
    }
    class MyOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener {

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                try {
                    mediaService.seekTo(progress);
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {

        }
    }


    private void getData() {
        notification = getIntent().getBooleanExtra("Notification", false);
        if(!notification){
            //从列表来的
            position = getIntent().getIntExtra("position", 0);
        }

    }

    private void bindAndStartService() {
        Intent intent=new Intent(this,MediaService.class);
        intent.setAction("com.wind.juheqi_OPENAUDIO");
        bindService(intent,con,Context.BIND_AUTO_CREATE);
        startService(intent);
    }

    @Override
    public void onClick(View v) {
        if ( v == btnAudioPlaymode ) {
            changePlaymode();

        } else if ( v == btnAudioPre ) {
            if(mediaService!=null){
                try {
                    mediaService.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } else if ( v == btnAudioStartPause ) {

            try {
                if(mediaService.isPlaying()){
                    //暂停
                    mediaService.pause();
                    //按钮设置播放状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_start_selector);
                }else{
                    //播放
                    mediaService.start();
                    //按钮设置暂停状态
                    btnAudioStartPause.setBackgroundResource(R.drawable.btn_audio_pause_selector);
                }

            } catch (RemoteException e) {
                e.printStackTrace();
            }

        } else if( v == btnAudioNext ) {
            if(mediaService!=null){
                try {
                    mediaService.next();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    private void changePlaymode() {
        try {
            int playmode=mediaService.getPlaymode();
            if(playmode==MediaService.REPEAT_ORDER){
                playmode=MediaService.REPEAT_SINGLE;
            }else if(playmode==MediaService.REPEAT_SINGLE){
                playmode=MediaService.REPEAT_ALL;
            }else if(playmode==MediaService.REPEAT_ALL){
                playmode=MediaService.REPEAT_ORDER;
            }else {
                playmode=MediaService.REPEAT_ORDER;
            }

            mediaService.setPlaymode(playmode);
            showPlaymode();
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }

    private void showPlaymode() {
        try {
            int playmode = mediaService.getPlaymode();//从服务里面

            if (playmode == MediaService.REPEAT_ORDER) {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            } else if (playmode == MediaService.REPEAT_SINGLE) {
                Toast.makeText(AudioPlayer.this, "单曲播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_single_selector);
            } else if (playmode == MediaService.REPEAT_ALL) {
                Toast.makeText(AudioPlayer.this, "全部播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_all_selector);
            } else {
                Toast.makeText(AudioPlayer.this, "顺序播放", Toast.LENGTH_SHORT).show();
                btnAudioPlaymode.setBackgroundResource(R.drawable.btn_audio_playmode_normal_selector);
            }

        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
