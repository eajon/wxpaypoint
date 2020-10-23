package cn.csfz.wxpaypoint.model;

import java.util.List;

import lombok.Data;

@Data
public class Product {

    /**
     * id : cd4a3daf-660c-436a-9690-93bfab0e540a
     * visualId : 2488ea3d-f9bd-48ab-b3ee-f0a3ff9e3778
     * visualCode : 4440
     * code : VP20200623046023
     * limit : 100
     * skuId : 39
     * skuName : 康师傅红烧牛肉面碗装
     * skuValue : 144g
     * skuBarCode : 6920152400777
     * skuImg : 1004879743089427728386915.jpeg
     * price : 0.01
     * count : 210
     */

    private String id;
    private String visualId;
    private String visualCode;
    private String code;
    private int limit;
    private int skuId;
    private String skuName;
    private String skuValue;
    private String skuBarCode;
    private String skuImg;
    private double price;
    private int count;
    private String typeId;

}
