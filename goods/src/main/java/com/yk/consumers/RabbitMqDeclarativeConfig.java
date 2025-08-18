package com.yk.consumers;


import constants.RabbitMqConstants;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.DirectExchange;
import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class RabbitMqDeclarativeConfig {

    @Bean
    public Queue delayQueue() {
        Map<String, Object> args = new HashMap<>();
        args.put("x-dead-letter-exchange", RabbitMqConstants.DELAY_PRODUCT_ALERT_EXCHANGE);
        args.put("x-dead-letter-routing-key", RabbitMqConstants.PRODUCT_ALERT_ROUTING_KEY);
        return new Queue("delay.queue", true, false, false, args);
    }

    @Bean
    public DirectExchange delayExchange() {
        return new DirectExchange("delay.exchange");
    }
    @Bean
    public Binding delayBinding(Queue delayQueue, DirectExchange delayExchange) {
        return BindingBuilder.bind(delayQueue).to(delayExchange).with("delay.routing");
    }
}
