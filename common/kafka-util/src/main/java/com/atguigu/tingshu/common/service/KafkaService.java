package com.atguigu.tingshu.common.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.kafka.support.SendResult;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class KafkaService {

    private static final Logger logger = LoggerFactory.getLogger(KafkaService.class);

    @Autowired
    private KafkaTemplate kafkaTemplate;

    /**
     * 发送消息工具方法
     *
     * @param topic 话题名称
     * @param data  业务消息
     */
    public void sendMessage(String topic, Object data) {
        this.sendMessage(topic, null, data);
    }

    /**
     * 发送消息工具方法
     *
     * @param topic 话题名称
     * @param key   业务消息Key
     * @param data  业务消息
     */
    public void sendMessage(String topic, String key, Object data) {
        //1.调用模板发送消息
        String value = String.valueOf(data); // 统一转为字符串
        CompletableFuture completableFuture = kafkaTemplate.send(topic, key, value);
        //2.获取发送消息结果
        completableFuture.thenAcceptAsync(result -> {
            logger.info("发送消息成功：{}", result);
        }).exceptionally(e -> {
           //3.获取发送异常后错误信息
            logger.error("发送消息失败：{}", e);
            return null;
        });
    }
}
