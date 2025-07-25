/*
package com.yk.config;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;



public class TokenInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String token = request.getHeader("Authorization");
        
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
}*/
