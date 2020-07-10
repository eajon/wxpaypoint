package cn.csfz.wxpaypoint;

import android.app.Application;
import android.content.Context;
import android.os.RemoteException;

import com.github.eajon.RxHttp;
import com.github.eajon.util.LoggerUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.DeviceUtils;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class App extends Application {


    private static Application self;

    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        UMConfigure.init(App.this, "5ddb92b3570df3af0a0002de", "csfz", UMConfigure.DEVICE_TYPE_PHONE, null);
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
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {

            @Override
            public void response(Map map) throws RemoteException {
                LogUtils.d(map.toString());
            }
        });



    }

    public static Application getContext() {
        if (self != null) {
            return self;
        }
        return null;
    }
}
