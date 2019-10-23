package com.leyou.auth.config;

import com.leyou.common.auth.utils.RsaUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.format.annotation.DateTimeFormat;

import javax.annotation.PostConstruct;
import java.security.PrivateKey;
import java.security.PublicKey;

@Slf4j
@Data
@ConfigurationProperties(prefix = "ly.jwt")
public class JwtProperties implements InitializingBean {
    //# 公钥地址
    private String pubKeyPath;
    //# 私钥地址
    private String  priKeyPath;
    //私钥
    private PrivateKey privateKey;
//    公钥
    private PublicKey publicKey;
    //自定义配置
    private UserProp user = new UserProp();
    @Data
    public class UserProp{
        private Integer expire;
        private String cookieName;//# cookie名称
        private String cookieDomain;//cookie的域
        private Integer minRefreshInterval;//最小刷新时间
    }
//    @PostConstruct
    @Override
    public void afterPropertiesSet() throws Exception {
        try {
            publicKey = RsaUtils.getPublicKey(pubKeyPath);
            privateKey = RsaUtils.getPrivateKey(priKeyPath);
        } catch (Exception e) {
            log.error("初始化公钥和私钥失败！", e);
            throw new RuntimeException(e);
        }
    }
}
