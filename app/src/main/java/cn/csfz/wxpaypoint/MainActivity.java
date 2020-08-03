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

import com.google.gson.Gson;
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
import cn.csfz.wxpaypoint.widget.QrCodeDialog;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.AppUtils;
import cn.eajon.tool.FileUtils;
import cn.eajon.tool.ObservableUtils;
import cn.eajon.tool.Utils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
    @BindView(R.id.open_iv)
    ImageView openIv;


    AdPresentation adPresentation;


    QrCodeDialog qrCodeDialog;
    int index;
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
        quitButton.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View view) {
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                View dialogView = LayoutInflater.from(MainActivity.this).inflate(R.layout.dialog_password, null);
                TextView cancel = dialogView.findViewById(R.id.choosepage_cancel);
                TextView sure = dialogView.findViewById(R.id.choosepage_sure);
                final EditText edittext = dialogView.findViewById(R.id.choosepage_edittext);
                final Dialog dialog = builder.create();
                dialog.show();
                Window dialogWindow = dialog.getWindow();
                WindowManager.LayoutParams lp = dialogWindow.getAttributes();
                lp.width = 500;
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

                return true;
            }
        });
        checkVersion();
        DaemonHolder.startService();
        getSecondDisplay();
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
                    return canPing();//符合条件后就不在发送
                }).take(1)
                .compose(ObservableUtils.ioMain())
                .compose(ObservableUtils.lifeCycle(MainActivity.this, ActivityEvent.DESTROY))
                .delay(10, TimeUnit.SECONDS)
                .subscribe(aLong -> VersionApi.getVersion().request(new SolveObserver<BaseEntity<Object>>(self) {
                    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
                    @Override
                    public void onSolve(BaseEntity<Object> response) {
                        VersionModel versionModel = new Gson().fromJson(new Gson().toJson(response.getData()), VersionModel.class);
                        updateApk(versionModel);
                        updateAd(versionModel);
                    }
                }));

    }

    private boolean canPing() {
        URL url = null;
        try {
            url = new URL(BuildConfig.SERVER_URL);
            InputStream in = url.openStream();//打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream
            System.out.println("连接正常");
            in.close();//关闭此输入流并释放与该流关联的所有系统资源。
            return true;
        } catch (IOException e) {
            System.out.println("无法连接到：" + url.toString());
        }
        return false;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
//        Toasty.success(self,AppUtils.isAppRoot()+"").show();
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    @OnClick(R.id.button)
    public void onViewClicked() {

        if (qrCodeDialog != null && qrCodeDialog.isShowing()) {
            qrCodeDialog.dismiss();
            qrCodeDialog = null;
        }


        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
            @Override
            public void response(final Map info) throws RemoteException {
                if (info == null) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toasty.error(self, "人脸识别初始化失败，请检查设备完整性").show();
                        }
                    });
                    return;
                } else {
                    String rawdata = (String) info.get("rawdata");
                    getWxAuthInfo(rawdata);
                }
            }
        });


//           ActivityManager am = (ActivityManager) self.getSystemService(Context.ACTIVITY_SERVICE);
//            am.killBackgroundProcesses("com.tencent.wxpayface");
//            Method forceStopPackage = am.getClass().getDeclaredMethod("forceStopPackage", String.class);
//            forceStopPackage.setAccessible(true);
//
//        Log.d("MainActivity",App.getHub().getConnectionState().name());


    }


    private void updateApk(VersionModel versionModel) {
        if (versionModel.getVersion() > getVersion()) {
            if (AppUtils.isAppRoot()) {
                AutoInstaller installer = new AutoInstaller.Builder(self)
                        .setMode(AutoInstaller.MODE.ROOT_ONLY)
                        .build();
                installer.installFromUrl(versionModel.getUrl());
            } else {
                (new Thread(new Runnable() {
                    public void run() {
                        File file = downloadFile(versionModel.getUrl());
                        try {
                            if (file == null) {
                                return;
                            }
                            Intent intent = new Intent(Intent.ACTION_VIEW);
                            String type;


                            type = MimeTypeMap.getSingleton().getMimeTypeFromExtension(FileUtils.getFileExtension(file));

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                                intent.setFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                                Uri contentUri = FileProvider.getUriForFile(self, "cn.csfz.paypoint.fileprovider", file);
                                intent.setDataAndType(contentUri, type);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                self.startActivity(intent);
                            }
                        }catch (Exception e)
                        {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    Toasty.error(self,"更新失败"+e.getMessage()).show();
                                }
                            });
                        }
                    }
                })).start();

            }

        }
    }

    private File downloadFile(String httpUrl) {
        if (TextUtils.isEmpty(httpUrl)) {
            throw new IllegalArgumentException();
        } else {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "update.apk");
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(httpUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                boolean var8 = false;

                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (Exception var17) {
                var17.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException var16) {
                    inputStream = null;
                    outputStream = null;
                }

            }

            return file;

        }
    }

    @RequiresApi(api = Build.VERSION_CODES.JELLY_BEAN_MR1)
    private void updateAd(VersionModel versionModel) {
        if (adPresentation != null) {
            adPresentation.updateVideos(versionModel);
        }
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


    private void setDefaultLauncher() {
        String packageName = "com.android.launcher3";    //默认launcher包名
        String className = "com.android.launcher3.Launcher";    //默认launcher入口

        PackageManager pm = self.getPackageManager();
        //清除当前默认launcher
        ArrayList<IntentFilter> intentList = new ArrayList<IntentFilter>();
        ArrayList<ComponentName> cnList = new ArrayList<ComponentName>();
        self.getPackageManager().getPreferredActivities(intentList, cnList, null);
        IntentFilter dhIF = null;
        for (int i = 0; i < cnList.size(); i++) {
            dhIF = intentList.get(i);
            if (dhIF.hasAction(Intent.ACTION_MAIN) && dhIF.hasCategory(Intent.CATEGORY_HOME)) {
                self.getPackageManager().clearPackagePreferredActivities(cnList.get(i).getPackageName());
            }
        }
        //获取所有launcher activity
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);


        List<ResolveInfo> list = pm.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);


        // get all components and the best match
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_MAIN);
        filter.addCategory(Intent.CATEGORY_HOME);
        filter.addCategory(Intent.CATEGORY_DEFAULT);
        final int N = list.size();
        //设置默认launcher
        ComponentName launcher = new ComponentName(packageName, className);
        ComponentName[] set = new ComponentName[N];
        int defaultMatch = 0;
        for (int i = 0; i < N; i++) {
            ResolveInfo r = list.get(i);
            set[i] = new ComponentName(r.activityInfo.packageName, r.activityInfo.name);
            if (launcher.getClassName().equals(r.activityInfo.name)) {
                defaultMatch = r.match;
            }
        }
        //将设置的默认launcher，添加到系统偏好
        pm.addPreferredActivity(filter, defaultMatch, set, launcher);
    }
}
