package com.atguigu.tingshu.common.login;

import java.lang.annotation.*;

/**
 * @author 杨健炜
 * 2025/9/23
 * 21:32
 **/
/*
*认证自定义注解
* 属性：要求是否必须登录属性（true：要求必须登录）
* 元注解：
 * @Target：注解使用位置:指定方法，类（接口），属性，构造器，构造器参数
 * @Retention：注解生命周期 例如设置为SOURCE 编译后注解没了;CLASS 运行是注解没了
 * @Inherited 该注解是否可以被继承
 * @Documented javadoc命令是否生成文档 javadoc -encoding UTF-8 -charset UTF-8 Test.java
*/
    @Target({ElementType.METHOD})
    @Retention(RetentionPolicy.RUNTIME)
    @Inherited
    @Documented
    public @interface KingLogin {
    //是否要求用户必须登录：默认为必须登录
    boolean required() default true;
    }
