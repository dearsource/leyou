package com.leyou.gateway.auth.filter;


import com.leyou.common.auth.entity.Payload;
import com.leyou.common.auth.entity.UserInfo;
import com.leyou.common.auth.utils.JwtUtils;
import com.leyou.common.enums.ExceptionEnum;
import com.leyou.common.utils.CookieUtils;
import com.leyou.config.FilterProperties;
import com.leyou.config.JwtProperties;
import com.netflix.zuul.ZuulFilter;
import com.netflix.zuul.context.RequestContext;
import com.netflix.zuul.exception.ZuulException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cloud.netflix.zuul.filters.support.FilterConstants;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 拦截请求
 */

@Slf4j
@Component
@EnableConfigurationProperties({JwtProperties.class, FilterProperties.class})
public class AuthFilter extends ZuulFilter {
    @Autowired
    private JwtProperties prop;
    @Autowired
    private FilterProperties filterprop;

    @Override
    public String filterType() {
        //前置过滤器
        return FilterConstants.PRE_TYPE;
    }

    @Override
    public int filterOrder() {
        return FilterConstants.FORM_BODY_WRAPPER_FILTER_ORDER - 1;
    }

    @Override
    public boolean shouldFilter() {
        //过去当前请求的path
        List<String> allowPaths = filterprop.getAllowPaths();
        RequestContext ctx = RequestContext.getCurrentContext();
        HttpServletRequest request = ctx.getRequest();
        String path = request.getRequestURI();
        //通过白名单 ，判断当前的path是否需要过滤
        return !isAllowPath(path);
    }

    private boolean isAllowPath(String requestURI) {
        // 定义一个标记
        boolean flag = false;
        // 遍历允许访问的路径
        List<String> allowPaths = filterprop.getAllowPaths();
        for (String allowPath : allowPaths) {
            if (requestURI.startsWith(allowPath)) {
                flag = true;
                break;
            }
        }
        return flag;
    }

    @Override
    public Object run()  {
//        是否登录：获取token，解密token，如果有异常，直接返回，没有登录
        RequestContext ctx = RequestContext.getCurrentContext();
        try {
//获取request
            HttpServletRequest request = ctx.getRequest();
//        获取token
            String token = CookieUtils.getCookieValue(request, prop.getUser().getCookieName());
            //用jwtutils加密token
            Payload<UserInfo> payload = JwtUtils.getInfoFromToken(token, prop.getPublicKey(), UserInfo.class);
            // 解析没有问题，获取用户
            UserInfo user = payload.getUserInfo();
            // 获取用户角色，查询权限
            String role = user.getRole();
            // 获取当前资源路径
            String path = request.getRequestURI();
            String method = request.getMethod();
            // TODO 判断权限，此处暂时空置，等待权限服务完成后补充
            log.info("【网关】用户{},角色{}。访问服务{} : {}，", user.getUsername(), role, method, path);
        } catch (Exception e) {
            //如果没有登录，返回未登录 401
            ctx.setSendZuulResponse(false);
            ctx.setResponseStatusCode(ExceptionEnum.UNAUTHORIZED.getStatus());
        }

        return null;
    }
}
