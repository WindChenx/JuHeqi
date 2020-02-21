package com.wind.juheqi.fragment;



import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.wind.juheqi.R;
import com.wind.juheqi.activity.SystemVideoPlayer;
import com.wind.juheqi.domain.MediaItem;
import com.wind.juheqi.uitls.CacheUtils;
import com.wind.juheqi.view.PullToRefreshListView;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;

import io.vov.vitamio.utils.Log;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;


public class NetVideoFragment extends BaseFragment  {
    private PullToRefreshListView lv_video_pager;
    private TextView tv_nomedia;
    private ProgressBar pb_loading;
    private ArrayList<MediaItem> mediaItems=new ArrayList<>();

    final String url="http://api.m.mtime.cn/PageSubArea/TrailerList.api";

    private MyNetVideoAdapter adapter=new MyNetVideoAdapter();



    @Override

    public View initView() {


        View view=View.inflate(context,R.layout.net_video_pager,null);
        lv_video_pager =  view.findViewById(R.id.lv_video_pager);
        tv_nomedia = view.findViewById(R.id.tv_nomedia);
        pb_loading =  view.findViewById(R.id.pb_loading);
        lv_video_pager.setOnItemClickListener(new MyOnItemClickListener());
        lv_video_pager.setonRefreshListener(new PullToRefreshListView.OnRefreshListener(){

            @Override
            public void onRefresh() {
                new  RefreshAsync().execute();
            }
        });
        return view;
    }

    class RefreshAsync extends AsyncTask<String,Integer,String>{

        @Override
        protected String doInBackground(String... strings) {
            getDataFromNet();
            return null;
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);
            adapter.notifyDataSetChanged();
            lv_video_pager.onRefreshComplete();

        }
    }





    class MyOnItemClickListener implements AdapterView.OnItemClickListener {

        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            //传视频列表
            Intent intent = new Intent(context, SystemVideoPlayer.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable("videoList", mediaItems);
            intent.putExtras(bundle);
            intent.putExtra("position", position);
            context.startActivity(intent);
        }
    }





    @Override
    public void initData() {
        String saveJson=CacheUtils.getString(context,url);
        if(!TextUtils.isEmpty(saveJson)){
            processData(saveJson);
        }

        getDataFromNet();

    }

    private void getDataFromNet() {


                        OkHttpClient okHttpClient=new OkHttpClient();
                        final Request request=new Request.Builder()
                                .url(url)
                                .get()
                                .build();

                        Call call=okHttpClient.newCall(request);


                        call.enqueue(new Callback() {
                            @Override
                            public void onFailure(Call call, IOException e) {
                                Log.d("TAG","onFailure:");
                            }

                            @Override
                            public void onResponse(Call call, Response response) throws IOException {
                                Log.d("TAG","联网成功");

                                ResponseBody data=response.body();
                                String json=data.string();
                                CacheUtils.putString(context,url,json);
                               processData(json);

                            }
                        });

        }



    private void processData(final String json) {
        //解析数据：1.手动解析（用系统的接口） 2.第三方工具解析 gson和fastjson
        parseJson(json);
        this.getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {

                //更新UI
                if (mediaItems != null && mediaItems.size() > 0) {
                    //
                    tv_nomedia.setVisibility(View.GONE);

                    //设置适配器
                    lv_video_pager.setAdapter(adapter);

                } else {
                    tv_nomedia.setVisibility(View.VISIBLE);
                }
                pb_loading.setVisibility(View.GONE);
            }
        });






    }





    class MyNetVideoAdapter extends BaseAdapter{

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
            if (convertView == null) {
                convertView = View.inflate(context, R.layout.item_netvideo_pager, null);
                viewHolder = new ViewHolder();
                viewHolder.iv_icon =  convertView.findViewById(R.id.iv_icon);
                viewHolder.tv_name =  convertView.findViewById(R.id.tv_name);
                viewHolder.tv_desc =  convertView.findViewById(R.id.tv_desc);

                convertView.setTag(viewHolder);
            } else {
                viewHolder = (ViewHolder) convertView.getTag();
            }

            //根据位置得到对应的数据
            MediaItem mediaItem = mediaItems.get(position);
            viewHolder.tv_name.setText(mediaItem.getName());
            viewHolder.tv_desc.setText(mediaItem.getDesc());

            Glide.with(context).load(mediaItem.getImageUrl())
                    .diskCacheStrategy(DiskCacheStrategy.ALL)//图片的缓存
                    .placeholder(R.drawable.vedio_default)//加载过程中的图片
                    .error(R.drawable.vedio_default)//加载失败的时候显示的图片
                    .into(viewHolder.iv_icon);//请求成功后把图片设置到的控件

            return convertView;
        }
    }
    static class ViewHolder {
        ImageView iv_icon;
        TextView tv_name;
        TextView tv_desc;
    }

    private void parseJson(String json) {
        try {

            JSONObject object=new JSONObject(json);
            JSONArray jsonArray=object.optJSONArray("trailers");

                for(int i=0;i<jsonArray.length();i++){
                    JSONObject jsonObject= (JSONObject) jsonArray.get(i);
                    if (jsonObject != null) {

                        MediaItem mediaItem = new MediaItem();


                        String coverImg = jsonObject.getString("coverImg");
                        mediaItem.setImageUrl(coverImg);

                        String url = jsonObject.optString("url");
                        mediaItem.setData(url);

                        String movieName = jsonObject.optString("movieName");
                        mediaItem.setName(movieName);

                        String videoTitle = jsonObject.optString("videoTitle");
                        mediaItem.setDesc(videoTitle);

                        mediaItems.add(mediaItem);//添加到集合中--可以


                    }
                }


        } catch (JSONException e) {
            e.printStackTrace();
        }

    }


}

