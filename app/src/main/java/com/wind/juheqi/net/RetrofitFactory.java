package com.wind.juheqi.net;

import retrofit2.Retrofit;

public class RetrofitFactory {
    private static Retrofit sRetrofit;
    // 创建网络请求Observable
    public static RetrofitService createRequest() {
        return getRetrofit().create(RetrofitService.class);
    }

    // 配置Retrofit
    private synchronized static Retrofit getRetrofit() {
        if (sRetrofit == null) {
            sRetrofit = new Retrofit.Builder()
                    .baseUrl(Url.FIDDLER_BASE_QQ_URL) // 对应服务端的host

                    .build();
        }
        return sRetrofit;
    }

}
