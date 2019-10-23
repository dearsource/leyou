package com.leyou.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
@EnableConfigurationProperties(CORSProperties.class)
public class GlobalCORSConfig {

    @Bean
    public CorsFilter corsConfig(CORSProperties prop){
        //        1.添加cors的配置信息
        CorsConfiguration cf = new CorsConfiguration();
//          允许访问的域
//        cf.addAllowedOrigin("http://manage.leyou.com");
        cf.setAllowedOrigins(prop.getAllowedOrigins());
//          是否允许发送cookie
        cf.setAllowCredentials(prop.getAllowedCredentials());
//          允许的请求方式
        cf.setAllowedMethods(prop.getAllowedMethods());
//        cf.addAllowedMethod(HttpMethod.GET);
//        cf.addAllowedMethod(HttpMethod.POST);
//        cf.addAllowedMethod(HttpMethod.PUT);
//        cf.addAllowedMethod(HttpMethod.DELETE);
//        cf.addAllowedMethod(HttpMethod.HEAD);
//          允许的头信息
//        cf.addAllowedHeader("*");
        cf.setAllowedHeaders(prop.getAllowedHeaders());
//          访问有效期
        cf.setMaxAge(prop.getMaxAge());
//        cf.setMaxAge(3600L);
//       2.添加映射路径，我们拦截一切请求
        UrlBasedCorsConfigurationSource urlbase = new UrlBasedCorsConfigurationSource();
//        urlbase.registerCorsConfiguration("/**",cf);
        urlbase.registerCorsConfiguration(prop.getFilterPath(),cf);
//       3.返回新的CORSFilter
        return new  CorsFilter(urlbase);
    }

}
