package cn.csfz.wxpaypoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.hardware.display.DisplayManager;
import android.os.Build;
import android.os.Bundle;
import android.os.RemoteException;
import android.util.Log;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ImageView;

import androidx.annotation.RequiresApi;

import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.HttpObserver;
import com.google.gson.Gson;
import com.microsoft.signalr.HubConnectionState;
import com.sunfusheng.daemon.DaemonHolder;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.util.HashMap;
import java.util.Map;

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
import cn.csfz.wxpaypoint.util.ActivityCollector;
import cn.csfz.wxpaypoint.widget.QrCodeDialog;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.LogUtils;
import es.dmoral.toasty.Toasty;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
    @BindView(R.id.open_iv)
    ImageView openIv;

    AdPresentation adPresentation;


    QrCodeDialog qrCodeDialog ;
    int index;
    HubReceiver hubReceiver;

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
         hubReceiver = new HubReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction("openNotify");
        filter.addAction("closeNotify");
        filter.addAction("updateNotify");
        filter.addAction("updateAd");
        registerReceiver(hubReceiver, filter);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    protected void initLogic() {
        checkVersion();
        DaemonHolder.startService();
        getSecondDisplay();
        qrCodeDialog =new QrCodeDialog(self);

    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getSecondDisplay() {

        DisplayManager manager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = manager.getDisplays();
//        int width =displays[displays.length - 1].getWidth();
//        int height =displays[displays.length - 1].getHeight();
        // displays[0] 主屏
        // displays[1] 副屏
        adPresentation = new AdPresentation(MainActivity.this, displays[displays.length - 1]);
        adPresentation.getWindow().setType(
                WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
        if(!adPresentation.isShowing()) {
            adPresentation.show();
        }


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
        App.resetHub();
        Log.d("MainActivity",App.getHub().getConnectionState().name());

        if(qrCodeDialog!=null&&!qrCodeDialog.isShowing()) {
            qrCodeDialog.show();
        }

//        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
//            @Override
//            public void response(final Map info) throws RemoteException {
//                if (info == null) {
//                    new RuntimeException("调用返回为空").printStackTrace();
//                    return;
//                } else {
//                    String rawdata = (String) info.get("rawdata");
//                    getWxAuthInfo(rawdata);
//                }
//            }
//        });
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

    private void openDoor()
    {
        if (!ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
            ActivityUtils.toActivity(self, OpenDoorActivity.class);
        }
    }

    private void closeDoor()
    {
        if (ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
            ActivityCollector.getActivity(OpenDoorActivity.class).finish();
        }
        if (!ActivityCollector.isActivityExist(CloseDoorActivity.class)) {
            ActivityUtils.toActivity(self, CloseDoorActivity.class);
        }
    }

    private void updateNotify(String message)
    {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toasty.normal(self,"开始更新").show();
            }
        });
        VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
        updateApk(versionModel);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateAd(String message)
    {
        VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
        adPresentation.updateVideos(versionModel);
    }

    public class HubReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            String intentAction = intent.getAction();
            if (intentAction.equals("openNotify")) {
                     openDoor();
            }else if(intentAction.equals("closeNotify"))
            {
                closeDoor();

            }else if(intentAction.equals("updateNotify"))
            {
                String message =intent.getStringExtra("message");
                updateNotify(message);
            }else if(intentAction.equals("updateAd"))
            {
                String message =intent.getStringExtra("message");
                updateAd(message);
            }
        }
    }

    @Override
    protected void onDestroy() {
        adPresentation.dismiss();
        adPresentation =null;
        unregisterReceiver(hubReceiver);
        super.onDestroy();
    }
}
