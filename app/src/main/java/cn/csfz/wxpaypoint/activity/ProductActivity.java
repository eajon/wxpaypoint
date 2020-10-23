package cn.csfz.wxpaypoint.activity;

import android.os.Bundle;
import android.view.View;

import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.chad.library.adapter.base.BaseQuickAdapter;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.util.List;

import butterknife.BindView;
import cn.csfz.wxpaypoint.R;
import cn.csfz.wxpaypoint.adapter.ProductAdapter;
import cn.csfz.wxpaypoint.api.ProductApi;
import cn.csfz.wxpaypoint.base.BaseActivity;
import cn.csfz.wxpaypoint.compont.SolveObserver;
import cn.csfz.wxpaypoint.model.BaseEntity;
import cn.csfz.wxpaypoint.model.Product;
import cn.csfz.wxpaypoint.model.VersionModel;
import es.dmoral.toasty.Toasty;

public class ProductActivity extends BaseActivity {

    @BindView(R.id.recycler_view)
    RecyclerView recyclerView;

    ProductAdapter productAdapter;
    @Override
    protected boolean hasToolBar() {
        return true;
    }

    @Override
    protected int setContentId() {
        return R.layout.activity_product;
    }

    @Override
    protected void init(Bundle savedInstanceState) {
        initToolBar(false, false, false, true, true, false);
        leftIv.setImageDrawable(getResources().getDrawable(R.mipmap.back));
        leftIv.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                self.finish();
            }
        });
        centerIv.setImageDrawable(getResources().getDrawable(R.mipmap.top));

        ProductApi.getProduct().request(new SolveObserver<BaseEntity<Object>>(self) {
            @Override
            public void onSolve(BaseEntity<Object> response) {
                Type type =new TypeToken<List<Product>>(){}.getType();
                List<Product> products = new Gson().fromJson(new Gson().toJson(response.getData()), type);
                productAdapter.setNewData(products);
            }
        });
    }

    @Override
    protected void initLogic() {
       recyclerView.setLayoutManager(new GridLayoutManager(self,4));
       productAdapter =new ProductAdapter(R.layout.item_product,null);
       recyclerView.setAdapter(productAdapter);
       productAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
           @Override
           public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
               Toasty.normal(self,"请返回首页购买").show();
           }
       });
    }
}
