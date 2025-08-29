package com.atguigu.tingshu.payment.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix="wechat.pay") //读取节点
@Data
public class WxPayConstantProperties {

    private String appid;
    private String mchid;
    private String appkey;
    private String notifyUrl;
}
