package cn.csfz.wxpaypoint.activity;

import android.os.Bundle;

import java.util.concurrent.TimeUnit;

import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.base.BaseActivity;
import io.reactivex.Observable;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;

public class CloseDoorActivity extends BaseActivity {




    @Override
    protected boolean hasToolBar() {
        return false;
    }

    @Override
    protected int setContentId() {
        return R.layout.activity_close_door;
    }

    @Override
    protected void init(Bundle savedInstanceState) {

    }

    private Disposable disposable;

    @Override
    protected void initLogic() {
        Observable.interval(1, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<Long>() {
            @Override
            public void onSubscribe(Disposable d) {
                CloseDoorActivity.this.disposable = d;
            }

            @Override
            public void onNext(Long aLong) {
                if (aLong > 5) {
                    CloseDoorActivity.this.finish();
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
