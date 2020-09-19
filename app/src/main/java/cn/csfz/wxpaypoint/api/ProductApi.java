package cn.csfz.wxpaypoint.api;

import com.github.eajon.RxHttp;

import cn.csfz.wxpaypoint.model.VersionModel;
import cn.eajon.tool.SPUtils;

public class ProductApi {

    public static RxHttp getProduct() {
        VersionModel versionModel =new VersionModel();
        versionModel.setData(SPUtils.getData("machineCode",String.class));
        return new RxHttp.Builder().post("Visual/GetVisualProductList").json(versionModel).build();
    }
}
