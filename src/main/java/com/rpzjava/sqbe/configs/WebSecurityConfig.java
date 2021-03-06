package com.rpzjava.sqbe.configs;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.rpzjava.sqbe.utils.JwtUtils;
import com.rpzjava.sqbe.utils.RedisUtils;
import com.rpzjava.sqbe.utils.ResultUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.*;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.ServletOutputStream;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * 登录拦截配置
 */
@Configuration
public class WebSecurityConfig extends WebMvcConfigurationSupport {

    private final RedisUtils redisUtils;
    private final String[] allWhiteList = new String[] {
            "/user/", "/user/all", "/user/*", "/login",
            "/post/*"
    };
    private final List<String> getWhiteList = Arrays.asList(
            "/post/latest", "/hello", "/tag/withPostCount",
            "/post/pagination"
    );

    public WebSecurityConfig(RedisUtils redisUtils) {
        this.redisUtils = redisUtils;
    }

    /**
     * 注入Bean 让 Spring 扫描 SecurityInterceptor
     * 不然过滤器不起作用
     */

    @Bean
    public SecurityInterceptor getSecurityInterceptor() {
        return new SecurityInterceptor();
    }

    /**
     * 配置自定义拦截拦截器
     */
    public void addInterceptors(InterceptorRegistry registry) {
        InterceptorRegistration addInterceptor = registry.addInterceptor(getSecurityInterceptor());

        List<String> passList = new ArrayList<>();
        Collections.addAll(passList, allWhiteList);
        addInterceptor.excludePathPatterns(passList);
        addInterceptor.addPathPatterns("/**");//拦截其他所有请求

    }

    private class SecurityInterceptor extends HandlerInterceptorAdapter {
        /**
         * 在业务处理器处理请求之前被调用
         */
        @Override
        public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
            // 在 GET 请求的白名单中
            if (request.getMethod().equals("GET") && getWhiteList.contains(request.getRequestURI())) {
                return true;
            }

            ServletOutputStream out = response.getOutputStream();//创建一个输出流
            OutputStreamWriter ow = new OutputStreamWriter(out, StandardCharsets.UTF_8);//设置编码格式,防止汉字乱码

            Cookie[] cookies = request.getCookies();//获取 Token
            if (request.getCookies() != null) {
                for (Cookie c : cookies) {
                    if (c.getName().equals("gssq_token")) { //找到 Token Cookie
                        if (redisUtils.hasKey(c.getValue())) {
                            redisUtils.expire(c.getValue(), JwtUtils.TOKEN_EXPIRE_TIME); //如果 Token 存在 重新刷新过期时间 并放行
                            return true;
                        } else {
                            ow.write(JSON.toJSONString(ResultUtils.error("token token 不正确!", 99401L)));
                            ow.flush();//冲刷出流，将所有缓冲的数据发送到目的地
                            ow.close();//关闭流
                            return false;
                        }
                    }
                }
            }

            // 遍历完毕也没找到则报空
            ow.write(JSON.toJSONString(ResultUtils.error("token 为空, 请先登录!", 99401L)));
            ow.flush();//冲刷出流，将所有缓冲的数据发送到目的地
            ow.close();//关闭流
            return false;//拦截
        }
    }

}