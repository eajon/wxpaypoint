package cn.csfz.wxpaypoint;

import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.hardware.display.DisplayManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.RemoteException;
import android.text.TextUtils;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.FileProvider;

import com.bumptech.glide.Glide;
import com.google.gson.Gson;
import com.jakewharton.rxbinding3.view.RxView;
import com.sunfusheng.daemon.DaemonHolder;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;
import com.trello.rxlifecycle3.android.ActivityEvent;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
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
import cn.csfz.wxpaypoint.model.Order;
import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.util.ActivityCollector;
import cn.csfz.wxpaypoint.util.Utils;
import cn.csfz.wxpaypoint.widget.QrCodeDialog;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.AppUtils;
import cn.eajon.tool.FileUtils;
import cn.eajon.tool.ObservableUtils;
import cn.eajon.tool.StringUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.functions.Consumer;
import kotlin.Unit;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
    @BindView(R.id.open_iv)
    ImageView openIv;


    AdPresentation adPresentation;


    QrCodeDialog qrCodeDialog;
    HubReceiver hubReceiver;
    @BindView(R.id.button)
    TextView button;
    @BindView(R.id.quit_button)
    TextView quitButton;

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

        RxView.clicks(button).throttleFirst(2, TimeUnit.SECONDS).compose(ObservableUtils.lifeCycle(MainActivity.this, ActivityEvent.DESTROY)).subscribe(unit -> {
            if (qrCodeDialog != null && qrCodeDialog.isShowing()) {
                qrCodeDialog.dismiss();
                qrCodeDialog = null;
            }
            WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
                @Override
                public void response(final Map info) throws RemoteException {
                    if (info == null) {
                        Toasty.error(self, "人脸识别初始化失败，请检查设备完整性").show();
                        return;
                    } else if (info.containsKey("rawdata")) {
                        String rawdata = (String) info.get("rawdata");
                        getWxAuthInfo(rawdata);
                    } else {
                        Toasty.error(self, (String) info.get("return_msg")).show();
                    }
                }
            });
        });
        RxView.longClicks(quitButton).compose(ObservableUtils.lifeCycle(MainActivity.this, ActivityEvent.DESTROY)).subscribe(new Consumer<Unit>() {
            @Override
            public void accept(Unit unit) throws Exception {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_password, null);
                TextView cancel = dialogView.findViewById(R.id.choosepage_cancel);
                TextView sure = dialogView.findViewById(R.id.choosepage_sure);
                final EditText edittext = dialogView.findViewById(R.id.choosepage_edittext);
                final Dialog dialog = builder.create();
                dialog.show();
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = 800;
                dialogWindow.setAttributes(lp);
                dialog.setCanceledOnTouchOutside(true);
                dialogWindow.setContentView(dialogView);
                //使editext可以唤起软键盘
                dialogWindow.clearFlags(WindowManager.LayoutParams.FLAG_ALT_FOCUSABLE_IM);
                cancel.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        dialog.dismiss();
                    }
                });
                sure.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (edittext.getText().toString().equals("963214")) {
                            Intent paramIntent = new Intent("android.intent.action.MAIN");
                            paramIntent.setComponent(new ComponentName("android", "com.android.internal.app.ResolverActivity"));
                            paramIntent.addCategory("android.intent.category.DEFAULT");
                            paramIntent.addCategory("android.intent.category.HOME");
                            self.startActivity(paramIntent);

                        }
                        dialog.dismiss();
                    }
                });
            }
        });
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void getSecondDisplay() {

        DisplayManager manager = (DisplayManager) getSystemService(Context.DISPLAY_SERVICE);
        Display[] displays = manager.getDisplays();
        if (displays.length > 1) {
            adPresentation = new AdPresentation(MainActivity.this, displays[displays.length - 1]);
            adPresentation.getWindow().setType(
                    WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
            adPresentation.show();
        }


    }

    private void getWxAuthInfo(String rawdata) {

        WxApi.getWxAuthInfo(rawdata).request(new SolveObserver<BaseEntity<Object>>(self) {
            @Override
            public void onSolve(BaseEntity<Object> response) {
                if (response.isSuccess()) {
                    AuthInfo authInfo = new Gson().fromJson(new Gson().toJson(response.getData()), AuthInfo.class);
                    Map<String, String> map = new HashMap<>();
                    map.put("appid", authInfo.getAppid());
                    map.put("mch_id", authInfo.getMch_id());
                    map.put("out_trade_no", authInfo.getOut_trade_no());
                    map.put("authinfo", authInfo.getAuthinfo());
                    map.put("payscore_out_request_no", authInfo.getPayscore_out_request_no());
                    map.put("payscore_service_id", authInfo.getPayscore_service_id());
//                    Toasty.normal(self, "开始获取支付分").show();
                    WxPayFace.getInstance().getUserPayScoreStatus(map, new IWxPayfaceCallback() {
                        @Override
                        public void response(Map map) throws RemoteException {
//                            WxPayFace.getInstance().releaseWxpayface(self);
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
                                        qrCodeDialog = new QrCodeDialog(self);
                                        qrCodeDialog.show();
                                    }
                                });
                                return;
                            }
                            if (map.containsKey("face_sid") && map.containsKey("openid")) {
                                WxApi.createOrder((String) map.get("face_sid"), (String) map.get("openid"), authInfo.getOut_trade_no()).request(new SolveObserver<BaseEntity<Object>>(self) {
                                    @Override
                                    public void onSolve(BaseEntity<Object> response) {
                                        Order order = new Gson().fromJson(new Gson().toJson(response.getData()), Order.class);
                                        if (response.isSuccess()) {
                                            if (order.isOpen()) {
                                                if (!ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
                                                    ActivityUtils.toActivity(self, OpenDoorActivity.class);
                                                }
                                            } else {
                                                qrCodeDialog = new QrCodeDialog(self);
                                                qrCodeDialog.show();
                                                qrCodeDialog.setMessage(order.getMessage());
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
        });

    }

    private void checkVersion() {
        Observable.interval(1, TimeUnit.SECONDS)
                .filter(aLong -> {
                    return Utils.canPing();//符合条件后就不在发送
                }).take(1)
                .compose(ObservableUtils.ioMain())
                .compose(ObservableUtils.lifeCycle(MainActivity.this, ActivityEvent.DESTROY))
                .delay(5, TimeUnit.SECONDS)
                .subscribe(aLong -> VersionApi.getVersion().request(new SolveObserver<BaseEntity<Object>>(self) {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onSolve(BaseEntity<Object> response) {
                        VersionModel versionModel = new Gson().fromJson(new Gson().toJson(response.getData()), VersionModel.class);
                        if(!StringUtils.isEmpty(versionModel.getBgImg()))
                        {
                            Glide.with(self).load(versionModel.getBgImg()).into(openIv);
                        }
                        updateApk(versionModel);
                        updateAd(versionModel);
                    }
                }));

    }


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }


    private void updateApk(VersionModel versionModel) {
        if (versionModel.getVersion() > Utils.getVersion()) {
            if (AppUtils.isAppRoot()) {
                AutoInstaller installer = new AutoInstaller.Builder(self)
                        .setMode(AutoInstaller.MODE.ROOT_ONLY)
                        .build();
                installer.installFromUrl(versionModel.getUrl());
            } else {
                (new Thread(new Runnable() {
                    public void run() {
                        File file = Utils.downloadFile(versionModel.getUrl());
                        try {
                            AppUtils.installApp(self,file,"cn.csfz.paypoint.fileprovider");

//
                        } catch (Exception e) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(self, "更新失败" + e.getMessage()).show();
                                }
                            });
                        }
                    }
                })).start();

            }

        }
    }


    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateAd(VersionModel versionModel) {
        if (adPresentation != null) {
            adPresentation.updateVideos(versionModel);
        }
    }


    private void openDoor() {
        if (!ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
            ActivityUtils.toActivity(self, OpenDoorActivity.class);
        }
    }

    private void closeDoor() {
        if (ActivityCollector.isActivityExist(OpenDoorActivity.class)) {
            ActivityCollector.getActivity(OpenDoorActivity.class).finish();
        }
        if (!ActivityCollector.isActivityExist(CloseDoorActivity.class)) {
            ActivityUtils.toActivity(self, CloseDoorActivity.class);
        }

    }

    private void updateNotify(String message) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toasty.normal(self, "开始更新").show();
            }
        });
        VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
        updateApk(versionModel);
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateAd(String message) {
        VersionModel versionModel = new Gson().fromJson(message, VersionModel.class);
        updateAd(versionModel);
    }

    public class HubReceiver extends BroadcastReceiver {
        //必须要重载的方法，用来监听是否有广播发送
        @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
        @Override
        public void onReceive(Context context, Intent intent) {
            if (qrCodeDialog != null && qrCodeDialog.isShowing()) {
                qrCodeDialog.dismiss();
                qrCodeDialog = null;
            }
            String intentAction = intent.getAction();
            if (intentAction.equals("openNotify")) {
                openDoor();
            } else if (intentAction.equals("closeNotify")) {
                closeDoor();

            } else if (intentAction.equals("updateNotify")) {
                String message = intent.getStringExtra("message");
                updateNotify(message);
            } else if (intentAction.equals("updateAd")) {
                String message = intent.getStringExtra("message");
                updateAd(message);
            }
        }
    }

    @Override
    protected void onDestroy() {
        if (adPresentation != null) {
            adPresentation.dismiss();
            adPresentation = null;
        }
        if (qrCodeDialog != null && qrCodeDialog.isShowing()) {
            qrCodeDialog.dismiss();
            qrCodeDialog = null;
        }
        unregisterReceiver(hubReceiver);
        super.onDestroy();
    }
}
