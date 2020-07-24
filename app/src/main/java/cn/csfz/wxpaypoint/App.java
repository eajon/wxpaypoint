package cn.csfz.wxpaypoint;

import android.app.Application;
import android.app.job.JobInfo;
import android.app.job.JobScheduler;
import android.content.ComponentName;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import com.danikula.videocache.HttpProxyCacheServer;
import com.github.eajon.RxConfig;
import com.github.eajon.RxHttp;
import com.github.eajon.util.LoggerUtils;
import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.sunfusheng.daemon.DaemonHolder;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.csfz.wxpaypoint.activity.CloseDoorActivity;
import cn.csfz.wxpaypoint.activity.OpenDoorActivity;
import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.util.ActivityCollector;
import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.AppUtils;
import cn.eajon.tool.DeviceUtils;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class App extends Application implements Thread.UncaughtExceptionHandler {

    public static HubConnection hubConnection;
    private static Application self;

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
        Thread.setDefaultUncaughtExceptionHandler(this);
        UMConfigure.init(App.this, "5f1a8825d62dd10bc71bda16", "csfz", UMConfigure.DEVICE_TYPE_PHONE, null);
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
                .log(!BuildConfig.PROD, "load");
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {

            @Override
            public void response(Map map) throws RemoteException {
                LogUtils.d(map.toString());
            }
        });
        DaemonHolder.init(this, HeartBeatService.class);



    }

    public static Application getContext() {
        if (self != null) {
            return self;
        }
        return null;
    }

    private HttpProxyCacheServer proxy;

    public static HttpProxyCacheServer getProxy(Context context) {
        App app = (App) context.getApplicationContext();
        return app.proxy == null ? (app.proxy = app.newProxy()) : app.proxy;
    }

    public static HubConnection getHub() {
        if(hubConnection ==null)
        {
            synchronized (App.class) {
                if (hubConnection == null) {
                    String sn = Utils.getDeviceSN();
                    hubConnection = HubConnectionBuilder.create("http://websocket.vendor.cxwos.com/websocket/MachineHub?userId=" + sn + "&machineId=" + sn).withTransport(TransportEnum.LONG_POLLING).build();
                }
            }
        }
        return hubConnection;
    }

    private HttpProxyCacheServer newProxy() {
        return new HttpProxyCacheServer.Builder(self)
                .maxCacheSize(1024 * 1024 * 1024)       // 1 Gb for cache
                .build();
    }

    @Override
    public void uncaughtException(Thread thread, Throwable ex) {
        Utils.restartAPP(self);
        System.exit(0);
    }


}
