package com.yk.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;


@Component
public class TokenInterceptor implements HandlerInterceptor {

//    配置白名单
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/login",
            "/user/login",
            "/register",
            "/actuator"
    );


    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        // 白名单直接通过
        if (WHITE_LIST.contains(request.getRequestURI())) {
            return true;
        }
        // 非白名单需要验证token
        if (isValidToken(token)) {
            return true;  // 继续处理请求
        } else {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED); // 返回401未授权
            return false;  // 拦截请求
        }
    }

    private boolean isValidToken(String token) {
        // 这里可以进行Token的解析和验证
        return token != null && token.equals("VALID_TOKEN");
    }
}