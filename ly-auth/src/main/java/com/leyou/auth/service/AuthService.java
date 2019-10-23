package com.leyou.auth.service;

import com.leyou.auth.config.JwtProperties;
import com.leyou.common.Exception.LyException;
import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.CookieUtils;
import com.leyou.user.DTO.UserDTO;
import com.leyou.user.client.UserClient;
import lombok.extern.slf4j.Slf4j;
import org.joda.time.DateTime;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Date;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
public class AuthService {

    @Autowired
    private UserClient userClient;
    @Autowired
    private JwtProperties prop;

    /**
     * 处理登录业务
     * @param username
     * @param password
     * @param response
     */
    public void login(String username, String password, HttpServletResponse response) {
//        1、获取用户信息
        UserDTO userDTO = userClient.queryUserByPassword(username, password);
//        2、使用不敏感的信息组成一个自描述内容
        UserInfo userInfo = new UserInfo(userDTO.getId(), userDTO.getUsername(), "admin");
//        3、使用jwt 生成token
        String token = JwtUtils.generateTokenExpireInMinutes(userInfo, prop.getPrivateKey(), 30);
//        4、把token放入cookie中
        CookieUtils.newCookieBuilder()
                .response(response)
                .domain(prop.getUser().getCookieDomain())
                .name(prop.getUser().getCookieName())
                .value(token)
                .httpOnly(true)
                .build();
    }

    /**
     * 验证用户登录信息
     * @param request
     * @return
     */
    public UserInfo userVerify(HttpServletRequest request,HttpServletResponse response) {
        try{
//        1、从request中获取 token
        String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
//        2、验证token，验证jwt 有效性
        Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            String jwtId = payload.getId();
            //插件token是否已经被注销
            Boolean b = redisTemplate.hasKey(jwtId);
            if(b != null && b){
                throw new LyException(ExceptionEnum.UNAUTHORIZED);
            }
            //token的过期时间
            Date expiration = payload.getExpiration();
            //刷新间隔
            Integer minRefreshInterval = prop.getUser().getMinRefreshInterval();
            //最早刷新的时间
            DateTime refreTime = new DateTime(expiration.getTime()).minusMinutes(minRefreshInterval);
            //用当前时间和最早刷新时间比较，如果当前时间 大于  最早刷新时间 就刷新
            if(refreTime.isBefore(System.currentTimeMillis())){
                log.info("token需要刷新");
                log.info("old token is {}",token);

                //开始刷新
//                1、创建token
                token = JwtUtils.generateTokenExpireInMinutes(payload.getUserInfo(), prop.getPrivateKey(), prop.getUser().getExpire());
//                2、放入cookie
                CookieUtils.newCookieBuilder()
                        .response(response)
                        .httpOnly(true) // js不能操作cookie
                        .domain(prop.getUser().getCookieDomain())
                        .name(prop.getUser().getCookieName())
                        .value(token)
                        .build();
                log.info("new token is {}",token);
            }
//        3、从payload中获取 userinfo，自描述信息
        return payload.getUserInfo();
        }catch (Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED,e);
        }
    }

    @Autowired
    private StringRedisTemplate redisTemplate;
    /**
     * 退出登录
     * @param request
     * @param response
     */
    public void logout(HttpServletRequest request, HttpServletResponse response) {

        try{
            //        1、获取token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            String jwtId = payload.getId();
            //过期时间
            Date expiration = payload.getExpiration();
//        计算离过期剩余的时间
            long time = expiration.getTime() - System.currentTimeMillis();
            //大于5秒，存redis
            if(time > 5000){
                redisTemplate.opsForValue().set(jwtId,"1",time, TimeUnit.MILLISECONDS);
            }
            CookieUtils.deleteCookie(prop.getUser().getCookieName(),prop.getUser().getCookieDomain(),response);

        }catch(Exception e){
            throw new LyException(ExceptionEnum.UNAUTHORIZED,e);
        }

    }
}
