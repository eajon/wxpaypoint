package cn.csfz.wxpaypoint;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.ImageView;

import com.github.eajon.RxHttp;
import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.DownloadObserver;
import com.github.eajon.observer.HttpObserver;
import com.github.eajon.task.DownloadTask;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.csfz.wxpaypoint.activity.CloseDoorActivity;
import cn.csfz.wxpaypoint.activity.OpenDoorActivity;
import cn.csfz.wxpaypoint.api.WxApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.compont.SolveObserver;
import cn.csfz.wxpaypoint.model.AuthInfo;
import cn.csfz.wxpaypoint.model.BaseEntity;
import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.AppUtils;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
    @BindView(R.id.open_iv)
    ImageView openIv;

    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected int setContentId() {
        return R.layout.activity_main;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        checkVersion();
    }

    @Override
    protected void initLogic() {
        String sn = Utils.getDeviceSN();
        HubConnection hubConnection = HubConnectionBuilder.create("http://websocket.vendor.cxwos.com/websocket/MachineHub?userId=" + sn + "&machineId=" + sn).withTransport(TransportEnum.LONG_POLLING).build();
        hubConnection.on("closeNotify", (message) -> {
            LogUtils.d(message);
            runOnUiThread(() -> ActivityUtils.toActivity(self, CloseDoorActivity.class));
//            Toasty.normal(self, "关门了").show();
        }, String.class);
        hubConnection.on("openNotify", (message) -> {
            LogUtils.d(message);
            runOnUiThread(() -> ActivityUtils.toActivity(self, OpenDoorActivity.class));
//            Toasty.normal(self, "开门了").show();
        }, String.class);
        hubConnection.on("updateNotify", (message) -> {
//            Toasty.normal(self, "自动更新开始").show();
            LogUtils.d(message);


            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    DownloadTask downloadTask = new DownloadTask();
                    downloadTask.setName("update.apk");
                    new RxHttp.Builder().baseUrl("http://ytj.cxwos.com/").get("app-debug.apk").download(downloadTask).withDialog(self, "更新中...").build().request(new DownloadObserver() {
                        @Override
                        public void onPause(DownloadTask downloadTask) {
                            LogUtils.d("pausessssssssssssss");
                        }

                        @Override
                        public void onProgress(DownloadTask downloadTask) {
                            LogUtils.d("progress");
                        }

                        @Override
                        public void onSuccess(DownloadTask response) {
                            AutoInstaller installer = new AutoInstaller.Builder(self)
                                    .setMode(AutoInstaller.MODE.ROOT_ONLY)
                                    .build();
                            installer.install(response.getLocalDir() + File.separator + response.getName());
//                            AppUtils.installAppSilent(response.getLocalDir()+ File.separator+response.getName());
                        }

                        @Override
                        public void onError(ApiException exception) {
                            LogUtils.d("errorrrrrrr");
                        }
                    });
                }
            });

//            AutoInstaller installer = new AutoInstaller.Builder(this)
//                    .setMode(AutoInstaller.MODE.ROOT_ONLY)
//                    .build();
//            installer.installFromUrl(message);
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

    private void getWxAuthInfo(String rawdata) {

        WxApi.getWxAuthInfo(rawdata).request(new HttpObserver<BaseEntity<AuthInfo>>() {
            @Override
            public void onSuccess(BaseEntity<AuthInfo> response) {
                if (response.isSuccess()) {
                    Map<String, String> map = new HashMap<>();
                    map.put("appid", response.getData().getAppid());
                    map.put("mch_id", response.getData().getMch_id());
                    map.put("out_trade_no", response.getData().getOut_trade_no());
                    map.put("authinfo", response.getData().getAuthinfo());
                    map.put("payscore_out_request_no", response.getData().getPayscore_out_request_no());
                    map.put("payscore_service_id", response.getData().getPayscore_service_id());
                    WxPayFace.getInstance().getUserPayScoreStatus(map, new IWxPayfaceCallback() {
                        @Override
                        public void response(Map map) throws RemoteException {
                            LogUtils.d(map.toString());
                            WxApi.createOrder((String) map.get("face_sid"), (String) map.get("openid"), response.getData().getOut_trade_no()).request(new SolveObserver<BaseEntity>(self) {
                                @Override
                                public void onSolve(BaseEntity response) {
                                    if (response.isSuccess()) {
                                        Toasty.normal(self, "正在开门中,请稍后").show();
                                    }
                                }

                            });
                        }
                    });
                }
            }

            @Override
            public void onError(ApiException exception) {

            }
        });

    }

    private void checkVersion() {
        int codeversin = getVersion();
        Toasty.normal(self, codeversin + "");


////
//        VersionApi.getVersion().request(new SolveObserver<BaseEntity<VersionModel>>(self) {
//            @Override
//            public void onSolve(BaseEntity<VersionModel> response) {
//                VersionModel versionModel = response.getData();
////                if (versionModel.getVersion() > codeversin) {
//                    AutoInstaller installer = new AutoInstaller.Builder(self)
//                            .setMode(AutoInstaller.MODE.ROOT_ONLY)
//                            .build();
//                    installer.installFromUrl(versionModel.getUrl());
////                }
//            }

//        });

    }


    public int getVersion() {
        PackageInfo pkg;
        int versionCode = 0;
        try {
            pkg = self.getPackageManager().getPackageInfo(self.getApplication().getPackageName(), 0);
            versionCode = pkg.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @OnClick(R.id.open_iv)
    public void onViewClicked() {
        Toasty.success(self, getVersion()).show();
        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
            @Override
            public void response(final Map info) throws RemoteException {
                if (info == null) {
                    new RuntimeException("调用返回为空").printStackTrace();
                    return;
                } else {
                    String rawdata = (String) info.get("rawdata");
                    getWxAuthInfo(rawdata);
                }
            }
        });
    }
}
