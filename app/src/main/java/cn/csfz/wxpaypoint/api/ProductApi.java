package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import cn.csfz.wxpaypoint.model.VersionModel;
import cn.csfz.wxpaypoint.util.Utils;

public class ProductApi {

    public static RxHttp getProduct() {
        String sn = Utils.getDeviceSN();
        VersionModel versionModel =new VersionModel();
        versionModel.setData(sn);
        return new RxHttp.Builder().post("Visual/GetVisualProductList").json(versionModel).build();
    }
}
