package com.wind.juheqi.uitls;

import android.content.Context;
import android.content.SharedPreferences;

public class CacheUtils {
    //保存历史进度
    public static void putInt(Context context, String data, int currentPosition) {
        SharedPreferences sp=context.getSharedPreferences("WIND",Context.MODE_PRIVATE);
        sp.edit().putInt(data,currentPosition).commit();
    }

    //得到播放历史进度
    public static int getInt(Context context,String data){
        SharedPreferences sp=context.getSharedPreferences("WIND",Context.MODE_PRIVATE);
        return sp.getInt(data,0);
    }

//保存网络请求
    public static void putString(Context context, String url, String json) {
        SharedPreferences sp=context.getSharedPreferences("WIND",Context.MODE_PRIVATE);
        sp.edit().putString(url,json).commit();
    }
//获取历史网络请求
    public static String getString(Context context, String url) {
        SharedPreferences sp=context.getSharedPreferences("WIND",Context.MODE_PRIVATE);
        return sp.getString(url,"");
    }
}
