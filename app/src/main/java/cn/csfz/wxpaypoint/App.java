package cn.csfz.wxpaypoint;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Build;
import android.os.RemoteException;

import androidx.annotation.RequiresApi;

import com.danikula.videocache.HttpProxyCacheServer;
import com.github.eajon.RxHttp;
import com.github.eajon.util.LoggerUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.sunfusheng.daemon.DaemonHolder;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLSocketFactory;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.KeyManagementException;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;

import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.LogUtils;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;

public class App extends Application implements Thread.UncaughtExceptionHandler {

    public static HubConnection hubConnection;
    private static Application self;



    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    public void onCreate() {
        super.onCreate();
        self = this;
//        Thread.setDefaultUncaughtExceptionHandler(this);
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
                .writeTimeout(60, TimeUnit.SECONDS)
                .sslSocketFactory(sslSocketFactory(),x509TrustManager());

        RxHttp.getConfig()
                .baseUrl(BuildConfig.SERVER_URL)
                .okHttpClient(httpClient)
                .rxCache(new File(getExternalCacheDir(), "load"))
                .log(!BuildConfig.PROD, "load");
        DaemonHolder.init(this, HeartBeatService.class);

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
                    hubConnection = HubConnectionBuilder.create("http://websocket.vendor.cxwos.com/websocket/MachineHub?userId=" + sn + "&machineId=" + sn).build();

                }
            }
        }
        return hubConnection;
    }

    public static void resetHub(){
        hubConnection.stop();
        hubConnection =null;
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

    public X509TrustManager x509TrustManager() {
        return new X509TrustManager() {
            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkClientTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @SuppressLint("TrustAllX509TrustManager")
            @Override
            public void checkServerTrusted(X509Certificate[] x509Certificates, String s) throws CertificateException {
            }

            @Override
            public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                return new X509Certificate[0];
            }
        };
    }


    public SSLSocketFactory sslSocketFactory() {
        try {
            //信任任何链接
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, new TrustManager[]{x509TrustManager()}, new SecureRandom());
            return sslContext.getSocketFactory();
        } catch (NoSuchAlgorithmException | KeyManagementException e) {
            e.printStackTrace();
        }
        return null;
    }


}
