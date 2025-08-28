package com.yk.config;

import org.springframework.cloud.gateway.filter.ratelimit.KeyResolver;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.server.reactive.ServerHttpRequest;
import reactor.core.publisher.Mono;

import java.net.InetSocketAddress;
import java.util.List;

@Configuration
public class RateLimiterConfig {

    @Bean
    public KeyResolver ipKeyResolver() {
        return exchange -> {
            ServerHttpRequest request = exchange.getRequest();
            List<String> xff = request.getHeaders().get("X-Forwarded-For");
            if (xff != null && !xff.isEmpty()) {
                String firstIp = xff.get(0);
                int commaIdx = firstIp.indexOf(',');
                return Mono.just(commaIdx > 0 ? firstIp.substring(0, commaIdx).trim() : firstIp.trim());
            }
            InetSocketAddress remoteAddress = request.getRemoteAddress();
            String hostAddress = remoteAddress != null && remoteAddress.getAddress() != null
                    ? remoteAddress.getAddress().getHostAddress()
                    : "unknown";
            return Mono.just(hostAddress);
        };
    }
}


