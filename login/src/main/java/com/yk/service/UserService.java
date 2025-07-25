package com.yk.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.yk.domain.User;
import com.yk.dto.LoginForm;
import util.ResponseResult;


public interface UserService extends IService<User> {


    ResponseResult loginOrRegister(LoginForm loginForm);
}
