package com.atguigu.tingshu.search.service;

import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;

import java.util.Collection;
import java.util.List;
import java.util.Map;

public interface SearchService {


    void upperAlbum(Long albumId);

    void lowerAlbum(Long albumId);

    AlbumSearchResponseVo search(AlbumIndexQuery queryVo);
    /**
     * 根据用户检索条件构建检索请求对象
     *
     * @param queryVo
     * @return
     */
    SearchRequest buildDSL(AlbumIndexQuery queryVo);
    /**
     * 解析ES检索结果，封装自定义结果
     *
     * @param response ·
     * @param queryVo
     * @return
     */
    AlbumSearchResponseVo parseResult(SearchResponse<AlbumInfoIndex> response, AlbumIndexQuery queryVo);

    /**
     * 查询首页每个三级分类下热门专辑列表
     *
     * @param category1Id
     * @return
     */
    List<Map<String, Object>> getCategory3Top6Hot(Long category1Id);
    /**
     * 新增提词库索引文档
     *
     * @param albumInfoIndex
     */
    void saveSuggestDoc(AlbumInfoIndex albumInfoIndex);
    /**
     * 关键字自动补全
     *
     * @param keyword
     * @return
     */
    List<String> completeSuggest(String keyword);
    /**
     * 解析提词结果
     * @param searchResponse
     * @param suggestName
     * @return
     */
    Collection<String> parseSuggestResult(SearchResponse<SuggestIndex> searchResponse, String suggestName);
}
