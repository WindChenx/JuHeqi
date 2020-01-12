package com.wind.juheqi.fragment;


import android.view.View;
import android.widget.TextView;


public class NetVideoFragment extends BaseFragment {

    private TextView textView;
    @Override

    public View initView() {
        textView=new TextView(context);

        return textView;
    }


    @Override
    public void initData() {
        textView.setText("网络");

    }


}

