package cn.rongcloud.im.net;

import java.util.concurrent.TimeUnit;

import cn.rongcloud.im.SealConst;
import cn.rongcloud.im.server.BaseAction;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.adapter.rxjava2.RxJava2CallAdapterFactory;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by will
 * on 2018/5/25.
 */
public class NetManager {
    private NetManager() {
    }

    private static class NetManagerHolder {
        private static OkHttpClient okHttpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor()
                        .setLevel(SealConst.DEBUG ? HttpLoggingInterceptor.Level.BODY : HttpLoggingInterceptor.Level.NONE))
                .addInterceptor(new MyInterceptor())
                .connectTimeout(60, TimeUnit.SECONDS) //超时时间
                .readTimeout(60, TimeUnit.SECONDS)
                .build();

        private static Retrofit INSTANCE = new Retrofit.Builder()
                .client(okHttpClient)
                .baseUrl(BaseAction.DOMAIN_IAMGE + BaseAction.API + "/")
                .addConverterFactory(GsonConverterFactory.create())
                .addCallAdapterFactory(RxJava2CallAdapterFactory.create())
                .build();
    }

    public static Retrofit getInstance() {
        return NetManagerHolder.INSTANCE;
    }

}
