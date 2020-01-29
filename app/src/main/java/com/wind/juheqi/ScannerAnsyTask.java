package com.wind.juheqi;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.wind.juheqi.domain.MediaItem;

import java.io.File;
import java.io.FileFilter;
import java.util.ArrayList;
import java.util.List;

public class ScannerAnsyTask extends AsyncTask<Void,Integer,List<MediaItem>> {
    private List<MediaItem> MediaItems=new ArrayList<MediaItem>();
    @Override
    protected List<MediaItem> doInBackground(Void... params) {
        MediaItems=getmediaItemFile(MediaItems, Environment.getExternalStorageDirectory());
        MediaItems=filtermediaItem(MediaItems);
        Log.i("tga","最后的大小"+MediaItems.size());
        return MediaItems;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
    }

    @Override
    protected void onPostExecute(List<MediaItem> MediaItems) {
        super.onPostExecute(MediaItems);
    }

    /**
     * 获取视频文件
     * @param list
     * @param file
     * @return
     */
    private List<MediaItem> getmediaItemFile(final List<MediaItem> list, File file) {

        file.listFiles(new FileFilter() {

            @Override
            public boolean accept(File file) {

                String name = file.getName();

                int i = name.indexOf('.');
                if (i != -1) {
                    name = name.substring(i);
                    if (name.equalsIgnoreCase(".mp4")
                            || name.equalsIgnoreCase(".3gp")
                            || name.equalsIgnoreCase(".wmv")
                            || name.equalsIgnoreCase(".ts")
                            || name.equalsIgnoreCase(".rmvb")
                            || name.equalsIgnoreCase(".mov")
                            || name.equalsIgnoreCase(".m4v")
                            || name.equalsIgnoreCase(".avi")
                            || name.equalsIgnoreCase(".m3u8")
                            || name.equalsIgnoreCase(".3gpp")
                            || name.equalsIgnoreCase(".3gpp2")
                            || name.equalsIgnoreCase(".mkv")
                            || name.equalsIgnoreCase(".flv")
                            || name.equalsIgnoreCase(".divx")
                            || name.equalsIgnoreCase(".f4v")
                            || name.equalsIgnoreCase(".rm")
                            || name.equalsIgnoreCase(".asf")
                            || name.equalsIgnoreCase(".ram")
                            || name.equalsIgnoreCase(".mpg")
                            || name.equalsIgnoreCase(".v8")
                            || name.equalsIgnoreCase(".swf")
                            || name.equalsIgnoreCase(".m2v")
                            || name.equalsIgnoreCase(".asx")
                            || name.equalsIgnoreCase(".ra")
                            || name.equalsIgnoreCase(".ndivx")
                            || name.equalsIgnoreCase(".xvid")) {
                        MediaItem mediaItem = new MediaItem();
                        file.getUsableSpace();
                        mediaItem.setName(file.getName());

                        mediaItem.setData(file.getAbsolutePath());
                        Log.i("tga","name"+mediaItem.getData());
                        list.add(mediaItem);
                        return true;
                    }
                    //判断是不是目录
                } else if (file.isDirectory()) {
                    getmediaItemFile(list, file);
                }
                return false;
            }
        });

        return list;
    }

    /**10M=10485760 b,小于10m的过滤掉
     * 过滤视频文件
     * @param MediaItems
     * @return
     */
    private List<MediaItem> filtermediaItem(List<MediaItem> MediaItems){
        List<MediaItem> mediaItems=new ArrayList<MediaItem>();
        for(MediaItem MediaItem:MediaItems){
            File f=new File(MediaItem.getData());
            if(f.exists()&&f.isFile()&&f.length()>10485760){
                mediaItems.add(MediaItem);
                Log.i("TGA","文件大小"+f.length());
            }else {
                Log.i("TGA","文件太小或者不存在");
            }
        }
        return mediaItems;
    }
}
