package cn.csfz.wxpaypoint;

import android.app.Application;

import com.github.eajon.RxHttp;
import com.github.eajon.util.LoggerUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.umeng.commonsdk.UMConfigure;

import java.io.File;
import java.util.concurrent.TimeUnit;

import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import io.reactivex.Completable;
import io.reactivex.CompletableEmitter;
import io.reactivex.CompletableOnSubscribe;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Predicate;
import io.reactivex.schedulers.Schedulers;
import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
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

        HubConnection hubConnection = HubConnectionBuilder.create("http://websocket.vendor.cxwos.com/websocket/MachineHub?userId=0001&machineId=0001").withTransport(TransportEnum.LONG_POLLING).build();
        hubConnection.on("closeNotify", (message) -> {
            LogUtils.d(message);
        }, String.class);
        hubConnection.on("openNotify", (message) -> {
            LogUtils.d(message);
        }, String.class);
        hubConnection.on("updateNotify", (message) -> {
            AutoInstaller installer = new AutoInstaller.Builder(this)
                    .setMode(AutoInstaller.MODE.ROOT_ONLY)
                    .build();
            installer.installFromUrl(message);
        }, String.class);
//        Completable.create(emitter -> {
//            hubConnection.start().blockingAwait(10, TimeUnit.SECONDS);
//            emitter.onComplete();
//        }).subscribeOn(Schedulers.io())
//                .unsubscribeOn(Schedulers.io())
//                .observeOn(AndroidSchedulers.mainThread()).subscribe();

        Observable.interval(0, 10, TimeUnit.SECONDS).doOnNext(new Consumer<Long>() {
            @Override
            public void accept(Long integer) throws Exception {
                if (hubConnection.getConnectionState() == HubConnectionState.DISCONNECTED) {
                    try {
                        hubConnection.start().blockingAwait(5, TimeUnit.SECONDS);
                    } catch (Exception e) {

                    }
                }
            }
        }).compose(ObservableUtils.ioMain()).subscribe();


    }
}
