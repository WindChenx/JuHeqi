package com.wind.juheqi.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.media.AudioManager;

import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.DisplayMetrics;
import android.view.GestureDetector;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.wind.juheqi.R;
import com.wind.juheqi.domain.MediaItem;
import com.wind.juheqi.uitls.CacheUtils;
import com.wind.juheqi.uitls.Utils;
import com.wind.juheqi.view.VitamioVideoView;


import java.util.ArrayList;

import io.vov.vitamio.MediaPlayer;
import io.vov.vitamio.Vitamio;

public class VitamioVideoPlayer extends Activity implements View.OnClickListener {
    private final int PROGRESS=0;
    /**
     * 隐藏控制面板
     */
    private static final int HIDE_MEDIACONTROLLER = 1;
    /**
     * 默认播放
     */
    private static final int DEFAULT_SCREEN = 2;
    /**
     * 全屏播放
     */
    private static final int FULL_SCREEN = 3;
    //显示网速
    private static final int SHOW_SPEED=4;
    private VitamioVideoView videoView;
    private Uri uri;
    private LinearLayout llTop;

    private Button btnVoice;
    private SeekBar seekbarVoice;

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
    private ArrayList<MediaItem> mediaItems;
    private int position;
    private GestureDetector detector;
    private RelativeLayout rl_loading;
    private LinearLayout ll_buffer;
    private TextView tv_buffer_netspeed;
    private TextView tv_loading_netspeed;
    /**
     * 屏幕的宽
     */
    private int screenWidth;
    /**
     * 屏幕的高
     */
    private int screenHeight;
    /**
     * 视频的本身的宽和高
     */
    private int videoWidth;
    private int videoHeight;
    /**
     * 是否全屏播放
     */
    private boolean isFullScreen = false;

    private AudioManager am;
    //当前音量
    private int currentVolume;
    //最大音量
    private int maxVolume;
    //是否静音 默认不是静音
    private boolean isMute=false;
    //是否是网络资源
    private boolean isNetUri=false;
    private int prePosition=0;

    Handler handler=new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            switch (msg.what){
                case PROGRESS:
                    //得到当前播放进度
                    int currentPosition= (int) videoView.getCurrentPosition();
                    seekbarVideo.setProgress(currentPosition);
                    tvCurrentTime.setText(utils.stringForTime(currentPosition));

                    //设置缓冲效果
                    if(isNetUri){
                        int buffer=videoView.getBufferPercentage();//0-100
                        int totalBuffer=seekbarVideo.getMax()*buffer;
                        int secondaryProgress=totalBuffer/100;
                        seekbarVideo.setSecondaryProgress(secondaryProgress);

                    }else {
                        seekbarVideo.setSecondaryProgress(0);
                    }

                    int buffer=currentPosition-prePosition;
                    if(videoView.isPlaying()){
                        if(buffer<500){
                            ll_buffer.setVisibility(View.VISIBLE);
                        }else {
                            ll_buffer.setVisibility(View.GONE);
                        }
                    }
                    prePosition=currentPosition;

                    //每一秒更新一次
                    removeMessages(PROGRESS);
                    sendEmptyMessageDelayed(PROGRESS,1000);
                    break;
                case HIDE_MEDIACONTROLLER:
                    hideMediaController();
                    break;
                case SHOW_SPEED:
                    String netSpeed=utils.getNetSpeed(VitamioVideoPlayer.this);
                    tv_buffer_netspeed.setText("缓冲中..."+netSpeed);
                    tv_loading_netspeed.setText("玩命加载中..."+netSpeed);
                    handler.sendEmptyMessageDelayed(SHOW_SPEED,1000);

                    break;

            }
        }
    };



    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
       initData();
        findViews();


        getData();
        setData();
        setListener();


    }

    private void initData() {
        utils=new Utils();

        am= (AudioManager) getSystemService(AUDIO_SERVICE);
        currentVolume=am.getStreamVolume(AudioManager.STREAM_MUSIC);
        maxVolume=am.getStreamMaxVolume(AudioManager.STREAM_MUSIC);
        //得到屏幕的宽和高
        DisplayMetrics displayMetrics = new DisplayMetrics();
        this.getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

        //实例化手势识别器
        detector=new GestureDetector(this,new GestureDetector.SimpleOnGestureListener(){
            @Override
            public void onLongPress(MotionEvent e) {
                super.onLongPress(e);
                startAndPause();
            }

            @Override
            public boolean onDoubleTap(MotionEvent e) {
                if (isFullScreen) {
                    setVideoType(DEFAULT_SCREEN);
                } else {
                    setVideoType(FULL_SCREEN);
                }
                return super.onDoubleTap(e);
            }

            @Override
            public boolean onSingleTapConfirmed(MotionEvent e) {
                if(isShowMediaController){
                    hideMediaController();
                    handler.removeMessages(HIDE_MEDIACONTROLLER);
                }else {
                    showMediaController();
                    handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
                }
                return super.onSingleTapConfirmed(e);

            }
        });
    }
    private void findViews() {
        //初始化解码器
        Vitamio.isInitialized(getApplicationContext());
        setContentView(R.layout.activity_vitamio_video_player);
        videoView=findViewById(R.id.videoview);
        llTop = findViewById( R.id.ll_top );


        btnVoice = findViewById( R.id.btn_voice );
        seekbarVoice = findViewById( R.id.seekbar_voice );

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
        rl_loading=findViewById(R.id.rl_loading);
        ll_buffer=findViewById(R.id.ll_buffer);
        tv_buffer_netspeed=findViewById(R.id.tv_buffer_netspeed);
        tv_loading_netspeed=findViewById(R.id.tv_loading_netspeed);

        btnVoice.setOnClickListener( this );
        btnSwitchPlayer.setOnClickListener( this );
        btnVideoExit.setOnClickListener( this );
        btnVideoPre.setOnClickListener( this );
        btnVideoStartPause.setOnClickListener( this );
        btnVideoNext.setOnClickListener( this );
        btnVideoSwitchScreen.setOnClickListener( this );

        //设置最大值
        seekbarVoice.setMax(maxVolume);
        //设置默认值
        seekbarVoice.setProgress(currentVolume);
        handler.sendEmptyMessage(SHOW_SPEED);
    }

    @Override
    public void onClick(View v) {
        if ( v == btnVoice ) {
           isMute=!isMute;
           updateVolume(currentVolume);
        } else if ( v == btnSwitchPlayer ) {
            showSwitchPlayer();
        } else if ( v == btnVideoExit ) {
            finish();
      
        } else if ( v == btnVideoPre ) {
            setPlayPre();
            
        } else if ( v == btnVideoStartPause ) {
            startAndPause();

        } else if ( v == btnVideoNext ) {
                setPlayNext();
        } else if ( v == btnVideoSwitchScreen ) {
            if (isFullScreen) {
                setVideoType(DEFAULT_SCREEN);
            } else {
                setVideoType(FULL_SCREEN);
            }
        }
    }

    private void showSwitchPlayer() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("提示");
        builder.setMessage("当前使用万能播放器播放，是否切换到系统播放器播放视频！");
        builder.setPositiveButton("确定", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

                startSystemPlayer();
            }
        });
        builder.setNegativeButton("取消", null);
        builder.show();
    }



    private void startAndPause() {
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
    }

    //播放上一个
    private void setPlayPre() {
        if(mediaItems!=null&&mediaItems.size()>0){
            position--;
            if(position>=0){
                MediaItem mediaItem=mediaItems.get(position);
                videoView.setVideoPath(mediaItem.getData());//设置播放地址
                isNetUri=utils.isNetUri(mediaItem.getData());
                setButtonState();//设置按钮状态
                rl_loading.setVisibility(View.VISIBLE);

            }
        }
    }

    //播放下一个
    private void setPlayNext() {
        if(mediaItems!=null&&mediaItems.size()>0){
            position++;
            if(position<mediaItems.size()){
                MediaItem mediaItem=mediaItems.get(position);
                videoView.setVideoPath(mediaItem.getData());//设置播放地址
                isNetUri=utils.isNetUri(mediaItem.getData());
                setButtonState();//设置按钮状态

                if(position==mediaItems.size()-1){
                    Toast.makeText(VitamioVideoPlayer.this,"已经是最后一个了",Toast.LENGTH_SHORT).show();
                }
                rl_loading.setVisibility(View.VISIBLE);

            }else {
                finish();
            }
        }
    }

    private void setButtonState() {
        if(mediaItems!=null&&mediaItems.size()>0){
            if(position==0){
                btnVideoPre.setEnabled(false);
                btnVideoPre.setBackgroundResource(R.drawable.btn_pre_gray);

            }else if (position==mediaItems.size()-1){
                btnVideoNext.setEnabled(false);
                btnVideoNext.setBackgroundResource(R.drawable.btn_next_gray);

            }else {
                btnVideoNext.setEnabled(true);
                btnVideoNext.setBackgroundResource(R.drawable.btn_video_next_selector);
                btnVideoPre.setEnabled(true);
                btnVideoPre.setBackgroundResource(R.drawable.btn_video_pre_selector);
            }
        }
    }

    private void getData() {
//        uri=getIntent().getData();//得到一个地址：文件浏览器，浏览器，相册
        mediaItems= (ArrayList<MediaItem>) getIntent().getSerializableExtra("videoList");
        position=getIntent().getIntExtra("position",0);
    }
    private void setData() {
        if(mediaItems!=null&&mediaItems.size()>0){
            MediaItem mediaItem=mediaItems.get(position);
            videoView.setVideoPath(mediaItem.getData());
            isNetUri=utils.isNetUri(mediaItem.getData());
        }
        setButtonState();
        videoView.setKeepScreenOn(true);
//

    }


    private void setListener() {
        videoView.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
            @Override
            public void onPrepared(MediaPlayer mp) {
                videoWidth=mp.getVideoWidth();
                videoHeight=mp.getVideoHeight();
                int duration= (int) videoView.getDuration();
                seekbarVideo.setMax(duration);
                tvDuration.setText(utils.stringForTime(duration));
                handler.sendEmptyMessage(PROGRESS);
                videoView.start();

                if(mediaItems != null&& mediaItems.size() >0){
                    String key = mediaItems.get(position).getData();
                    int history = CacheUtils.getInt(VitamioVideoPlayer.this, key);
                    if(history > 0){
                        videoView.seekTo(history);
                    }
                }else if(uri != null){
                    String key = uri.toString();
                    int history = CacheUtils.getInt(VitamioVideoPlayer.this, key);
                    if(history > 0){
                        videoView.seekTo(history);
                    }
                }

                hideMediaController();
                setVideoType(DEFAULT_SCREEN);
                //隐藏加载页面
                rl_loading.setVisibility(View.GONE);
            }
        });
        videoView.setOnErrorListener(new MediaPlayer.OnErrorListener() {
            @Override
            public boolean onError(MediaPlayer mp, int what, int extra) {
//                Toast.makeText(SystemVideoPlayer.this,"播放出错了",Toast.LENGTH_SHORT).show();
                showErrorDialog();
                return true;
            }
        });
        videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
//                Toast.makeText(SystemVideoPlayer.this,"播放完成",Toast.LENGTH_SHORT).show();
//
//                finish();
                setPlayNext();
            }
        });
        seekbarVideo.setOnSeekBarChangeListener(new VideoOnSeekBarChangeListener());
        seekbarVoice.setOnSeekBarChangeListener(new VoiceOnSeekBarChangeListener());

        //设置监听卡
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
//            videoView.setOnInfoListener(new MyOnfoListener());
//        }
    }

    private void showErrorDialog() {
        new AlertDialog.Builder(this).setTitle("提示").setMessage("播放视频出错了，请检查网络或者视频是否有缺损！")
                .setPositiveButton("确定", new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int whichButton) {
                        finish();
                    }
                }).setCancelable(false).show();
    }

    //跳转到系统播放器
    private void startSystemPlayer() {
        if(videoView != null){
            videoView.stopPlayback();
        }


        Intent intent = new Intent(this, SystemVideoPlayer.class);
        if(mediaItems != null && mediaItems.size() >0){

            Bundle bundle = new Bundle();
            bundle.putSerializable("videolist", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);

        }else if(uri != null){
            intent.setData(uri);//文件
        }

        startActivity(intent);

        finish();

    }

    private void setVideoType(int type) {
        switch (type) {
            case FULL_SCREEN://全屏

                videoView.setVideoSize(screenWidth, screenHeight);
                isFullScreen = true;

                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_default_selector);
                break;
            case DEFAULT_SCREEN://默认

                //真实视频的高和宽
                int mVideoWidth = videoWidth;
                int mVideoHeight = videoHeight;


                /**
                 * 要播放视频的宽和高
                 */

                int width = screenWidth;
                int height = screenHeight;
                if (mVideoWidth > 0 && mVideoHeight > 0) {
                    // for compatibility, we adjust size based on aspect ratio
                    if (mVideoWidth * height < width * mVideoHeight) {
                        //Log.i("@@@", "image too wide, correcting");
                        width = height * mVideoWidth / mVideoHeight;
                    } else if (mVideoWidth * height > width * mVideoHeight) {
                        //Log.i("@@@", "image too tall, correcting");
                        height = width * mVideoHeight / mVideoWidth;
                    }

                    videoView.setVideoSize(width, height);

                }

                isFullScreen = false;
                btnVideoSwitchScreen.setBackgroundResource(R.drawable.btn_switch_screen_full_selector);
                break;
        }
    }

    class VoiceOnSeekBarChangeListener implements SeekBar.OnSeekBarChangeListener{

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if(fromUser){
                updateVolumeProgress(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            handler.removeMessages(HIDE_MEDIACONTROLLER);
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
        }
    }

    //设置静音
    private void updateVolume(int volume) {
        if (isMute) {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, 0, 0);
            seekbarVoice.setProgress(0);//设置seekBar进度
        } else {
            am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
            seekbarVoice.setProgress(volume);//设置seekBar进度
            currentVolume = volume;
        }

    }
    /**
     * 根据传入的值修改音量
     *
     * @param volume
     */
    private void updateVolumeProgress(int volume) {
        am.setStreamVolume(AudioManager.STREAM_MUSIC, volume, 0);
        seekbarVoice.setProgress(volume);//设置seekBar进度
        currentVolume = volume;
        if (volume <= 0) {
            isMute = true;
        } else {
            isMute = false;
        }
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
            handler.removeMessages(HIDE_MEDIACONTROLLER);

        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);

        }
    }


//是否隐藏控制面板 是为true,否为false
    private Boolean isShowMediaController=false;
    //隐藏
    private void hideMediaController(){

        llBottom.setVisibility(View.GONE);
        llTop.setVisibility(View.GONE);
        isShowMediaController=false;
    }
    //显示
    private void showMediaController(){

        llBottom.setVisibility(View.VISIBLE);
        llTop.setVisibility(View.VISIBLE);
        isShowMediaController=true;

    }

    private float startY;
    private float touchRang;
    private int mVol;
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //把事件给手势识别器解析
        detector.onTouchEvent(event);
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                //1.按下的时候记录初始值
                startY = event.getY();
                touchRang = Math.min(screenHeight, screenWidth);//screenHeight
                mVol = am.getStreamVolume(AudioManager.STREAM_MUSIC);
                handler.removeMessages(HIDE_MEDIACONTROLLER);
                break;
            case MotionEvent.ACTION_MOVE:
                //2.来到新的坐标
                float endY = event.getY();
                //3.计算偏移量
                float distanceY = startY - endY;
                //屏幕滑动的距离： 总距离 = 改变的声音： 最大音量
                float changVolume = (distanceY / touchRang) * maxVolume;

                //最终的声音= 原来的音量 + 改变的声音；
                float volume = Math.min(Math.max(mVol + changVolume, 0), maxVolume);

                if (changVolume != 0) {
                    updateVolumeProgress((int) volume);
                }
                break;
            case MotionEvent.ACTION_UP:
                handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER, 5000);
                break;
        }
        return super.onTouchEvent(event);
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode==KeyEvent.KEYCODE_VOLUME_DOWN){
            currentVolume--;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
            return true;
        }else if (keyCode==KeyEvent.KEYCODE_VOLUME_UP){
            currentVolume++;
            updateVolumeProgress(currentVolume);
            handler.removeMessages(HIDE_MEDIACONTROLLER);
            handler.sendEmptyMessageDelayed(HIDE_MEDIACONTROLLER,5000);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeCallbacksAndMessages(null);
    }

//    private class MyOnfoListener implements MediaPlayer.OnInfoListener {
//        @Override
//        public boolean onInfo(MediaPlayer mp, int what, int extra) {
//            switch (what){
//                case MediaPlayer.MEDIA_INFO_BUFFERING_START:
//                    ll_buffer.setVisibility(View.VISIBLE);
//                    break;
//                case MediaPlayer.MEDIA_INFO_BUFFERING_END:
//                    ll_buffer.setVisibility(View.GONE);
//                    break;
//            }
//            return true;
//        }
//    }
}
