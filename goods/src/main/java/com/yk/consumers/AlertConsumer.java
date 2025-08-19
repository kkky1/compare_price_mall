package com.yk.consumers;

import com.yk.dto.PriceAlertMessage;
import com.yk.feign.UserFeignClient;
import constants.ProductPrice;
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
    private RedisTemplate<String, Object> redisTemplate;

    @Resource
    private UserFeignClient userFeignClient;

    /**
     * 监听价格下降通知队列
     * 处理商品价格下降时的用户通知
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMqConstants.PRICE_DECREASE_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMqConstants.PRICE_DECREASE_EXCHANGE, type = "direct"),
            key = RabbitMqConstants.PRICE_DECREASE_ROUTING_KEY
    ))
    public void handlePriceDecrease(PriceAlertMessage alertMessage) {
        log.info("【收到价格下降通知】商品: {}, 价格: {} -> {}, 降价幅度: {:.2f}%", 
                alertMessage.getProductName(), 
                alertMessage.getOldPrice(), 
                alertMessage.getNewPrice(),
                alertMessage.getPriceDecreasePercent());

        try {
            // 处理每个订阅用户的通知
            int successCount = 0;
            int failCount = 0;
            
            for (Object userIdObj : alertMessage.getUserIds()) {
                try {
                    Long userId = Long.valueOf(userIdObj.toString());
                    
                    // 发送用户通知
                    boolean success = sendUserNotification(userId, alertMessage);
                    if (success) {
                        successCount++;
                    } else {
                        failCount++;
                    }
                    
                } catch (Exception e) {
                    log.error("处理用户通知失败, userId: {}", userIdObj, e);
                    failCount++;
                }
            }
            
            log.info("价格下降通知处理完成，成功: {} 个用户，失败: {} 个用户", successCount, failCount);
            
        } catch (Exception e) {
            log.error("处理价格下降通知失败", e);
        }
    }

    /**
     * 监听商品订阅队列
     * 用于延迟处理商品订阅请求
     */
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMqConstants.PRODUCT_ALERT_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMqConstants.DELAY_PRODUCT_ALERT_EXCHANGE, type = "direct"),
            key = RabbitMqConstants.PRODUCT_ALERT_ROUTING_KEY
    ))
    public void receive(Long productId) {
        log.info("【收到商品订阅消息】productId = {}", productId);

        try {
            // 从 Redis 获取列表，如果为 null 就初始化
            List<Long> productIds = (List<Long>) redisTemplate.opsForValue().get(PRODUCT_PRICE_DECREASE_IDS);
            if (productIds == null) {
                productIds = new ArrayList<>();
            }

            // 避免重复添加
            if (!productIds.contains(productId)) {
                productIds.add(productId);
                redisTemplate.opsForValue().set(PRODUCT_PRICE_DECREASE_IDS, productIds);
                log.info("【添加新监控商品】productId = {}", productId);
            }

            log.info("【更新后的监控商品列表】{}", productIds);
            
        } catch (Exception e) {
            log.error("处理商品订阅消息失败", e);
        }
    }

    /**
     * 发送用户通知
     * 这里需要根据你的通知方式来实现
     * @param userId 用户ID
     * @param alertMessage 价格提醒消息
     * @return 是否发送成功
     */
    private boolean sendUserNotification(Long userId, PriceAlertMessage alertMessage) {
        try {
            // 这里可以实现具体的通知逻辑
            // 比如调用邮件服务、短信服务、推送服务等
            
            String notificationContent = String.format(
                "亲爱的用户，您关注的商品【%s】价格下降了！\n" +
                "原价：%.2f\n" +
                "现价：%.2f\n" +
                "降价幅度：%.2f%%\n" +
                "降价金额：%.2f",
                alertMessage.getProductName(),
                alertMessage.getOldPrice(),
                alertMessage.getNewPrice(),
                alertMessage.getPriceDecreasePercent(),
                alertMessage.getPriceDecrease()
            );
            
            log.info("发送通知给用户 {}: {}", userId, notificationContent);
            
            // TODO: 实现具体的通知发送逻辑
            // 1. 调用邮件服务发送邮件
            // 2. 调用短信服务发送短信
            // 3. 调用推送服务发送推送
            // 4. 存储到数据库记录通知历史
            
            // 临时模拟发送成功
            Thread.sleep(100); // 模拟网络延迟
            
            return true;
            
        } catch (Exception e) {
            log.error("发送用户通知失败, userId: {}", userId, e);
            return false;
        }
    }
}
