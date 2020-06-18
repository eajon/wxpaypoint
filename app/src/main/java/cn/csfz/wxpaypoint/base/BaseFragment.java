package cn.csfz.wxpaypoint.base;

import android.app.Activity;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.LayoutRes;
import androidx.annotation.Nullable;

import com.trello.rxlifecycle3.components.support.RxFragment;

import butterknife.BindView;
import butterknife.ButterKnife;
import cn.csfz.wxpaypoint.R;


/**
 * Created By eajon on 2019/5/17.
 */

public abstract class BaseFragment extends RxFragment {

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


    protected Activity self;

    @LayoutRes
    protected abstract int setContentId();

    protected abstract void init(Bundle savedInstanceState);

    protected abstract void initLogic();

    protected abstract boolean hasToolBar();


    /******************************lifecycle area*****************************************/
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        self = this.getActivity();
        if (hasToolBar()) {
            LinearLayout layout = new LinearLayout(self);
            layout.setOrientation(LinearLayout.VERTICAL);
            layout.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            inflater.inflate(R.layout.tool_bar_layout, layout, true);
            inflater.inflate(setContentId(), layout, true);
            return layout;
        } else {
            return inflater.inflate(setContentId(), container);
        }
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        ButterKnife.bind(this, view);
        init(savedInstanceState);
        initLogic();
    }


    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onDestroy() {
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
