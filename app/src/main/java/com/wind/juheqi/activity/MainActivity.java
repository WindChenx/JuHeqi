package com.wind.juheqi.activity;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.widget.RadioGroup;


import com.wind.juheqi.R;
import com.wind.juheqi.fragment.AudioFragment;
import com.wind.juheqi.fragment.BaseFragment;
import com.wind.juheqi.fragment.NetAudioFragment;
import com.wind.juheqi.fragment.NetVideoFragment;
import com.wind.juheqi.fragment.VideoFragment;

import java.util.ArrayList;

public class MainActivity extends FragmentActivity {
    private RadioGroup rg_main;


    private ArrayList<BaseFragment> baseFragments;
    private Fragment lastFragment;//上次的fragment
    private Activity context;

    /**
     * 页面对应的位置
     */
    private int position;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

        rg_main=findViewById(R.id.rg_main);

        
        initFragment();
        rg_main.setOnCheckedChangeListener(new MyOnCheckedChangeListener());
        rg_main.check(R.id.rb_video);


    }

    private void initFragment() {
        baseFragments=new ArrayList<>();
        baseFragments.add(new VideoFragment());
        baseFragments.add(new AudioFragment());
        baseFragments.add(new NetVideoFragment());
        baseFragments.add(new NetAudioFragment());
    }


    class MyOnCheckedChangeListener implements RadioGroup.OnCheckedChangeListener {

        @Override
        public void onCheckedChanged(RadioGroup group, int checkedId) {
            switch (checkedId){
                default://本地视频
                    position = 0;
                    break;
                case R.id.rb_audio://本地音乐
                    position = 1;
                    break;
                case R.id.rb_net_video://网络视频
                    position = 2;
                    break;
                case R.id.rb_net_audio://网络音乐
                    position = 3;
                    break;
            }

            BaseFragment to=getFragment();
            switchFragment(lastFragment,to);

        }
    }

    private void switchFragment(Fragment from,Fragment to) {
       if(from!=to){
           lastFragment=to;
           FragmentTransaction ft=getSupportFragmentManager().beginTransaction();
           if(!to.isAdded()){//判断有没有添加
               //to没有被添加，from隐藏
               if(from!=null){
                   ft.hide(from);
               }
               //添加to
               if(to!=null){
                   ft.add(R.id.fl_main,to).commit();
               }


           }else {
               //to已经被添加，from隐藏
               if(from!=null){
                   ft.hide(from);
               }
               //显示to
               if(to!=null){
                   ft.show(to).commit();
               }
           }

       }
    }



    private BaseFragment getFragment(){
        BaseFragment fragment=baseFragments.get(position);
        return fragment;
    }




}
