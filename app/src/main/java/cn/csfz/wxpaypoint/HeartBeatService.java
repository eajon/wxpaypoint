package cn.csfz.wxpaypoint;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Looper;
import android.util.Log;

import com.sunfusheng.daemon.AbsHeartBeatService;

import java.util.List;

import cn.csfz.wxpaypoint.activity.CloseDoorActivity;
import cn.csfz.wxpaypoint.activity.OpenDoorActivity;
import cn.csfz.wxpaypoint.util.ActivityCollector;
import cn.eajon.tool.ActivityUtils;

/**
 * @author sunfusheng on 2018/8/3.
 */
public class HeartBeatService extends AbsHeartBeatService {
    private static final String TAG = "---> HeartBeatService";
    private static final android.os.Handler mainThreadHandler = new android.os.Handler(Looper.getMainLooper());

    @Override
    public void onStartService() {
        Log.d(TAG, "onStartService()");
    }

    @Override
    public void onStopService() {
        Log.e(TAG, "onStopService()");
    }

    @Override
    public long getDelayExecutedMillis() {
        return 0;
    }

    @Override
    public long getHeartBeatMillis() {
        return 30 * 1000;
    }

    @Override
    public void onHeartBeat() {

        Log.d(TAG, "onHeartBeat()");
        if(!ActivityCollector.isActivityExist(MainActivity.class))
        {
            Intent i = new Intent(this, MainActivity.class);
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(i);
        }else {
            if(!(ActivityCollector.isActivityExist(CloseDoorActivity.class)||ActivityCollector.isActivityExist(OpenDoorActivity.class)))
            {
                moveToFront();
            }
        }
    }

    private boolean isTopActivity(Context context) {
        ActivityManager am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);
        List<ActivityManager.RunningAppProcessInfo> appProcesses = am.getRunningAppProcesses();
        if (appProcesses == null) {
            return false;
        }
        for (ActivityManager.RunningAppProcessInfo appProcess : appProcesses) {
            if (appProcess.processName.equals(context.getPackageName()) && appProcess.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                return true;
            }
        }
        return false;
    }

    protected void moveToFront() {
        // honeycomb
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        List<ActivityManager.RunningTaskInfo> recentTasks = manager.getRunningTasks(Integer.MAX_VALUE);
        for (int i = 0; i < recentTasks.size(); i++){
            Log.e("xk", "  "+recentTasks.get(i).baseActivity.toShortString() + "   ID: "+recentTasks.get(i).id+"");
            Log.e("xk","@@@@  "+recentTasks.get(i).baseActivity.toShortString());
            // bring to front
            if (recentTasks.get(i).baseActivity.toShortString().indexOf("cn.csfz.wxpaypoint.MainActivity") > -1) {
                manager.moveTaskToFront(recentTasks.get(i).id, ActivityManager.MOVE_TASK_WITH_HOME);
            }
        }
    }
}
