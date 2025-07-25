package config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {

    @Bean
    @Primary
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        
        // 使用StringRedisSerializer确保key和value都是字符串格式
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        // 使用GenericJackson2JsonRedisSerializer处理复杂对象
        GenericJackson2JsonRedisSerializer jsonSerializer = new GenericJackson2JsonRedisSerializer();

        template.setConnectionFactory(connectionFactory);
        
        // key序列化方式 - 使用StringRedisSerializer避免乱码
        template.setKeySerializer(stringSerializer);
        // value序列化 - 使用JSON序列化器处理对象
        template.setValueSerializer(jsonSerializer);
        // hash key序列化
        template.setHashKeySerializer(stringSerializer);
        // hash value序列化
        template.setHashValueSerializer(jsonSerializer);
        
        // 设置默认序列化器
        template.setDefaultSerializer(jsonSerializer);
        
        // 初始化RedisTemplate
        template.afterPropertiesSet();
        
        return template;
    }

    @Bean
    public StringRedisTemplate stringRedisTemplate(RedisConnectionFactory connectionFactory) {
        StringRedisTemplate template = new StringRedisTemplate(connectionFactory);
        
        // 确保StringRedisTemplate使用StringRedisSerializer
        RedisSerializer<String> stringSerializer = new StringRedisSerializer();
        template.setKeySerializer(stringSerializer);
        template.setValueSerializer(stringSerializer);
        template.setHashKeySerializer(stringSerializer);
        template.setHashValueSerializer(stringSerializer);
        
        // 初始化
        template.afterPropertiesSet();
        
        return template;
    }
}