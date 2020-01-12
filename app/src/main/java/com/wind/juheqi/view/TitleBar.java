package com.wind.juheqi.view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.LinearLayout;

import com.wind.juheqi.R;

public class TitleBar extends LinearLayout {
    private final Context context;
    private View search;
    private View game;
    private View iv_history;
    public TitleBar(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.context=context;
    }

    @Override
    protected void onFinishInflate() {
        super.onFinishInflate();
        search=getChildAt(1);
        game=getChildAt(2);
        iv_history=getChildAt(3);
        MyOnClickListener myOnClickListener=new MyOnClickListener();
        search.setOnClickListener(myOnClickListener);
        game.setOnClickListener(myOnClickListener);
        iv_history.setOnClickListener(myOnClickListener);

    }

    private class MyOnClickListener implements OnClickListener{
        @Override
        public void onClick(View v) {
            switch (v.getId()){
                case R.id.tv_seache:
                    break;
                case R.id.rl_game:
                    break;
                case R.id.iv_history:
                    break;
            }

        }
    }
}
