package com.yk.config;

import com.yk.utils.JwtUtils;
import constants.UserConstants;
import jakarta.annotation.Resource;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Arrays;
import java.util.List;


@Component
public class TokenInterceptor implements HandlerInterceptor {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    RedisTemplate redisTemplate;

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
//      获取token
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (token.startsWith("Bearer ")){
            token = token.split("Bearer ")[1];
        }
        System.out.println("token = " + token);
        String usernameFromToken = jwtUtils.getUsernameFromToken(token);
        // 从redis中获取token
        String tokenFromRedis = (String) redisTemplate.opsForValue().get(UserConstants.LOGIN_USER_KEY + usernameFromToken);
        System.out.println("tokenFromRedis = " + tokenFromRedis);
        return tokenFromRedis != null ;
    }
}