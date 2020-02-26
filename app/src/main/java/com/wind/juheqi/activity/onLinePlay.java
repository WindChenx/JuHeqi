package com.wind.juheqi.activity;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.AnimationDrawable;
import android.media.MediaPlayer;
import android.nfc.Tag;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.wind.juheqi.IMediaService;
import com.wind.juheqi.R;
import com.wind.juheqi.domain.Song;
import com.wind.juheqi.service.MediaService;
import com.wind.juheqi.uitls.Utils;

import java.util.ArrayList;

import io.vov.vitamio.utils.Log;

public class onLinePlay extends Activity implements View.OnClickListener{
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

    private Utils utils;
    private Song song;
    private ArrayList<Song> songs;
    private MediaPlayer mediaPlayer=new MediaPlayer();
    @Override
    protected void onCreate( Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        findViews();
        getData();


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


        btnAudioStartPause.setOnClickListener(this);



    }

    @Override
    public void onClick(View v) {
        if ( v == btnAudioPlaymode ) {


        } else if ( v == btnAudioPre ) {
            if(mediaService!=null){
                try {
                    mediaService.pre();
                } catch (RemoteException e) {
                    e.printStackTrace();
                }
            }

        } else if ( v == btnAudioStartPause ) {



        mediaPlayer.start();

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

    private void    getData(){
        songs= (ArrayList<Song>) getIntent().getSerializableExtra("searchsongList");
        position=getIntent().getIntExtra("position",0);
        setViewData();

    }


    private void setViewData() {
        try {
            tvName.setText(song.getSongName());
            tvArtist.setText(song.getSinger());
            mediaPlayer.setDataSource(song.getUrl());
            Log.d("Tag" ,song.getUrl());



        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
