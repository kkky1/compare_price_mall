package com.yk.consumers;

import constants.RabbitMqConstants;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.Exchange;
import org.springframework.amqp.rabbit.annotation.Queue;
import org.springframework.amqp.rabbit.annotation.QueueBinding;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class PriceDecreaseListener {


//    声明交换机
    @RabbitListener(bindings = @QueueBinding(
            value = @Queue(value = RabbitMqConstants.PRICE_DECREASE_QUEUE, durable = "true"),
            exchange = @Exchange(value = RabbitMqConstants.PRICE_DECREASE_EXCHANGE, type = "direct"),
            key = RabbitMqConstants.PRICE_DECREASE_ROUTING_KEY
    ))
    public void receive(String productId) {
        log.info("【收到价格下降消息】productId = {}", productId);
    }

}
