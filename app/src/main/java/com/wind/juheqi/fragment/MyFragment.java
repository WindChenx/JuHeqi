package com.wind.juheqi.fragment;


import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.wind.juheqi.R;
import com.wind.juheqi.view.MyView;


public class MyFragment extends BaseFragment implements MyView.OnRootClickListener,MyView.OnArrowClickListener {


    LinearLayout llRoot;
    @Override

    public View initView() {
        View view=View.inflate(context,R.layout.my_fragment,null);
        llRoot = view.findViewById(R.id.ll_root);

        //icon + 文字 + 箭头
        llRoot.addView(new MyView(context)
                .init(R.mipmap.ic_launcher, "版本")
                .setOnRootClickListener(this, 1));

        return view;
    }


    @Override
    public void initData() {


    }


    @Override
    public void onRootClick(View view) {

    }

    @Override
    public void onArrowClick(View view) {

    }
}

