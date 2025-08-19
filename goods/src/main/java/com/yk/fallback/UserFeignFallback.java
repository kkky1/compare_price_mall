package com.yk.fallback;

import com.yk.feign.UserFeignClient;
import org.springframework.stereotype.Service;
import util.ResponseResult;

@Service
public class UserFeignFallback implements UserFeignClient {


  @Override
  public ResponseResult getUserInfo(String authorization) {
    return ResponseResult.fail("请求业务繁忙请稍后再试");
  }

  @Override
  public ResponseResult validateToken(String authorization) {
    return ResponseResult.fail("请求业务繁忙请稍后再试");
  }
}
