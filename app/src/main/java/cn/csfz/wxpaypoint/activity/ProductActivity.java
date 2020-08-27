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
//       String data ="[\n" +
//               " {\n" +
//               "      \"id\": \"cd4a3daf-660c-436a-9690-93bfab0e540a\",\n" +
//               "      \"visualId\": \"2488ea3d-f9bd-48ab-b3ee-f0a3ff9e3778\",\n" +
//               "      \"visualCode\": \"4440\",\n" +
//               "      \"code\": \"VP20200623046023\",\n" +
//               "      \"limit\": 100,\n" +
//               "      \"skuId\": 39,\n" +
//               "      \"skuName\": \"康师傅红烧牛肉面碗装\",\n" +
//               "      \"skuValue\": \"144g\",\n" +
//               "      \"skuBarCode\": \"6920152400777\",\n" +
//               "      \"skuImg\": \"1004879743089427728386915.jpeg\",\n" +
//               "      \"price\": 0.01,\n" +
//               "      \"count\": 210\n" +
//               "    },\n" +
//               "    {\n" +
//               "      \"id\": \"342f755e-02b9-4f80-b95f-673f4ae86b16\",\n" +
//               "      \"visualId\": \"2488ea3d-f9bd-48ab-b3ee-f0a3ff9e3778\",\n" +
//               "      \"visualCode\": \"4440\",\n" +
//               "      \"code\": \"VP20200801794282\",\n" +
//               "      \"limit\": 10,\n" +
//               "      \"skuId\": 1422,\n" +
//               "      \"skuName\": \"芬达/橙味500mL PET瓶装饮料\",\n" +
//               "      \"skuValue\": \"500ml\",\n" +
//               "      \"skuBarCode\": \"6954767442075\",\n" +
//               "      \"skuImg\": \"16581241805451828377230955.jpg\",\n" +
//               "      \"price\": 0.02,\n" +
//               "      \"count\": 23\n" +
//               "    },\n" +
//               "    {\n" +
//               "      \"id\": \"8875f413-0823-45cc-9741-93249ee75f6d\",\n" +
//               "      \"visualId\": \"2488ea3d-f9bd-48ab-b3ee-f0a3ff9e3778\",\n" +
//               "      \"visualCode\": \"4440\",\n" +
//               "      \"code\": \"VP20200623368039\",\n" +
//               "      \"limit\": 100,\n" +
//               "      \"skuId\": 407,\n" +
//               "      \"skuName\": \"百事可乐Pepsi碳酸饮料\",\n" +
//               "      \"skuValue\": \"330ml\",\n" +
//               "      \"skuBarCode\": \"6902827130226\",\n" +
//               "      \"skuImg\": \"1567256042256440497311850.jpg\",\n" +
//               "      \"price\": 0.01,\n" +
//               "      \"count\": 97\n" +
//               "    },\n" +
//               "    {\n" +
//               "      \"id\": \"8380b501-5652-4169-81d6-a053a51e1673\",\n" +
//               "      \"visualId\": \"2488ea3d-f9bd-48ab-b3ee-f0a3ff9e3778\",\n" +
//               "      \"visualCode\": \"4440\",\n" +
//               "      \"code\": \"VP20200623434087\",\n" +
//               "      \"limit\": 100,\n" +
//               "      \"skuId\": 74,\n" +
//               "      \"skuName\": \"可口可乐汽水\",\n" +
//               "      \"skuValue\": \"330ml\",\n" +
//               "      \"skuBarCode\": \"6908512208645\",\n" +
//               "      \"skuImg\": \"9312957602379079568679437.jpg\",\n" +
//               "      \"price\": 0.01,\n" +
//               "      \"count\": 77\n" +
//               "    }]";
//        Type type =new TypeToken<List<Product>>(){}.getType();
//        List<Product> products = new Gson().fromJson(data, type);
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
