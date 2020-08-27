package cn.csfz.wxpaypoint.activity;

import android.os.Bundle;
import android.view.View;
import android.webkit.WebChromeClient;
import android.webkit.WebSettings;
import android.webkit.WebView;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.util.Utils;
import me.jingbin.progress.WebProgress;

/**
 * @author eajon on 2020/1/13.
 */
public class NoticeActivity extends BaseActivity {

    @BindView(R.id.progress)
    WebProgress progress;
    @BindView(R.id.webView)
    WebView webView;

    @Override
    protected boolean hasToolBar() {
        return true;
    }

    @Override
    protected int setContentId() {
        return R.layout.activity_notice;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initLogic() {
//        webView.getSettings().setDomStorageEnabled(true);
        initToolBar(false, false, false, true, false, false);
        leftIv.setImageDrawable(getResources().getDrawable(R.mipmap.back));
        leftIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.finish();
            }
        });
        webView.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                super.onProgressChanged(view, newProgress);
                if (progress.isShown()) {
                    progress.setWebProgress(newProgress);
                    if (newProgress == 100) {
                        progress.hide();
                    }
                }
            }
        });
        WebSettings webSettings = webView.getSettings();
        webSettings.setJavaScriptEnabled(true);
        webSettings.setDomStorageEnabled(true);
        webSettings.setAllowFileAccess(true);
        webSettings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.NORMAL);
        webSettings.setLoadWithOverviewMode(true);
        String sn = Utils.getDeviceSN();
        webView.loadUrl("http://alipay.vendor.cxwos.com/#/about?machineCode=" + sn);
        progress.show();

    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // TODO: add setContentView(...) invocation
        ButterKnife.bind(this);
    }
}
