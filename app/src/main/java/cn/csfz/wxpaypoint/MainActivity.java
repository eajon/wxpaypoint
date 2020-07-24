package cn.csfz.wxpaypoint;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.github.eajon.RxHttp;
import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.DownloadObserver;
import com.github.eajon.observer.HttpObserver;
import com.github.eajon.task.DownloadTask;
import com.google.gson.Gson;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.HubConnectionState;
import com.microsoft.signalr.TransportEnum;
import com.sunfusheng.daemon.DaemonHolder;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.csfz.wxpaypoint.activity.CloseDoorActivity;
import cn.csfz.wxpaypoint.activity.OpenDoorActivity;
import cn.csfz.wxpaypoint.api.VersionApi;
import cn.csfz.wxpaypoint.api.WxApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.compont.AdPresentation;
import cn.csfz.wxpaypoint.compont.SolveObserver;
import cn.csfz.wxpaypoint.model.AuthInfo;
import cn.csfz.wxpaypoint.model.BaseEntity;
import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.model.Video;
import cn.csfz.wxpaypoint.util.ActivityCollector;
import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import cn.eajon.tool.SPUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
    @BindView(R.id.open_iv)
    ImageView openIv;

    AdPresentation adPresentation;

    int index;

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
//        checkVersion();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void initLogic() {
        checkVersion();
        DaemonHolder.startService();
        getSecondDisplay();
        //for test secondDisplay
//        List<Video> videoPaths = new ArrayList<>();
//        Video video = new Video();
//        video.setDelay(10);
//        video.setType(1);
//        video.setImage("https://qmoss.blob.core.chinacloudapi.cn/ads/0262203af97d0be0843e69a5134b6fff.jpg");
//        video.setUrl("https://qmoss.blob.core.chinacloudapi.cn/ads/23ecd9d8ee137dbebd5a87e43c64432e.mp4");
//        videoPaths.add(video);
//        video = new Video();
//        video.setDelay(10);
//        video.setType(0);
//        video.setImage("https://qmoss.blob.core.chinacloudapi.cn/ads/8241f1f02a86902f37d6f2710d81965c.jpg");
//        video.setUrl("https://qmoss.blob.core.chinacloudapi.cn/ads/8241f1f02a86902f37d6f2710d81965c.mp4");
//        videoPaths.add(video);
//        VersionModel versionModel = new VersionModel();
//        versionModel.setAdVersion(5);
//        versionModel.setVideos(videoPaths);
//        updateAd(versionModel);


        App.getHub().on("closeNotify", (message) -> {
            LogUtils.d(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
                        ActivityCollector.getActivity(OpenDoorActivity.class).finish();
                    }
                    if (!ActivityCollector.isActivityExist(CloseDoorActivity.class)) {
                        ActivityUtils.toActivity(self, CloseDoorActivity.class);
                    }

                }
            });
//            Toasty.normal(self, "关门了").show();
        }, String.class);
        App.getHub().on("openNotify", (message) -> {
            LogUtils.d(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    if (!ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
                        ActivityUtils.toActivity(self, OpenDoorActivity.class);
                    }

                }
            });
//            Toasty.normal(self, "开门了").show();
        }, String.class);
        App.getHub().on("updateNotify", (message) -> {
//            Toasty.normal(self, "自动更新开始").show();
            LogUtils.d(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
                    updateApk(versionModel);
                }
            });

        }, String.class);
        App.getHub().on("updateAd", (message) -> {
//            Toasty.normal(self, "自动更新开始").show();
            LogUtils.d(message);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
                    updateAd(versionModel);
                }
            });

        }, String.class);
        if (App.getHub().getConnectionState() == HubConnectionState.DISCONNECTED) {
            try {
                App.getHub().start().blockingAwait();
            } catch (Exception e) {
                if (null != e.getMessage()) {
                    LogUtils.e(e.getMessage());
                }
            }
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getSecondDisplay() {

        DisplayManager manager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = manager.getDisplays();
//        int width =displays[displays.length - 1].getWidth();
//        int height =displays[displays.length - 1].getHeight();
        // displays[0] 主屏
        // displays[1] 副屏
        adPresentation = new AdPresentation(App.getContext(), displays[displays.length - 1]);
        adPresentation.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        adPresentation.show();


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
//                    Toasty.normal(self, "开始获取支付分").show();
                    index = 0;
                    WxPayFace.getInstance().getUserPayScoreStatus(map, new IWxPayfaceCallback() {
                        @Override
                        public void response(Map map) throws RemoteException {
                            index++;
//                            runOnUiThread(new Runnable() {
//                                @Override
//                                public void run() {
//                                    Toasty.normal(self, "支付分返回第" + index + "次").show();
//                                }
//                            });
                            if (map == null) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.error(self, "支付分异常").show();
                                    }
                                });
                                return;
                            }
                            if (map.get("return_code").equals("USER_CANCEL")) {
                                return;
                            }
                            if (map.get("return_code").equals("SCAN_PAYMENT")) {
                                runOnUiThread(new Runnable() {
                                    @Override
                                    public void run() {
                                        Toasty.normal(self, "暂未开通该流程").show();
                                    }
                                });
                                return;
                            }
                            if (map.containsKey("face_sid") && map.containsKey("openid")) {
                                WxApi.createOrder((String) map.get("face_sid"), (String) map.get("openid"), response.getData().getOut_trade_no()).request(new SolveObserver<BaseEntity>(self) {
                                    @Override
                                    public void onSolve(BaseEntity response) {
                                        if (response.isSuccess()) {
                                            if (!ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
                                                ActivityUtils.toActivity(self, OpenDoorActivity.class);
                                            }
                                        }
                                    }

                                });
                            } else {
                                Toasty.error(self, "系统错误").show();
                            }
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
        VersionApi.getVersion().request(new SolveObserver<BaseEntity<VersionModel>>(self) {
            @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
            @Override
            public void onSolve(BaseEntity<VersionModel> response) {
                updateApk(response.getData());
                updateAd(response.getData());
            }
        });
    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @OnClick(R.id.button)
    public void onViewClicked() {
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


    private void updateApk(VersionModel versionModel) {
        if (versionModel.getVersion() > getVersion()) {
            AutoInstaller installer = new AutoInstaller.Builder(self)
                    .setMode(AutoInstaller.MODE.ROOT_ONLY)
                    .build();
            installer.installFromUrl(versionModel.getUrl());
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateAd(VersionModel versionModel) {
        adPresentation.updateVideos(versionModel);
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
}
