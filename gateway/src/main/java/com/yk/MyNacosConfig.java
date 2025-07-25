package com.yk;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "my")
public class MyNacosConfig {
    private String name;
    private Integer age;
}
