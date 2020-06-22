package cn.csfz.wxpaypoint;

import android.os.Bundle;
import android.os.RemoteException;

import com.github.eajon.RxHttp;
import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.HttpObserver;
import com.tencent.wxpayface.IWxPayfaceCallback;
import com.tencent.wxpayface.WxPayFace;

import java.util.HashMap;
import java.util.Map;

import cn.csfz.wxpaypoint.api.WxApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.eajon.tool.LogUtils;

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
        WxPayFace.getInstance().initWxpayface(this, new IWxPayfaceCallback() {
            @Override
            public void response(Map map) throws RemoteException {
                System.out.println(map.toString());
            }
        });

        WxPayFace.getInstance().getWxpayfaceRawdata(new IWxPayfaceCallback() {
            @Override
            public void response(final Map info) throws RemoteException {

                if (info == null) {


                    new RuntimeException("调用返回为空").printStackTrace();
                    return;

                }else
                {
                    String code = (String) info.get("return_code");
                    String msg = (String) info.get("return_msg");
                    String rawdata = (String) info.get("rawdata");
                    getWxAuthInfo(rawdata);
                }


            }
        });
    }

    @Override
    protected void initLogic() {

    }

    private void getWxAuthInfo(String rawdata)
    {
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
}
