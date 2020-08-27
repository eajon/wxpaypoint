package cn.csfz.wxpaypoint.util;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Environment;
import android.text.TextUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

import cn.csfz.wxpaypoint.App;
import cn.csfz.wxpaypoint.BuildConfig;

import static android.content.Context.ACTIVITY_SERVICE;
import static androidx.core.content.ContextCompat.getSystemService;

public class Utils {


    private static SimpleDateFormat format = new SimpleDateFormat(
            "yyyy-MM-dd-HH-mm-ss");

    public static String getDeviceSN() {

        String serialNumber = android.os.Build.SERIAL;

        return serialNumber;
    }


    /**
     * 重启整个APP
     *
     * @param context
     */
    public static void restartAPP(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(App.getContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }


    /**
     * 启动整个APP
     *
     * @param context
     */
    public static void startApp(Context context) {
        Intent intent = context.getPackageManager()
                .getLaunchIntentForPackage(App.getContext().getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    public static boolean canPing() {
        URL url = null;
        try {
            url = new URL(BuildConfig.SERVER_URL);
            InputStream in = url.openStream();//打开到此 URL 的连接并返回一个用于从该连接读入的 InputStream
            System.out.println("连接正常");
            in.close();//关闭此输入流并释放与该流关联的所有系统资源。
            return true;
        } catch (IOException e) {
            System.out.println("无法连接到：" + url.toString());
        }
        return false;
    }

    public static int getVersion() {
        PackageInfo pkg;
        int versionCode = 0;
        try {
            pkg = App.getContext().getPackageManager().getPackageInfo(App.getContext().getPackageName(), 0);
            versionCode = pkg.versionCode;

        } catch (PackageManager.NameNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        return versionCode;
    }

    public static File downloadFile(String httpUrl) {
        if (TextUtils.isEmpty(httpUrl)) {
            throw new IllegalArgumentException();
        } else {
            File file = new File(Environment.getExternalStorageDirectory().getAbsolutePath());
            if (!file.exists()) {
                file.mkdirs();
            }

            file = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "update.apk");
            InputStream inputStream = null;
            FileOutputStream outputStream = null;
            HttpURLConnection connection = null;

            try {
                URL url = new URL(httpUrl);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(10000);
                connection.setReadTimeout(10000);
                connection.connect();
                inputStream = connection.getInputStream();
                outputStream = new FileOutputStream(file);
                byte[] buffer = new byte[1024];
                boolean var8 = false;

                int len;
                while ((len = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, len);
                }
            } catch (Exception var17) {
                var17.printStackTrace();
            } finally {
                try {
                    if (inputStream != null) {
                        inputStream.close();
                    }

                    if (outputStream != null) {
                        outputStream.close();
                    }

                    if (connection != null) {
                        connection.disconnect();
                    }
                } catch (IOException var16) {
                    inputStream = null;
                    outputStream = null;
                }

            }

            return file;

        }
    }

    public static String saveCrashInfo2File(Throwable ex) {
        StringBuffer sb = new StringBuffer("设备号：" + getDeviceSN());
        Writer writer = new StringWriter();
        PrintWriter pw = new PrintWriter(writer);
        ex.printStackTrace(pw);
        Throwable cause = ex.getCause();
// 循环着把所有的异常信息写入writer中
        while (cause != null) {
            cause.printStackTrace(pw);
            cause = cause.getCause();
        }
        pw.close();// 记得关闭
        String result = writer.toString();
        sb.append(result);
// 保存文件
        long timetamp = System.currentTimeMillis();

        String time = format.format(new Date());
        String fileName = "crash-" + time + "-" + timetamp + ".log";
        if (Environment.getExternalStorageState().equals(
                Environment.MEDIA_MOUNTED)) {
            try {
                File dir = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + File.separator + "crash");
                if (!dir.exists())
                    dir.mkdir();
                FileOutputStream fos = new FileOutputStream(new File(dir,
                        fileName));
                fos.write(sb.toString().getBytes());
                fos.close();
                return fileName;
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }

}
