package cn.csfz.wxpaypoint.model;


import java.util.List;

import lombok.Data;

@Data
public class VersionModel {


    /**
     * version : 2020062201
     * url : http://ytj.cxwos.com/wxpaypoint_1.0.0_1_20200622.apk
     */

    private int version;
    private String url;
    private int adVersion;
    private List<Video> videos;
}
