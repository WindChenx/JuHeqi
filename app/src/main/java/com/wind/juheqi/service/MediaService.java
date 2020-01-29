package com.wind.juheqi.service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Color;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.IBinder;
import android.os.RemoteException;
import android.provider.MediaStore;
import android.widget.Toast;

import com.wind.juheqi.IMediaService;
import com.wind.juheqi.R;
import com.wind.juheqi.activity.AudioPlayer;
import com.wind.juheqi.domain.MediaItem;

import java.io.IOException;
import java.util.ArrayList;

public class MediaService extends Service {
    public static final String OPEN_AUDIO = "com.wind.juheqi.OPEN_AUDIO";
    private IMediaService.Stub stub=new IMediaService.Stub() {
      MediaService mediaService=MediaService.this;
      @Override
      public void basicTypes(int anInt, long aLong, boolean aBoolean, float aFloat, double aDouble, String aString) throws RemoteException {

      }

      @Override
      public void openAudio(int position) throws RemoteException {
            mediaService.openAudio(position);
      }

      @Override
      public void start() throws RemoteException {
            mediaService.start();
      }

      @Override
      public void pause() throws RemoteException {
            mediaService.pause();
      }

      @Override
      public void next() throws RemoteException {
          mediaService.next();

      }

      @Override
      public void pre() throws RemoteException {
            mediaService.pre();
      }

      @Override
      public int getPlaymode() throws RemoteException {
          return mediaService.getPlaymode();
      }

      @Override
      public void setPlaymode(int playmode) throws RemoteException {
            mediaService.setPlaymode(playmode);
      }

      @Override
      public int getCurrentPosition() throws RemoteException {
          return mediaService.getCurrentPosition();
      }

      @Override
      public int getDuration() throws RemoteException {
          return mediaService.getDuration();
      }

      @Override
      public String getName() throws RemoteException {
          return mediaService.getName();
      }

      @Override
      public String getArtist() throws RemoteException {
          return mediaService.getArtist();
      }

      @Override
      public void seekTo(int seekto) throws RemoteException {
          mediaService.seekTo(seekto);
      }

      @Override
      public boolean isPlaying() throws RemoteException {
          return mediaService.isPlaying();
      }

        @Override
        public void notifyChange(String action) throws RemoteException {
            mediaService.notifyChange(action);
        }


    };


    /**
     * 音频列表
     */
    private ArrayList<MediaItem> mediaItems;
    /**
     * 当前列表的播放位置
     */
    private int position;
    /**
     * 一首音乐的信息
     */
    private MediaItem mediaItem;
    /**
     * 播放器
     */
    private MediaPlayer mediaplayer;
    //顺序播放
    public static final int REPEAT_ORDER=1;
    //单曲循环
    public static final  int REPEAT_SINGLE=2;
    //全部循环
    public static final int REPEAT_ALL=3;
    private int playmode=REPEAT_ORDER;


    @Override
    public IBinder onBind(Intent intent) {
        return stub;
    }

    @Override
    public void onCreate() {
        super.onCreate();
      getData();

    }



    private void getData() {


        new Thread(){
            @Override
            public void run() {
                super.run();

                mediaItems = new ArrayList<MediaItem>();
                ContentResolver contentResolver = getContentResolver();
                Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                String[] objects = {
                        MediaStore.Audio.Media.DISPLAY_NAME,//在Sdcard显示的名称
                        MediaStore.Audio.Media.DURATION,//视频的长度
                        MediaStore.Audio.Media.SIZE,//视频文件大小
                        MediaStore.Audio.Media.DATA,//视频的绝对地址
                        MediaStore.Audio.Media.ARTIST//艺术家
                };
                Cursor cursor =  contentResolver.query(uri, objects, null, null, null);
                if(cursor != null){
                    while (cursor.moveToNext()){

                        MediaItem mediaItem = new MediaItem();
                        String name = cursor.getString(0);
                        mediaItem.setName(name);

                        long duration = cursor.getLong(1);
                        mediaItem.setDuration(duration);

                        long size = cursor.getLong(2);
                        mediaItem.setSize(size);

                        String data = cursor.getString(3);
                        mediaItem.setData(data);

                        String artist = cursor.getString(4);
                        mediaItem.setArtist(artist);

                        //把视频添加到列表中
                        mediaItems.add(mediaItem);
                    }


                    cursor.close();
                }



            }
        }.start();
    }

    /**
     * 根据位置打开音乐
     * @param position
     */
    private void openAudio(int position){
        this.position = position;

        if(mediaItems != null && mediaItems.size() >0){

            mediaItem = mediaItems.get(position);

            //把上一次或者正在播放的给释放掉
            if(mediaplayer != null){
                mediaplayer.reset();
                mediaplayer.release();
                mediaplayer = null;
            }

            try {
                mediaplayer = new MediaPlayer();
                //设置准备好的监听
                mediaplayer.setOnPreparedListener(new MyOnPreparedListener());
                mediaplayer.setOnErrorListener(new MyOnErrorListener());
                mediaplayer.setOnCompletionListener(new MyOnCompletionListener());
                mediaplayer.setDataSource(mediaItem.getData());
                mediaplayer.prepareAsync();//本地资源和网络资源都行
//                mediaplayer.prepare();//本地资源

                if (playmode == MediaService.REPEAT_SINGLE) {
                    mediaplayer.setLooping(true);
                } else {
                    mediaplayer.setLooping(false);
                }


            } catch (IOException e) {
                e.printStackTrace();
            }


        }else{
            //数据还没有加载好
            Toast.makeText(MediaService.this, "数据还没有加载好呢！", Toast.LENGTH_SHORT).show();
        }

    }

    class MyOnCompletionListener implements MediaPlayer.OnCompletionListener {

        @Override
        public void onCompletion(MediaPlayer mp) {
            next();

        }
    }

    class MyOnErrorListener implements MediaPlayer.OnErrorListener {

        @Override
        public boolean onError(MediaPlayer mp, int what, int extra) {
            next();
            return true;
        }
    }

    class MyOnPreparedListener implements MediaPlayer.OnPreparedListener {

        @Override
        public void onPrepared(MediaPlayer mp) {
            start();
            notifyChange(OPEN_AUDIO);

        }
    }

    /**
     * 根据不同的动作发广播
     * @param openAudio
     */

    private void notifyChange(String openAudio) {
        Intent intent=new Intent();
        intent.setAction(openAudio);
        sendBroadcast(intent);
    }
    private boolean isPlaying() {
        return mediaplayer.isPlaying();
    }


    //通知服务
    private NotificationManager manager;

    /**
     * 播放音乐
     */
    private void start(){
        mediaplayer.start();
        //弹出通知-点击的时候进入音乐播放器页面
        manager= (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        Intent intent = new Intent(this, AudioPlayer.class);
        intent.putExtra("Notification",true);//从状态栏进入音乐播放页面
        PendingIntent pi = PendingIntent.getActivity(this,0,intent,PendingIntent.FLAG_UPDATE_CURRENT);
        Notification.Builder botification = new Notification.Builder(this);
                botification.setSmallIcon(R.drawable.notification_music_playing)
                .setContentTitle("321音乐")
                .setContentText("正在播放:"+getName())
                .setContentIntent(pi);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            NotificationChannel channel = new NotificationChannel("1","my_channel", NotificationManager.IMPORTANCE_DEFAULT);
            channel.enableLights(true);
            channel.setLightColor(Color.green(1));
            channel.setShowBadge(true);
            manager.createNotificationChannel(channel);
            botification.setChannelId("1");
        }
        Notification n=botification.build();


        n.flags = Notification.FLAG_ONGOING_EVENT;//点击不消失
        manager.notify(1,n);


    }

    /**
     * 暂停音乐
     */
    private void pause(){
        mediaplayer.pause();
        manager.cancel(1);
    }

    /**
     * 下一首
     */
    private void next(){
        setNextPosition();
        openNextPosition();
    }
    private void openNextPosition() {
        int playmode = getPlaymode();


        if (playmode == MediaService.REPEAT_ORDER) {

            if (position < mediaItems.size()) {
                openAudio(position);
            }else{
                position = mediaItems.size()-1;
            }

        } else if (playmode == MediaService.REPEAT_SINGLE) {

            if (position < mediaItems.size()) {
                openAudio(position);


            }

        } else if (playmode == MediaService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position < mediaItems.size()) {
                openAudio(position);
            }else{
                position = mediaItems.size()-1;
            }
        }

    }

    private void setNextPosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {
            position++;

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            position++;
        } else if (playmode == MediaService.REPEAT_ALL) {
            position++;
            if (position > mediaItems.size() - 1) {
                position = 0;
            }
        } else {
            position++;
        }

    }




    /**
     * 上一首
     */
    private void pre(){
        setPrePosition();
        openPrePosition();
    }
    private void openPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {

            if (position >= 0) {
                openAudio(position);
            }else{
                position = 0;
            }

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            if (position >= 0) {
                openAudio(position);

            }

        } else if (playmode == MediaService.REPEAT_ALL) {
            openAudio(position);
        } else {
            if (position >= 0) {
                openAudio(position);
            }else{
                position = 0;
            }
        }

    }

    private void setPrePosition() {
        int playmode = getPlaymode();

        if (playmode == MediaService.REPEAT_ORDER) {
            position--;

        } else if (playmode == MediaService.REPEAT_SINGLE) {
            position--;
        } else if (playmode == MediaService.REPEAT_ALL) {
            position--;
            if (position < 0) {
                position = mediaItems.size() - 1;
            }
        } else {
            position--;
        }

    }

    /**
     * 得到播放模式
     * @return
     */
    private int getPlaymode(){
        return  playmode;
    }

    /**
     * 设置播放模式
     * @param playmode
     */
    private void setPlaymode(int playmode){
        this.playmode=playmode;
        if (playmode == MediaService.REPEAT_SINGLE) {
            mediaplayer.setLooping(true);
        } else {
            mediaplayer.setLooping(false);
        }

    }

    /**
     * 得到当前播放进度
     * @return
     */
    private int getCurrentPosition(){
        return  mediaplayer.getCurrentPosition();
    }

    /**
     * 得到当前的总时长
     * @return
     */
    private int getDuration(){
        return  mediaplayer.getDuration();
    }


    /**
     * 得到歌曲的名称
     * @return
     */
    private String getName(){
        return mediaItem.getName();
    }


    /**
     * 得到演唱者
     * @return
     */
    private String getArtist(){
        return mediaItem.getArtist();
    }

    /**
     * 音频的拖动
     * @param seekto
     */
    private void seekTo(int seekto){
        mediaplayer.seekTo(seekto);
    }

}
