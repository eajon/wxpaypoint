package cn.csfz.wxpaypoint;

import android.os.Bundle;

import cn.csfz.wxpaypoint.base.BaseActivity;

public class MainActivity extends BaseActivity {
    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected int setContentId() {
        return 0;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    @Override
    protected void initLogic() {

    }
}
