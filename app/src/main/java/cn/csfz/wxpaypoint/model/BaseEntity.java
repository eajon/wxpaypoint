package cn.csfz.wxpaypoint.model;

import lombok.Data;

/**
 * @author eajon on 2019/9/24.
 */

@Data
public class BaseEntity<T> {


    private String message;
    private String code;
    private T data;

    public boolean isSuccess() {
        if (code.equals("200")) {
            return true;
        } else {
            return false;
        }
    }

}
