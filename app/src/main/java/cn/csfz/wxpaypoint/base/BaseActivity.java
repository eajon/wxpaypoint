package cn.csfz.wxpaypoint.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;

import com.trello.rxlifecycle3.components.support.RxAppCompatActivity;
import com.umeng.analytics.MobclickAgent;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.csfz.wxpaypoint.R;


/**
 * @author eajon on 2019/5/17.
 */
public abstract class BaseActivity extends RxAppCompatActivity {
    @Nullable
    @BindView(R.id.left_tv)
    public TextView leftTv;
    @Nullable
    @BindView(R.id.title_tv)
    public TextView titleTv;
    @Nullable
    @BindView(R.id.right_tv)
    public TextView rightTv;
    @Nullable
    @BindView(R.id.left_iv)
    public ImageView leftIv;
    @Nullable
    @BindView(R.id.center_iv)
    public ImageView centerIv;
    @Nullable
    @BindView(R.id.right_iv)
    public ImageView rightIv;
    @Nullable
    @BindView(R.id.toolbar)
    public RelativeLayout toolbar;
    protected FragmentActivity self;

    protected abstract boolean hasToolBar();

    @LayoutRes
    protected abstract int setContentId();

    protected abstract void init(Bundle savedInstanceState);

    protected abstract void initLogic();


    /******************************lifecycle area*****************************************/
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        self = this;
        if (hasToolBar()) {
            LinearLayout layout = new LinearLayout(self);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            LayoutInflater.from(self).inflate(R.layout.tool_bar_layout, layout, true);
            LayoutInflater.from(self).inflate(setContentId(), layout, true);
            setContentView(layout);
        } else {
            setContentView(setContentId());
        }
        ButterKnife.bind(this);
        init(savedInstanceState);
        initLogic();

    }


    @Override
    protected void onResume() {
        super.onResume();
        MobclickAgent.onResume(this);
    }

    @Override
    protected void onPause() {
        super.onPause();
        MobclickAgent.onPause(this);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    protected void initToolBar(boolean isLeftTv, boolean isTitleTv, boolean isRightTv, boolean isLeftIv, boolean isCenterIv, boolean isRightIV) {
        if (null != toolbar) {
            if (isLeftTv) {
                leftTv.setVisibility(View.VISIBLE);
            } else {
                leftTv.setVisibility(View.GONE);
            }

            if (isTitleTv) {
                titleTv.setVisibility(View.VISIBLE);
            } else {
                titleTv.setVisibility(View.GONE);
            }

            if (isRightTv) {
                rightTv.setVisibility(View.VISIBLE);
            } else {
                rightTv.setVisibility(View.GONE);
            }

            if (isLeftIv) {
                leftIv.setVisibility(View.VISIBLE);
            } else {
                leftIv.setVisibility(View.GONE);
            }

            if (isCenterIv) {
                centerIv.setVisibility(View.VISIBLE);
            } else {
                centerIv.setVisibility(View.GONE);
            }

            if (isRightIV) {
                rightIv.setVisibility(View.VISIBLE);
            } else {
                rightIv.setVisibility(View.GONE);
            }
        }
    }
}
