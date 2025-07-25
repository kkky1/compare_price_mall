package com.yk.controller;

import com.yk.GatewayConfigProperties;
import com.yk.MyNacosConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("gateway")
public class controller {

    @Autowired
    private GatewayConfigProperties gatewayConfigProperties;

    @Autowired
    private MyNacosConfig config;



    @GetMapping("/123")
    public String payment(){
        String uri = gatewayConfigProperties.getRoutes().get(0).getUri();
        return uri;
    }



    @GetMapping
    public String hello(){
        System.out.println(config.getName());
        return config.getAge().toString();
    }



}
