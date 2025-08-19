package com.yk.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.lang.UUID;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.yk.domain.User;
import com.yk.dto.LoginForm;
import com.yk.mapper.UserMapper;
import com.yk.service.UserService;
import com.yk.utils.JwtUtils;
import constants.UserConstants;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import util.ResponseResult;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User>  implements UserService {

    @Resource
    private UserMapper userMapper;

    @Resource
    private JwtUtils jwtUtils;

    @Resource
    private RedisTemplate redisTemplate;

    @Override
    public ResponseResult loginOrRegister(LoginForm loginForm) {

//        设置一个redis: 防止用户重复登录
        String loginUserKey = UserConstants.LOGIN_USER_KEY + loginForm.getUsername();

//        先校验用户是否存在
        User user = lambdaQuery().eq(User::getUsername, loginForm.getUsername()).one();
//        1.如果存在，直接返回
        if (!BeanUtil.isEmpty(user)){
//            1.1 校验密码是否正确，正确则返回token，不正确则返回错误信息
            if (!user.getPassword().equals(loginForm.getPassword())){
                return ResponseResult.fail("密码错误");
            }
            // 1.2 密码正确，返回token
                // 1.2.1 查看redis是否有该用户
            if (!BeanUtil.isEmpty(redisTemplate.opsForValue().get(loginUserKey))){
                return ResponseResult.fail("用户已登录");
            }
            String jsonUser = JSONUtil.toJsonStr(user);
            redisTemplate.opsForValue().set(loginUserKey, jsonUser, 60, TimeUnit.MINUTES);
            String token = jwtUtils.generateToken(user.getId().toString(), user.getUsername());
            token = "Bearer " + token;
            return ResponseResult.success(token);
        }else{
//        2.如果不存在，创建新用户
//            2.1 直接创建新用户
            User newUser = new User();
            newUser.setId(System.currentTimeMillis());
            newUser.setUsername(loginForm.getUsername());
            newUser.setPassword(loginForm.getPassword());
            newUser.setCreatedAt(new Date());
            userMapper.insert(newUser);
//            2.2 返回token
            String token = jwtUtils.generateToken(newUser.getId().toString(), newUser.getUsername());
            return ResponseResult.success(token);
        }


    }

    @Override
    public ResponseResult getInfo(String token) {
//        获取当前登录的用户
        if(token.startsWith("Bearer ")){
            token = token.substring(7);
        }
        if (token.startsWith("Bearer ")){
            token = token.split("Bearer ")[1];
        }
        String userId = jwtUtils.getUserIdFromToken(token);
        System.out.println("userId"+userId);
        if (userId == null){
            return ResponseResult.fail("用户未登录");
        }
        User user = userMapper.selectById(Long.parseLong(userId));
        if (user == null){
            return ResponseResult.fail("用户不存在");
        }
        return ResponseResult.success(user);
    }


}
