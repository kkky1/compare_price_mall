package com.yk.config;

import com.alibaba.csp.sentinel.adapter.gateway.sc.callback.BlockRequestHandler;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.reactive.function.server.ServerResponse;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

/**
 * 自定义Sentinel阻塞请求处理器
 * 兼容Spring Boot 3.x版本
 */
public class CustomBlockRequestHandler implements BlockRequestHandler {

    @Override
    public Mono<ServerResponse> handleRequest(ServerWebExchange exchange, Throwable ex) {
        if (ex instanceof BlockException) {
            return ServerResponse.status(HttpStatus.TOO_MANY_REQUESTS)
                    .contentType(MediaType.APPLICATION_JSON)
                    .bodyValue(createBlockResponse(ex));
        }
        
        return ServerResponse.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(createErrorResponse(ex));
    }

    private Map<String, Object> createBlockResponse(Throwable ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 429);
        response.put("message", "请求过于频繁，请稍后再试");
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", "/");
        response.put("exception", ex.getClass().getSimpleName());
        return response;
    }

    private Map<String, Object> createErrorResponse(Throwable ex) {
        Map<String, Object> response = new HashMap<>();
        response.put("code", 500);
        response.put("message", "服务器内部错误");
        response.put("timestamp", System.currentTimeMillis());
        response.put("path", "/");
        response.put("exception", ex.getClass().getSimpleName());
        return response;
    }
}
