package com.wind.juheqi.net;

import com.wind.juheqi.domain.SearchSong;

import java.util.Observable;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.Response;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface RetrofitService {
    @GET(Url.SEARCH_SONG)
    Call<ResponseBody> search(@Query("w") String search, @Query("p")int offset);
}
