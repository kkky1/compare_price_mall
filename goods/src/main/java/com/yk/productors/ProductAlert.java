package com.yk.productors;

import constants.RabbitMqConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessagePostProcessor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/product")
@Slf4j
public class ProductAlert {


    @Resource
    private RabbitTemplate rabbitTemplate;


    @GetMapping("/alert")
    public void alert(@RequestParam("productId") String productId) {
        // 向指定的交换机发送消息
        rabbitTemplate.convertAndSend(
                "delay.exchange", // 交换机
                "delay.routing",
                productId,                                     // 消息内容（这里直接用 productId）
                new MessagePostProcessor() {                   // 设置消息属性
                    @Override
                    public Message postProcessMessage(Message message) throws AmqpException {
                        // 设置消息的过期时间（TTL = 10000 毫秒）
                        message.getMessageProperties().setExpiration("10000");
                        return message;
                    }
                }
        );

        log.info("发送延迟消息成功");
    }



}