package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import cn.csfz.wxpaypoint.model.FaceInfo;
import cn.csfz.wxpaypoint.model.RawData;
import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.DeviceUtils;
import cn.eajon.tool.SPUtils;

public class WxApi {





    public static RxHttp getWxAuthInfo(String data)
    {
        RawData rawData =new RawData();
        rawData.setRawdata(data);
        rawData.setDevice_id(SPUtils.getData("machineCode",String.class));
        return new RxHttp.Builder().post("/Visual/GetWxPayFaceAuthinfo").json(rawData).build();
    }

    public static RxHttp getWxUnion(String faceSid,String openId,String outTradeNo)
    {
        FaceInfo faceInfo =new FaceInfo();
        faceInfo.setFace_sid(faceSid);
        faceInfo.setOpen_id(openId);
        faceInfo.setOut_trade_no(outTradeNo);
        faceInfo.setDevice_id(SPUtils.getData("machineCode",String.class));
        return new RxHttp.Builder().post("/WeChatPay/GetWxPayFaceUnionId").json(faceInfo).build();
    }

    public static RxHttp createOrder(String faceSid,String openId,String outTradeNo)
    {
        FaceInfo faceInfo =new FaceInfo();
        faceInfo.setFace_sid(faceSid);
        faceInfo.setOpen_id(openId);
        faceInfo.setOut_trade_no(outTradeNo);
        faceInfo.setDevice_id(SPUtils.getData("machineCode",String.class));
        return new RxHttp.Builder().post("Visual/CreatWxPayFaceOrder").json(faceInfo).build();
    }

}
