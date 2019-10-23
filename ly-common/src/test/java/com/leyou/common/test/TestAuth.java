package com.leyou.common.test;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.auth.utils.RsaUtils;
import org.junit.Test;

import java.security.PrivateKey;
import java.security.PublicKey;

public class TestAuth {
    //私钥的存放目录
    private String privateFilePath = "F:\\itcast-work\\heima-jee98\\ssh\\id_rsa";
    //公钥的存放目录
    private String publicFilePath = "F:\\itcast-work\\heima-jee98\\ssh\\id_rsa.pub";

    @Test
    public void testRsa() throws Exception {
        RsaUtils.generateKey(publicFilePath,privateFilePath,"hello",2048);

        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        System.out.println("公钥"+publicKey);
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        System.out.println("私钥=="+privateKey);

    }

    @Test
    public void testJwt() throws Exception {
        UserInfo userInfo = new UserInfo();
        userInfo.setUsername("jack");
        userInfo.setId(1L);
        userInfo.setRole("admin");
        PrivateKey privateKey = RsaUtils.getPrivateKey(privateFilePath);
        //创建token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, privateKey, 3);
        System.out.println(token);
//        String newToken = "eyJhbGciOiJSUzI1NiJ9.eyJ2c2VyIjoie1wiaWRcIjoxLFwidXNlcm5hbWVcIjpcImphY2tcIixcInJvbGVcIjpcImFkbWluXCJ9IiwianRpIjoiTURWaFlUUTVORFl0T0RVMk9TMDBaV1ppTFdGaFlUQXRNMkprWlRFME1UUXhPREU1IiwiZXhwIjoxNTY2ODc5MTMxfQ.n6xQV2xelqQA0-qpI0Vy9pjsP1zBx03rO50KbtDqGjUleN_o1yG_vBwLRgXszzwasDcbWthso5cMG3EYZPwhYAgcz7RBJPXV_NlmS4nJX5JH2TLD4Uf_itWCAke7pio3x1gz-4r0onKAw5CHpzx2MmykK5BUowx-3RTW4oxk-tikcfHLl6i94NU2CiZy9L58HxEU7tpfX6Nv6iTyx9-OFTMjbh8jiVoOig1BujADzTHMd_R2KNj6Rg0DEiN-9Qu4z-GTiBngUjMeW3e_2aT3CLbp_Jy2pLU-TFVW9Tm7BohMS6x-uCLQYW1IRnBcwPNd-y69ktfHvQOEPHNLftoGNw";
        //解密
        PublicKey publicKey = RsaUtils.getPublicKey(publicFilePath);
        Payload<UserInfo> payLoad = JwtUtils.getInfoFromToken(token, publicKey, UserInfo.class);
        System.out.println(payLoad.getId());
        System.out.println(payLoad.getExpiration());
        System.out.println(payLoad.getUserInfo());
    }

}
