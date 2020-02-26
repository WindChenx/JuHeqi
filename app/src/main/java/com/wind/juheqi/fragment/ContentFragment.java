package com.wind.juheqi.fragment;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.wind.juheqi.R;
import com.wind.juheqi.activity.AudioPlayer;
import com.wind.juheqi.activity.onLinePlay;
import com.wind.juheqi.domain.SearchSong;
import com.wind.juheqi.domain.Song;
import com.wind.juheqi.net.RetrofitFactory;
import com.wind.juheqi.net.RetrofitService;
import com.wind.juheqi.view.MyListView;

import org.greenrobot.eventbus.EventBus;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ContentFragment extends BaseFragment {
    private ViewPager viewPager;
    private Bundle bundle;
//    private TableLayout tableLayout;
    private ListView myListView;
    private String search;
    private ArrayList<Song> searchsongs=new ArrayList<>();


    @Override
    public View initView() {
        View view=View.inflate(context,R.layout.searchsong_list,null);
        myListView=view.findViewById(R.id.searchsong_list);
        bundle=getArguments();
        if(bundle!=null){
            search=bundle.getString("SEARCH");
        }
        viewPager=view.findViewById(R.id.page);
//        tableLayout=view.findViewById(R.id.tab_layout);
        myListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                EventBus.getDefault().post(searchsongs.get(position).getUrl());
                Intent intent = new Intent(context,onLinePlay.class);

                Bundle bundle=new Bundle();
                bundle.putSerializable("searchsongList",searchsongs);
                intent.putExtras(bundle);
                intent.putExtra("position",position);
                context.startActivity(intent);
            }
        });
        return view;
    }

    @Override
    public void initData() {
        getData();

    }


    class searchsongAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return searchsongs.size();
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
            ContentFragment.ViewHolder viewHolder;
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.search_song, null);
                viewHolder =  new ViewHolder();
                viewHolder.singname=  convertView.findViewById(R.id.search_name);
                viewHolder.singer=  convertView.findViewById(R.id.search_singer);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder)convertView.getTag();
            }
            Song  searchSong=searchsongs.get(position);
            viewHolder.singname.setText(searchSong.getSongName());
            viewHolder.singer.setText(searchSong.getSinger());
            return convertView;
        }


    }


    static  class ViewHolder{
        TextView singname;
        TextView singer;
    }
    private void getData() {
       RetrofitService retrofitService= RetrofitFactory.createRequest();
        Call<ResponseBody> call=retrofitService.search(search,1);
        call.enqueue(new Callback<ResponseBody>() {
            @Override
            public void onResponse(Call<ResponseBody> call, Response<ResponseBody> response) {
                if(response.body()!=null){
                    String result= null;
                    try {
                        result = response.body().string();
                        parseJson(result);
                        Log.d("Tag",result);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                }else {
                    Log.d("Tag","联网成功，但是数据错误");
                }
            }

            @Override
            public void onFailure(Call<ResponseBody> call, Throwable t) {
                Log.d("Tag","联网失败");

            }
        });

    }

    private void parseJson( String result) {

        try {
          JSONObject  object = new JSONObject(result);
            JSONObject data=object.optJSONObject("data");
                JSONObject song=data.optJSONObject("song");
                JSONArray list=song.optJSONArray("list");
                for(int i=0;i<list.length();i++){
                    JSONObject jsonObject= (JSONObject) list.get(i);
                    if(jsonObject!=null){
                        Song searchsong=new Song();
                        String name=jsonObject.optString("songname");
                        searchsong.setSongName(name);

                        String url=jsonObject.optString("songurl");
                        searchsong.setUrl(url);
                        JSONArray singer=jsonObject.optJSONArray("singer");
                        JSONObject resu=singer.getJSONObject(0);
                            String singername=resu.optString("name");
                            searchsong.setSinger(singername);


                        searchsongs.add(searchsong);
                        Log.d("Tag","数据为"+searchsong.getSongName());



                }



            }

        } catch (JSONException e) {
            e.printStackTrace();
        }




        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //更新UI
                if (searchsongs != null && searchsongs.size() > 0) {
                    //


                    //设置适配器
                    myListView.setAdapter(new searchsongAdapter());

                } else {
                   Log.d("Tag","数据错误");
                }

            }
        });
    }

    public static Fragment newInstance(String seek){
        ContentFragment fragment=new ContentFragment();
        Bundle bundle=new Bundle();
        bundle.putString("SEARCH",seek);
        fragment.setArguments(bundle);
        return fragment;
    }
}
