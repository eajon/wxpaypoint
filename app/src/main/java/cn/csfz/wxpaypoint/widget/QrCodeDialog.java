package cn.csfz.wxpaypoint.widget;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.engine.DiskCacheStrategy;
import com.bumptech.glide.request.RequestOptions;

import java.util.Timer;
import java.util.TimerTask;

import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.util.MyTask;
import cn.csfz.wxpaypoint.util.QRCodeUtil;
import cn.csfz.wxpaypoint.util.QrCodeUtil2;
import cn.csfz.wxpaypoint.util.Utils;


public class QrCodeDialog extends Dialog {

    private Context mContext;
    private String url = "  https://vendor.cxwos.com/?machineCode=";
    private TextView textView;
    private String qrImg;

    public QrCodeDialog(Context context, boolean cancelable, OnCancelListener cancelListener) {
        super(context, cancelable, cancelListener);
        this.mContext = context;
    }

    public QrCodeDialog(Context context, int theme) {
        super(context, theme);
        this.mContext = context;
    }

    public QrCodeDialog(Context context,String qrImg) {
        super(context);
        this.mContext = context;
        this.qrImg =qrImg;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.dialog_qrcode);
        ImageView qrImage = findViewById(R.id.qrcode_image);
//        BitmapDrawable bd = (BitmapDrawable) mContext.getResources().getDrawable(R.mipmap.fp_logo);
//        Bitmap logoBitMap = bd.getBitmap();
        String updateTime = String.valueOf(System.currentTimeMillis());
//        RequestOptions options = new RequestOptions().skipMemoryCache(true).diskCacheStrategy(DiskCacheStrategy.NONE).timeout();
        Glide.with(mContext).load(qrImg).apply(new RequestOptions().timeout(60000)).into(qrImage);
//        new MyTask(qrImg,qrImage).execute();
        Window dialogWindow = this.getWindow();
        WindowManager.LayoutParams lp = dialogWindow.getAttributes();
        lp.width = dpToPx(this.getContext(), 280);
        dialogWindow.setAttributes(lp);
        setCanceledOnTouchOutside(true);
        textView =findViewById(R.id.message_text);
        findViewById(R.id.back_text).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                QrCodeDialog.this.dismiss();
            }
        });
    }

    private int dpToPx(Context context, float dpValue) {//dp转换为px
        float scale = context.getResources().getDisplayMetrics().density;//获得当前屏幕密度
        return (int) (dpValue * scale + 0.5f);
    }

    public void setMessage(String message) {

        textView.setText(message);
    }

    public void show() {
        super.show();
        final Timer t = new Timer();
        t.schedule(new TimerTask() {
            public void run() {
                if (null != QrCodeDialog.this && QrCodeDialog.this.isShowing()) {
                    QrCodeDialog.this.dismiss();
                }
                t.cancel();
            }
        }, 30000);
    }


}