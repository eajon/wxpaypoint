package cn.csfz.wxpaypoint.adapter;

import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.model.Product;

public class ProductAdapter extends BaseQuickAdapter<Product, BaseViewHolder> {


    private static String PREFIX = "https://oss-teatime-pic.51teatime.com/";

    public ProductAdapter(int layoutResId, @Nullable List<Product> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, Product item) {

        Glide.with(mContext).load(PREFIX + item.getSkuImg()).into((ImageView) helper.getView(R.id.product_iv));

        helper.setText(R.id.product_tv, item.getSkuName()).setText(R.id.price_tv, "￥" + item.getPrice());
    }
}
