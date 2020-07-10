package cn.csfz.wxpaypoint.activity;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.eajon.tool.ObservableUtils;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class OpenDoorActivity extends BaseActivity {
    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected int setContentId() {
        return R.layout.activity_open_door;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    private Disposable disposable;

    @Override
    protected void initLogic() {
        Observable.interval(1, TimeUnit.SECONDS).compose(ObservableUtils.ioMain()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                OpenDoorActivity.this.disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (aLong > 30) {
                    OpenDoorActivity.this.finish();
                }
            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onComplete() {

            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (disposable != null && !disposable.isDisposed()) {
            disposable.dispose();
        }
    }
}
