package com.yk.controller;

import com.yk.dto.LoginForm;
import com.yk.service.UserService;
import com.yk.utils.JwtUtils;
import jakarta.annotation.Resource;
import org.springframework.web.bind.annotation.*;
import util.ResponseResult;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/user")
public class UserController {

    @Resource
    JwtUtils jwtUtils;

    @Resource
    private UserService userService;


    @PostMapping("/login")
    public ResponseResult login(@RequestBody LoginForm loginForm) {
        return userService.loginOrRegister(loginForm);
    }



}
