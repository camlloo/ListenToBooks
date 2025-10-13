package com.atguigu.tingshu.search.receiver;

import com.atguigu.tingshu.common.constant.KafkaConstant;
import com.atguigu.tingshu.search.service.SearchService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

/**
 * @author 杨健炜
 * 2025/10/11
 * 14:49
 **/
@Slf4j
@Component
public class SearchReceiver {
    @Autowired
    private SearchService searchService;
    /**
     * 监听专辑上架
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_UPPER )
    public void albumUpper(ConsumerRecord<String,String> record){
        //获取到发送的消息
        String value = record.value();
        if (StringUtils.isNotBlank(value)){
            log.info("[搜索服务]监听到专辑上架消息，专辑ID：{}",value);
            searchService.upperAlbum(Long.valueOf(value));
        }

    }

    /**
     * 监听专辑下架
     *
     * @param record
     */
    @KafkaListener(topics = KafkaConstant.QUEUE_ALBUM_LOWER)
    public void albumLower(ConsumerRecord<String, String> record) {
        //  获取到发送的消息
        String value = record.value();
        if (StringUtils.isNotBlank(value)) {
            log.info("[搜索服务]监听到专辑下架消息，专辑ID：{}",value);
            searchService.lowerAlbum(Long.valueOf(value));
        }
    }
}
