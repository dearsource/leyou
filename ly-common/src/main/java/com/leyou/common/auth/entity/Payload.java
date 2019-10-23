package com.leyou.common.auth.entity;

import lombok.Data;

import java.util.Date;

@Data
public class Payload<T> {
    //jwt的id
    private String id;
    //过期时间
    private Date Expiration;
    //自定义信息
    private T userInfo;
}
