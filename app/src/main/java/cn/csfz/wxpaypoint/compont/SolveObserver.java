package cn.csfz.wxpaypoint.compont;

import android.app.Activity;

import com.github.eajon.exception.ApiException;
import com.github.eajon.observer.HttpObserver;
import com.google.gson.Gson;

import cn.csfz.wxpaypoint.model.BaseEntity;
import es.dmoral.toasty.Toasty;

public abstract class SolveObserver<T extends BaseEntity> extends HttpObserver<BaseEntity> {

    public abstract void onSolve(T response);

    private Activity context;

    public SolveObserver(Activity context) {
        this.context = context;
    }


    @SuppressWarnings("unchecked")
    @Override
    public void onSuccess(BaseEntity response) {
        if (response.isSuccess()) {
            onSolve(( T ) response);
        } else {
            Toasty.error(context, response.getMessage()).show();
        }
    }

    /*服务器异常若有指定Json格式返回错误信息，无则提示服务器错误*/
    @Override
    public void onError(ApiException exception) {
        try {
            BaseEntity errorResponse = new Gson().fromJson(exception.getBodyMessage(), BaseEntity.class);
            Toasty.error(context, errorResponse.getMessage()).show();
        } catch (Exception e) {
            Toasty.error(context, "网络错误").show();
        }
    }
}
