package com.yk.consumers;

import com.yk.domain.Product;
import com.yk.mapper.ProductMapper;
import constants.RabbitMqConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

import static constants.ProductPrice.PRODUCT_PRICE_DECREASE_IDS;

@Component
@Slf4j
public class AlertConsumer {

    @Resource
    private ProductMapper productMapper;

    @Resource
    private RedisTemplate redisTemplate;

    // 监听死信队列
    // 监听死信队列
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMqConstants.PRODUCT_ALERT_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMqConstants.DELAY_PRODUCT_ALERT_EXCHANGE, type = "direct"),
            key = RabbitMqConstants.PRODUCT_ALERT_ROUTING_KEY
    ))
    public void receive(Long productId) {
        log.info("【收到消息】productId = {}", productId );

        // 从 Redis 获取列表，如果为 null 就初始化
        List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
        if (productIds == null) {
            productIds = new ArrayList<>();
        }

        // 避免重复添加
        if (!productIds.contains(productId)) {
            productIds.add(productId);
            redisTemplate.opsForValue().set(PRODUCT_PRICE_DECREASE_IDS, productIds);
        }

        log.info("【更新后的监控商品列表】{}", productIds);
    }

}
