package com.leyou.order.interceptors;

import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.threadlocatls.UserHolder;
import com.leyou.common.utils.CookieUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserInterceptor implements HandlerInterceptor {

    /**
     * 从request中获取token，解析出userid
     * 放入当前线程的threadlocal中
     * @param request
     * @param response
     * @param handler
     * @return
     * @throws Exception
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        try{
            //获取token
            String token = CookieUtils.getCookieValue(request, "LY_TOKEN");
            //解析payload
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, UserInfo.class);
            //把用户信息放入 当前线程的 threadlocal中
            UserHolder.setUser(payload.getUserInfo().getId());
            return true;
        }catch(Exception e){
            e.printStackTrace();
            return false;
        }

    }

    /**
     * 把数据从当前线程的threadlocal中清除掉
     * @param request
     * @param response
     * @param handler
     * @param ex
     * @throws Exception
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        UserHolder.deleteUser();
    }
}
