package com.atguigu.tingshu.user.api;

import com.atguigu.tingshu.common.login.KingLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.user.service.UserInfoService;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@Tag(name = "微信授权登录接口")
@RestController
@RequestMapping("/api/user/wxLogin")
@Slf4j
public class WxLoginApiController {

    @Autowired
    private UserInfoService userInfoService;

    @GetMapping("/wxLogin/{code}")
    public Result<Map<String,String>> wxLogin(@PathVariable String code){
        Map<String,String> mapResult =   userInfoService.wxLogin(code);
        return Result.ok(mapResult);
    }
    @GetMapping("/getUserInfo")
    @KingLogin(required = true)
    public Result<UserInfoVo> getUserInfo(){
        Long userId = AuthContextHolder.getUserId();
      UserInfoVo userInfoVo =  userInfoService.getUserInfoByUserId(userId);
      return Result.ok(userInfoVo);
    }
    @KingLogin(required = true)
    @Operation(summary = "更新用户信息")
    @PostMapping("/updateUser")
    public Result updateUser(@RequestBody @Validated UserInfoVo userInfoVo){
        Long userId = AuthContextHolder.getUserId();
        userInfoVo.setId(userId);
        userInfoService.updataUser(userInfoVo);
        return Result.ok();
    }
}
