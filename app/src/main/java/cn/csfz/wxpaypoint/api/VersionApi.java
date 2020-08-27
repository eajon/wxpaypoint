package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import java.util.HashMap;

import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.util.Utils;

public class VersionApi {


    public static RxHttp getVersion() {
        String sn = Utils.getDeviceSN();
        VersionModel versionModel = new VersionModel();
        versionModel.setData(sn);
        return new RxHttp.Builder().post("/Visual/GetInitData").json(versionModel).build();
    }
}
