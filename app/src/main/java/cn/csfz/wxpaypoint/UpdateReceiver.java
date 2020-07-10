package cn.csfz.wxpaypoint;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import cn.csfz.wxpaypoint.util.Utils;
import cn.eajon.tool.AppUtils;
import cn.eajon.tool.LogUtils;

public class UpdateReceiver extends BroadcastReceiver {


    @Override
    public void onReceive(Context context, Intent intent) {


        String packageName = intent.getDataString();
        if (intent.getAction().equals(Intent.ACTION_PACKAGE_REPLACED)) {//接收升级广播
            LogUtils.e("onReceive:升级了一个安装包，重新启动此程序");
            if (packageName.equals("package:" + App.getContext().getPackageName())) {
                Utils.restartAPP(context);//升级完自身app,重启自身
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_ADDED)) {//接收安装广播
            LogUtils.e("onReceive:安装了" + packageName);
            if (packageName.equals("package:" + App.getContext().getPackageName())) {
                /*SystemUtil.reBootDevice();*/
            }
        } else if (intent.getAction().equals(Intent.ACTION_PACKAGE_REMOVED)) { //接收卸载广播
            LogUtils.e("onReceive:卸载了" + packageName);
        }

    }
}
