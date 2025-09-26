package com.atguigu.tingshu.common.login;

import cn.hutool.http.server.HttpServerRequest;
import cn.hutool.http.server.HttpServerResponse;
import com.atguigu.tingshu.common.constant.RedisConstant;
import com.atguigu.tingshu.common.execption.GuiguException;
import com.atguigu.tingshu.common.result.ResultCodeEnum;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.user.UserInfo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * @author 杨健炜
 * 2025/9/23
 * 22:09
 **/
@Slf4j
@Aspect
@Component
public class KingLoginAspect {
    @Autowired
    private RedisTemplate redisTemplate;
    @SneakyThrows//自动对方法体try catch
    @Around("execution(* com.atguigu.tingshu.*.api.*.*(..)) && @annotation(kingLogin)")
    public Object kingLonginAspect(ProceedingJoinPoint joinPoint,KingLogin kingLogin) {
       Object object = new Object();
        log.info("前置通知逻辑...");
        //1.尝试从请求对象中获取用户Token
        RequestAttributes requestAttributes = RequestContextHolder.getRequestAttributes();
        //RequestAttributes是接口  ServletRequestAttributes接口实现类
        ServletRequestAttributes sra = (ServletRequestAttributes) requestAttributes;
        HttpServletRequest request = sra.getRequest();
        HttpServletResponse response = sra.getResponse();
        //2.根据Token获取用户信息（用户ID，用户昵称）
        //2.1获取用户token
        String token = request.getHeader("token");
        //2.2拼接用户登录时候存入Redis中的Key
        String loginKey = RedisConstant.USER_LOGIN_KEY_PREFIX+token;
        //2.3根据key查询用户信息
        UserInfoVo userInfoVo = (UserInfoVo) redisTemplate.opsForValue().get(loginKey);
        if(kingLogin.required()){
            if (userInfoVo == null) {
                //要求用户必须登录才可以，如果此时用户信息为空抛出异常，小程序员引导用户进行登录
                throw new GuiguException(ResultCodeEnum.LOGIN_AUTH);
            }
        }
        //3.将用户信息隐式传入，当前线程生命周期内获取到用户信息
        if(userInfoVo != null){
            //说明用户登录-将用户ID跟用户名称存入ThreadLocal
            AuthContextHolder.setUserId(userInfoVo.getId());
        }
        //执行目标方法->切入点方法（被增强方法）
        object =joinPoint.proceed();
        log.info("后置通知逻辑...");
        //4.避免ThreadLocal导致内存泄漏，产生OOM问题，立即将ThreadLocal中数据清理
        AuthContextHolder.removeUserId();
        return object;
    }
}
