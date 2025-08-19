package com.yk.config;

import feign.Logger;
import feign.Request;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * OpenFeign配置类
 * 用于自定义Feign客户端的配置
 */
@Configuration
public class FeignConfig {

    /**
     * 配置Feign的日志级别
     */
    @Bean
    public Logger.Level feignLoggerLevel() {
        return Logger.Level.FULL; // 记录所有请求和响应的明细
    }

    /**
     * 配置Feign的超时时间
     */
    @Bean
    public Request.Options options() {
        return new Request.Options(
            5000,  // 连接超时时间（毫秒）
            10000  // 读取超时时间（毫秒）
        );
    }
}
