package com.yk.feign;

import com.yk.fallback.UserFeignFallback;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import util.ResponseResult;

/**
 * 用户服务Feign客户端
 * 用于调用login服务获取用户信息
 */
@FeignClient(name = "cloud-login-service",fallback = UserFeignFallback.class)
public interface UserFeignClient {

    /**
     * 获取当前用户信息
     * @param authorization 认证token
     * @return 用户信息
     */
    @GetMapping("/user/info")
    ResponseResult getUserInfo(@RequestHeader("Authorization") String authorization);

    /**
     * 验证token有效性
     * @param authorization 认证token
     * @return 验证结果
     */
    @GetMapping("/validate")
    ResponseResult validateToken(@RequestHeader("Authorization") String authorization);
}
