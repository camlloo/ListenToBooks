package com.atguigu.tingshu;

import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.search.repository.SuggestIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.elasticsearch.core.suggest.Completion;

import java.util.concurrent.CompletableFuture;

@SpringBootTest
class ServiceSearchApplicationTest {

    @Autowired
    private SearchService searchService;


    /**
     *   //TODO 每批次（采用多线程线程处理）新增200条文档； 每次逻辑：获取200个专辑索引库文档集合；调用批量操作API
     * 不严谨批量导入：
     * TODO：ES中批量新增API https://www.elastic.co/guide/en/elasticsearch/client/java-api-client/current/indexing-bulk.html
     */
    @Test
    public void test() {
        for (long i = 1; i <= 1577; i++) {
            searchService.upperAlbum(i);
        }
    }
@Autowired
private SuggestIndexRepository suggestIndexRepository;
    @Test
    public void suggestIndex() {
        String text = "经典留声机";
        SuggestIndex suggestIndex = new SuggestIndex();
        suggestIndex.setId(IdUtil.fastSimpleUUID());
        suggestIndex.setTitle(text);
        suggestIndex.setKeyword(new Completion(new String[]{text}));
        suggestIndex.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(text," ")}));
        suggestIndex.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(text," ")}));
        suggestIndexRepository.save(suggestIndex);
    }
}