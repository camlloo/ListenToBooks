package com.atguigu.tingshu.search.repository;

import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * @author 杨健炜
 * 2025/10/9
 * 22:15
 **/
public interface AlbumInfoIndexRepository extends ElasticsearchRepository<AlbumInfoIndex,Long> {
}
