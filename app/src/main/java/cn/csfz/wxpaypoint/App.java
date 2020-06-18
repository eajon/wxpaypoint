package cn.csfz.wxpaypoint;

import android.app.Application;

import com.github.eajon.RxHttp;
import com.github.eajon.util.LoggerUtils;
import com.tencent.rtmp.TXLiveBase;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        UMConfigure.init(App.this, "5ddb92b3570df3af0a0002de", "csfz", UMConfigure.DEVICE_TYPE_PHONE, null);
        String licenceURL = "http://license.vod2.myqcloud.com/license/v1/6cd53f1181e4001c058c440b7f539ca4/TXLiveSDK.licence"; // 获取到的 licence url
        String licenceKey = "a5d390b47de6a943feaaf91e9b30b947"; // 获取到的 licence key
        TXLiveBase.getInstance().setLicence(this, licenceURL, licenceKey);
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder()
                .addNetworkInterceptor(new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger() {
                    @Override
                    public void log(String message) {
                        LoggerUtils.debug(message);
                    }
                })
                        .setLevel(HttpLoggingInterceptor.Level.BASIC))
                .connectTimeout(60, TimeUnit.SECONDS)
                .readTimeout(60, TimeUnit.SECONDS)
                .writeTimeout(60, TimeUnit.SECONDS);

        RxHttp.getConfig()
                .baseUrl(BuildConfig.SERVER_URL)
                .okHttpClient(httpClient)
                .rxCache(new File(getExternalCacheDir(), "load"))
                .log(true, "load");

    }
}
