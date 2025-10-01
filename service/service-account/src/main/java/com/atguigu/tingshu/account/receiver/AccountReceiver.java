package com.atguigu.tingshu.account.receiver;

import com.atguigu.tingshu.account.service.UserAccountService;
import com.atguigu.tingshu.common.constant.KafkaConstant;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.apache.commons.lang3.StringUtils;

/**
 * @author 杨健炜
 * 2025/9/27
 * 14:21
 **/
@Slf4j
@Component
public class AccountReceiver {
    @Autowired
    private UserAccountService userAccountService;
    /**
     * 监听消息并添加账户信息
     *
     * @param record
     */
@KafkaListener(topics = KafkaConstant.QUEUE_USER_REGISTER)
    public void processInitAccount(ConsumerRecord<String,String> record){
    String userId = record.value();
    if(StringUtils.isBlank(userId)){
        return;
    }
    log.info("收到消息userId{}",userId);
    userAccountService.saveUserAccount(Long.valueOf(userId));
}
}
