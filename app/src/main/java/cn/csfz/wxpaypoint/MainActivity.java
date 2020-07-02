package cn.csfz.wxpaypoint;

import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.RemoteException;
import android.widget.ImageView;

import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.util.HashMap;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import cn.csfz.wxpaypoint.api.WxApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.compont.SolveObserver;
import cn.csfz.wxpaypoint.model.AuthInfo;
import cn.csfz.wxpaypoint.model.BaseEntity;
import cn.eajon.tool.LogUtils;
import es.dmoral.toasty.Toasty;

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

    }

    private void getWxAuthInfo(String rawdata) {

        WxApi.getWxAuthInfo(rawdata).request(new SolveObserver<BaseEntity<AuthInfo>>(self) {
            @Override
            public void onSolve(BaseEntity<AuthInfo> response) {
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
                        }
                    });
                }
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
