package com.yk.filter;

import com.alibaba.fastjson.JSON;
import com.yk.utils.JwtUtils;
import constants.UserConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Component
@Slf4j
public class AuthGlobalFilter implements GlobalFilter, Ordered {

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisTemplate<String, String> redisTemplate;

    // 白名单路径，不需要token验证
    private static final List<String> WHITE_LIST = Arrays.asList(
            "/gateway/123",
            "/gateway",
            "/login",
            "/user/login",
            "/payment/123",
            "/register",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();
        
        // 检查是否在白名单中
        if (isWhiteList(path)) {
            System.out.println("白名单路径，不需要token验证: " + path);
            return chain.filter(exchange);
        }
        
        // 获取token
        String token = getToken(request);

        System.out.println("获取的token为"+token);

        if (token == null || token.isEmpty()) {
            return handleNoToken(exchange);
        }
        
        // 验证token
        if (!validateToken(token)) {
            System.out.println("token无效: " + token);
            return handleInvalidToken(exchange);
        }
        
        // token有效，继续执行
        System.out.println("当前请求路径已放行"+path);
        return chain.filter(exchange);

    }

    /**
     * 检查路径是否在白名单中
     */
    private boolean isWhiteList(String path) {
        return WHITE_LIST.stream().anyMatch(path::startsWith);
    }

    /**
     * 从请求中获取token
     */
    private String getToken(ServerHttpRequest request) {
        // 优先从Header中获取
        String token = request.getHeaders().getFirst("Authorization");
        if (token != null && token.startsWith("Bearer ")) {
            return token.substring(7);
        }
        
        // 从Header中获取自定义token字段
        token = request.getHeaders().getFirst("token");
        if (token != null) {
            return token;
        }
        
        // 从查询参数中获取
        return request.getQueryParams().getFirst("token");
    }

    /**
     * 验证token
     */
    private boolean validateToken(String token) {
        // 这里实现你的token验证逻辑
        // 可以调用认证服务或者本地验证JWT

        // 1. JWT解析验证
        if (token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (jwtUtils.validateToken(token)){
            String loginUserKey = UserConstants.LOGIN_USER_KEY + jwtUtils.getUsernameFromToken(token);
            System.out.println("redis"+redisTemplate.opsForValue().get(loginUserKey));
        // 2. Redis中验证token是否存在
            if (redisTemplate.opsForValue().get(loginUserKey) == null){
                return false;
            }
        // 3. 调用认证服务验证
//            重新设置redis存活时间
            redisTemplate.expire(loginUserKey, 1, TimeUnit.HOURS);
            log.debug("当前请求路径已放行");
            return true;
        }

        
        return false;
    }

    /**
     * 处理没有token的情况
     */
    private Mono<Void> handleNoToken(ServerWebExchange exchange) {
        return buildErrorResponse(exchange, 401, "缺少访问令牌");
    }

    /**
     * 处理token无效的情况
     */
    private Mono<Void> handleInvalidToken(ServerWebExchange exchange) {
        return buildErrorResponse(exchange, 401, "访问令牌无效");
    }

    /**
     * 构建错误响应
     */
    private Mono<Void> buildErrorResponse(ServerWebExchange exchange, int code, String message) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.valueOf(code));
        response.getHeaders().add("Content-Type", "application/json;charset=UTF-8");
        
        Map<String, Object> result = new HashMap<>();
        result.put("success", false);
        result.put("code", code);
        result.put("message", message);
        result.put("timestamp", System.currentTimeMillis());
        
        String body = JSON.toJSONString(result);
        DataBuffer buffer = response.bufferFactory().wrap(body.getBytes(StandardCharsets.UTF_8));
        
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return -100; // 优先级，数字越小优先级越高
    }
}