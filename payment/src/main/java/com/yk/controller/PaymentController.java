package com.yk.controller;


import com.yk.entity.BasicException;
import com.yk.entity.BusinessException;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/payment")  // 放在类上统一前缀
public class PaymentController {



    @GetMapping("/123")
    public String payment() {
        System.out.println("123");
        throw new BusinessException(10001, "10001");
    }

    @GetMapping("/456")
    public String payment2() {
        throw new BasicException(10002, "10002");
    }

}
