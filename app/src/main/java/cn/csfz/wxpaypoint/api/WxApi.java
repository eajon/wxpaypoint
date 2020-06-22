package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import cn.csfz.wxpaypoint.model.RawData;

public class WxApi {



    public static RxHttp getWxAuthInfo(String rawDate)
    {
        RawData rawData =new RawData();
        rawData.setRawdata(rawDate);
        return new RxHttp.Builder().post("/WeChatPay/GetWxPayFaceAuthinfo").json(rawData).build();
    }

}
