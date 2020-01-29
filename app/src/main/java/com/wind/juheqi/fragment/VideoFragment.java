package com.wind.juheqi.fragment;


import android.Manifest;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Message;
import android.provider.MediaStore;
import android.text.format.Formatter;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.wind.juheqi.R;

import com.wind.juheqi.activity.SystemVideoPlayer;
import com.wind.juheqi.domain.MediaItem;

import com.wind.juheqi.uitls.Utils;

import java.util.ArrayList;


public class VideoFragment extends BaseFragment {

    private ListView lv_video_pager;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mediaItems;
    private Utils utils;





    @Override

    public View initView() {

        utils=new Utils();

        View view=View.inflate(context,R.layout.video_pager,null);
        lv_video_pager =  view.findViewById(R.id.lv_video_pager);
        tv_nomedia = view.findViewById(R.id.tv_nomedia);
        pb_loading =  view.findViewById(R.id.pb_loading);
        lv_video_pager.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

//传单个视频地址
//                Intent intent=new Intent(context,SystemVideoPlayer.class);
//                intent.setDataAndType(Uri.parse(mediaItem.getData()),"video/*");
                //传视频列表
                Intent intent=new Intent(context,SystemVideoPlayer.class);

                Bundle bundle=new Bundle();
                bundle.putSerializable("videoList",mediaItems);
                intent.putExtras(bundle);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        return view;

    }

    @Override
    public void initData() {
        Log.d("tag","本地视频初始化");

       getData();
    }

    private android.os.Handler handler=new android.os.Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if(mediaItems!=null&&mediaItems.size()>0){
                tv_nomedia.setVisibility(View.GONE);
                pb_loading.setVisibility(View.GONE);

                lv_video_pager.setAdapter(new VideoAdapter());
            }else {
                tv_nomedia.setVisibility(View.VISIBLE);
                pb_loading.setVisibility(View.GONE);
            }
        }
    };
    class VideoAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return mediaItems.size();
        }

        @Override
        public Object getItem(int position) {
            return null;
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
          ViewHolder viewHolder;
            if(convertView==null){//复用contentView，避免每次获取item时都重新加载一遍布局文件
                convertView=View.inflate(context,R.layout.item_video_pager,null);
                viewHolder=new ViewHolder();
                viewHolder.tv_name=convertView.findViewById(R.id.tv_name);
                viewHolder.tv_duration=convertView.findViewById(R.id.tv_duration);
                viewHolder.tv_size=convertView.findViewById(R.id.tv_size);
                convertView.setTag(viewHolder);
            }else {
                viewHolder= (ViewHolder) convertView.getTag();
            }
            MediaItem mediaItem=mediaItems.get(position);
            viewHolder.tv_name.setText(mediaItem.getName());
            viewHolder.tv_size.setText(Formatter.formatFileSize(context,mediaItem.getSize()));
            viewHolder.tv_duration.setText(utils.stringForTime((int)mediaItem.getDuration()));

            return convertView;
        }
    }

    static class ViewHolder{
        TextView tv_name;
        TextView tv_duration;
        TextView tv_size;
    }


    private void getData() {
        new Thread(){
            @Override
            public void run() {
                super.run();
isGrantExternalRW((Activity) context);
                mediaItems=new ArrayList<>();

                ContentResolver contentResolver=context.getContentResolver();
                Uri uri=MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                String[] objects= {
                        MediaStore.Video.Media.DISPLAY_NAME,//在sdcard显示的名称
                        MediaStore.Video.Media.DURATION,//视频的长度
                        MediaStore.Video.Media.SIZE,//视频文件大小
                        MediaStore.Video.Media.DATA//视频的绝对地址
                };

                Cursor cursor=contentResolver.query(uri,objects,null,null,null);
                if(cursor!=null){
                    while (cursor.moveToNext()){
                        MediaItem mediaItem=new MediaItem();
                        String name=cursor.getString(0);
                        mediaItem.setName(name);
                        long duration=cursor.getLong(1);
                        mediaItem.setDuration(duration);
                        long size=cursor.getLong(2);
                        mediaItem.setSize(size);
                        String data=cursor.getString(3);
                        mediaItem.setData(data);
                        mediaItems.add(mediaItem);

                    }
                    cursor.close();
                }
//                ScannerAnsyTask ansyTask=new ScannerAnsyTask();
//                ansyTask.execute();
                handler.sendEmptyMessage(0);

            }
        }.start();
    }

    public static boolean isGrantExternalRW(Activity activity) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && activity.checkSelfPermission(
                Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            activity.requestPermissions(new String[]{
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE
            }, 1);
            return false;
        }
        return true;
    }

}

