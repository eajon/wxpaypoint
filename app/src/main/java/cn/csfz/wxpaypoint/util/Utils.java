package cn.csfz.wxpaypoint.util;

import android.content.Context;
import android.content.Intent;

import cn.csfz.wxpaypoint.App;

public class Utils {

    public static String getDeviceSN(){

        String serialNumber = android.os.Build.SERIAL;

        return serialNumber;
    }



    /**
     * 重启整个APP
     * @param context
     */
    public static void restartAPP(Context context){
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(App.getContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        context.startActivity(intent);
    }

}
