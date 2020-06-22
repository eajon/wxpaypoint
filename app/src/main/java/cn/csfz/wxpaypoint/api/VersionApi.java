package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import java.util.HashMap;

public class VersionApi {


    public static RxHttp getVersion()
    {
        HashMap<String,String> map =new HashMap<>();
        return new RxHttp.Builder().get("WeChatPay/GetWxPayAndroidVersion").build();
    }
}
