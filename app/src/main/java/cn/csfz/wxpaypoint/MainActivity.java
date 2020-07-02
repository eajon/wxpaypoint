package cn.csfz.wxpaypoint;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;

import com.github.eajon.RxHttp;
import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.HttpObserver;
import com.github.eajon.util.LoggerUtils;
import com.github.eajon.util.RxUtils;
import com.microsoft.signalr.HubConnection;
import com.microsoft.signalr.HubConnectionBuilder;
import com.microsoft.signalr.TransportEnum;
//import com.tencent.wxpayface.IWxPayfaceCallback;
//import com.tencent.wxpayface.WxPayFace;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import cn.csfz.wxpaypoint.api.VersionApi;
import cn.csfz.wxpaypoint.api.WxApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.compont.SolveObserver;
import cn.csfz.wxpaypoint.model.BaseEntity;
import cn.csfz.wxpaypoint.model.VersionModel;
import cn.eajon.tool.ActivityUtils;
import cn.eajon.tool.IntentUtils;
import cn.eajon.tool.LogUtils;
import cn.eajon.tool.ObservableUtils;
import es.dmoral.toasty.Toasty;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.functions.Consumer;
import top.wuhaojie.installerlibrary.AutoInstaller;

public class MainActivity extends BaseActivity {
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

//        HubConnection hubConnection = HubConnectionBuilder.create("http://websocket.vendor.cxwos.com:80/websocket/MachineHub?userId=0002&machineId=0002").withTransport(TransportEnum.LONG_POLLING).build();
//        hubConnection.on("closeNotify", (message) -> {
//            LogUtils.d(message);
//        }, String.class);
//        hubConnection.on("openNotify", (message) -> {
//            LogUtils.d(message);
//        }, String.class);
////        hubConnection.on("updateNotify", (message) -> {
////            AutoInstaller installer = new AutoInstaller.Builder(this)
////                    .setMode(AutoInstaller.MODE.ROOT_ONLY)
////                    .build();
////            installer.installFromUrl(message);
////        }, String.class);
//        Observable.create((ObservableOnSubscribe) emitter -> {
//                    hubConnection.start().blockingAwait(10, TimeUnit.SECONDS);
//                    emitter.onComplete();
//                }
//        ).compose(ObservableUtils.ioMain()).subscribe();
//        checkVersion();
//        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {
//            @Override
//            public void response(Map map) throws RemoteException {
//                System.out.println(map.toString());
//            }
//        });
//
//        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
//            @Override
//            public void response(final Map info) throws RemoteException {
//
//                if (info == null) {
//
//
//                    new RuntimeException("调用返回为空").printStackTrace();
//                    return;
//
//                }else
//                {
//                    String code = (String) info.get("return_code");
//                    String msg = (String) info.get("return_msg");
//                    String rawdata = (String) info.get("rawdata");
//                    getWxAuthInfo(rawdata);
//                }
//
//
//            }
//        });
    }

    @Override
    protected void initLogic() {

    }

    private void getWxAuthInfo(String rawdata) {
        WxApi.getWxAuthInfo(rawdata).request(new HttpObserver<Object>() {
            @Override
            public void onSuccess(Object response) {
                LogUtils.d(response.toString());
//                Map<String, String> map = new HashMap<>();
//                map.put("appid","");
//                map.put("","");
//                map.put("","");
//                map.put("","");
//                map.put("","");
//                map.put("","");
//
//
//              WxPayFace.getInstance().getUserPayScoreStatus(map, new IWxPayfaceCallback() {
//                  @Override
//                  public void response(Map map) throws RemoteException {
//
//                  }
//              });
            }

            @Override
            public void onError(ApiException exception) {
                LogUtils.d(exception.toString());
            }
        });
    }

    private void checkVersion() {
        int codeversin = getVersion();
        Toasty.normal(self, codeversin + "");


//
        VersionApi.getVersion().request(new SolveObserver<BaseEntity<VersionModel>>(self) {
            @Override
            public void onSolve(BaseEntity<VersionModel> response) {
                VersionModel versionModel = response.getData();
//                if (versionModel.getVersion() > codeversin) {
                AutoInstaller installer = new AutoInstaller.Builder(self)
                        .setMode(AutoInstaller.MODE.ROOT_ONLY)
                        .build();
                installer.installFromUrl(versionModel.getUrl());
//                }
            }

        });

    }


    public int getVersion() {
        PackageInfo pkg;
        int versionCode = 0;
        String versionName = "";
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
