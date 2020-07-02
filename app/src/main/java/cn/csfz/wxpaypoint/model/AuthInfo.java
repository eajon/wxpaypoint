package cn.csfz.wxpaypoint.model;


import lombok.Data;

@Data
public class AuthInfo {

    private String authinfo;
    private String nonce_str;
    private String appid;
    private String mch_id;
    private String out_trade_no;
    private String payscore_out_request_no;
    private String payscore_service_id;
    private String sign;
}
