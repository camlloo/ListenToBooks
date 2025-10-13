package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.SuggestIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author 杨健炜
 * 2025/10/13
 * 11:04
 **/
public interface SuggestIndexRepository extends ElasticsearchRepository<SuggestIndex,String> {
}
