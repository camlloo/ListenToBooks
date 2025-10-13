package com.atguigu.tingshu.search.service.impl;
import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.lang.Assert;
import cn.hutool.core.util.IdUtil;
import cn.hutool.extra.pinyin.PinyinUtil;
import co.elastic.clients.elasticsearch.ElasticsearchClient;
import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.Aggregate;
import co.elastic.clients.elasticsearch._types.aggregations.LongTermsAggregate;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.NestedQuery;
import co.elastic.clients.elasticsearch.core.SearchRequest;
import co.elastic.clients.elasticsearch.core.SearchResponse;
import co.elastic.clients.elasticsearch.core.search.CompletionSuggestOption;
import co.elastic.clients.elasticsearch.core.search.Hit;
import co.elastic.clients.elasticsearch.core.search.Suggestion;
import co.elastic.clients.json.JsonData;
import com.alibaba.fastjson.JSON;
import com.atguigu.tingshu.album.AlbumInfoFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.atguigu.tingshu.model.search.AlbumInfoIndex;
import com.atguigu.tingshu.model.search.AttributeValueIndex;
import com.atguigu.tingshu.model.search.SuggestIndex;
import com.atguigu.tingshu.query.search.AlbumIndexQuery;
import com.atguigu.tingshu.search.repository.AlbumInfoIndexRepository;
import com.atguigu.tingshu.search.repository.SuggestIndexRepository;
import com.atguigu.tingshu.search.service.SearchService;
import com.atguigu.tingshu.user.client.UserFeignClient;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.search.AlbumInfoIndexVo;
import com.atguigu.tingshu.vo.search.AlbumSearchResponseVo;
import com.atguigu.tingshu.vo.user.UserInfoVo;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.elasticsearch.core.suggest.Completion;
import org.springframework.stereotype.Service;

import javax.naming.directory.SearchResult;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.stream.Collectors;


@Slf4j
@Service
@SuppressWarnings({"all"})
public class SearchServiceImpl implements SearchService {
    //专辑索引库名称
    private static final String INDEX_NAME = "albuminfo";
@Autowired
private SuggestIndexRepository suggestIndexRepository;
    @Autowired
    private AlbumInfoIndexRepository albumInfoIndexRepository;
    @Autowired
    private AlbumInfoFeignClient albumInfoFeignClient;
    @Autowired
    private UserFeignClient userFeignClient;
    @Autowired
    private ThreadPoolExecutor threadPoolExecutor;
    @Autowired
    private ElasticsearchClient elasticsearchClient;
    /**
     * 上架专辑到索引库
     * @param albumId
     */
    @Override
    public void upperAlbum(Long albumId) {
        //1.创建索引库文档对象
        AlbumInfoIndex albumInfoIndex = new AlbumInfoIndex();
        //2.远程调用专辑服务获取专辑以及属性列表信息，为索引库文档对象中相关属性赋值
        CompletableFuture<AlbumInfo> albumInfoIndexCompletableFuture = CompletableFuture.supplyAsync(() -> {
            AlbumInfo albumInfo = albumInfoFeignClient.getAlbumInfo(albumId).getData();
            Assert.notNull(albumInfo, "专辑信息为空"+albumId);
            //2.1 拷贝专辑基本信息
            BeanUtil.copyProperties(albumInfo, albumInfoIndex);
            //2.2处理专辑属性列表
            List<AlbumAttributeValue> albumAttributeValueVoList = albumInfo.getAlbumAttributeValueVoList();
            if (CollectionUtil.isNotEmpty(albumAttributeValueVoList)) {
                List<AttributeValueIndex> attributeValueIndexList = albumAttributeValueVoList.stream().map(albumAttributeValue -> {
                    return BeanUtil.copyProperties(albumAttributeValue, AttributeValueIndex.class);
                }).collect(Collectors.toList());
                albumInfoIndex.setAttributeValueIndexList(attributeValueIndexList);
            }
            return albumInfo;
        }, threadPoolExecutor);
        //3.远程调用用户服务获取用户信息，为索引库文档对象中相关属性赋值
        CompletableFuture<Void> userInfoCompletableFuture = albumInfoIndexCompletableFuture.thenAcceptAsync(albumInfo -> {
            UserInfoVo userInfoVo = userFeignClient.getUserInfoByUserId(albumInfo.getUserId()).getData();
            Assert.notNull(userInfoVo, "主播信息不存在");
            albumInfoIndex.setAnnouncerName(userInfoVo.getNickname());
        }, threadPoolExecutor);
        //4.远程调用专辑服务获取分类信息，为索引库文档对象中相关属性赋值
        CompletableFuture<Void> categoryCompletableFuture = albumInfoIndexCompletableFuture.thenAcceptAsync(albumInfo -> {
            BaseCategoryView baseCategoryView = albumInfoFeignClient.getCategoryViewBy3Id(albumInfo.getCategory3Id()).getData();
            Assert.notNull(baseCategoryView, "标题信息不存在");
            albumInfoIndex.setCategory1Id(baseCategoryView.getCategory1Id());
            albumInfoIndex.setCategory2Id(baseCategoryView.getCategory2Id());
        }, threadPoolExecutor);
        CompletableFuture.allOf(albumInfoIndexCompletableFuture,userInfoCompletableFuture,categoryCompletableFuture).join();
        //5.手动随机生成统计值，为索引库文档对象中相关属性赋值
        int num1 = new Random().nextInt(1000);
        int num2 = new Random().nextInt(800);
        int num3 = new Random().nextInt(500);
        int num4 = new Random().nextInt(100);
        albumInfoIndex.setPlayStatNum(num1);
        albumInfoIndex.setSubscribeStatNum(num2);
        albumInfoIndex.setBuyStatNum(num3);
        albumInfoIndex.setCommentStatNum(num4);
        double hotScore = num1 * 0.1 + num2 * 0.2 + num3 * 0.4 + num4 * 0.3;
        albumInfoIndex.setHotScore(hotScore);
        //6.调用持久层新增文档
        albumInfoIndexRepository.save(albumInfoIndex);
        //6 TODO 将上架专辑中 专辑标题 专辑作者名称 存入提词索引库
        this.saveSuggestDoc(albumInfoIndex);
    }

    @Override
    public void lowerAlbum(Long albumId) {
        albumInfoIndexRepository.deleteById(albumId);
    }
    /**
     * 根据关键字、分类ID、属性/属性值进行专辑检索
     *
     * @param queryVo
     * @return
     */
    @Override
    public AlbumSearchResponseVo search(AlbumIndexQuery queryVo) {
        try {
            //1.创建检索请求对象
            SearchRequest searchRequest = this.buildDSL(queryVo);
            System.err.println("本次检索DSL：");
            System.err.println(searchRequest.toString());
            //2.执行检索
            SearchResponse<AlbumInfoIndex> response = elasticsearchClient.search(searchRequest, AlbumInfoIndex.class);
            //3.解析ES响应数据
            return this.parseResult(response,queryVo);
        } catch (Exception e) {
            log.error("【搜索服务】检索专辑异常：{}", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 根据用户检索条件构建检索请求对象
     *
     * @param queryVo
     * @return
     */
    @Override
    public SearchRequest buildDSL(AlbumIndexQuery queryVo) {
        //1.创建检索请求构建器对象，指定检索索引库名称
        SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
        searchRequestBuilder.index(INDEX_NAME);

        //2.设置查询条件 请求体参数中"query" 包含：关键字，分类ID过滤，属性过滤
        BoolQuery.Builder allBoolQueryBuilder = new BoolQuery.Builder();
        //2.1 设置关键字查询 采用bool查询 满足：标题中包含或者介绍中包含或者等值主播名称 任意条件即可返回文档
        String keyword = queryVo.getKeyword();
        if (StringUtils.isNotBlank(keyword)) {
            //2.1.1 创建关键字查询bool查询对象 内部：三个条件满足其一即可
            BoolQuery.Builder keywordBoolQueryBuilder = new BoolQuery.Builder();
            keywordBoolQueryBuilder.should(s -> s.match(m -> m.field("albumTitle").query(keyword)));
            keywordBoolQueryBuilder.should(s -> s.match(m -> m.field("albumIntro").query(keyword)));
            keywordBoolQueryBuilder.should(s -> s.term(t -> t.field("announcerName").value(keyword)));
            //2.1.2 将关键字bool查询对象放入最大bool查询对象中
            allBoolQueryBuilder.must(keywordBoolQueryBuilder.build()._toQuery());
        }
        //2.2 设置1、2、3级分类过滤数据 使用Filter不会对文档进行算分，且会进行缓存命中数据
        if (queryVo.getCategory1Id() != null) {
            allBoolQueryBuilder.filter(f -> f.term(t -> t.field("category1Id").value(queryVo.getCategory1Id())));
        }
        if (queryVo.getCategory2Id() != null) {
            allBoolQueryBuilder.filter(f -> f.term(t -> t.field("category2Id").value(queryVo.getCategory2Id())));
        }
        if (queryVo.getCategory3Id() != null) {
            allBoolQueryBuilder.filter(f -> f.term(t -> t.field("category3Id").value(queryVo.getCategory3Id())));
        }
        //2.3 设置若干项属性及属性值过滤条件 每循环一次构建一个Nested查询对象 某个属性过滤条件形式 属性id:属性值id
        List<String> attributeList = queryVo.getAttributeList();
        if (CollectionUtil.isNotEmpty(attributeList)) {
            //属性id:属性值id
            for (String attribute : attributeList) {
                String[] split = attribute.split(":");
                if (split != null && split.length == 2) {
                    String attrId = split[0];
                    String attrValueId = split[1];
                    //2.3.1 创建Nested嵌套查询对象
                    NestedQuery nestedQuery = NestedQuery.of(
                            o->o.path("attributeValueIndexList")
                                    .query(q->q.bool(
                                            b->b.must(
                                                    m->m.term(t->t.field("attributeValueIndexList.attributeId").value(attrId))
                                            ).must(
                                                    m->m.term(t->t.field("attributeValueIndexList.valueId").value(attrValueId))
                                            )
                                    ))
                    );
                    allBoolQueryBuilder.filter(nestedQuery._toQuery());
                }
            }
        }

        searchRequestBuilder.query(allBoolQueryBuilder.build()._toQuery());

        //3.设置分页 设置请求参数中"from","size" 起始位置 页大小
        int from = (queryVo.getPageNo() - 1) * queryVo.getPageSize();
        searchRequestBuilder.from(from).size(queryVo.getPageSize());

        //4.设置关键字高亮 设置请求参数中"highlight" 包含：高亮字段、高亮标签
        if (StringUtils.isNotBlank(keyword)) {
            searchRequestBuilder.highlight(h -> h.fields("albumTitle", hf -> hf.preTags("<font style='color:red'>").postTags("</font>")));
        }

        //5.设置排序 设置请求参数中"sort" 包含：排序字段、排序方式
        if (StringUtils.isNotBlank(queryVo.getOrder())) {
            //5.1 先获取排序条件，根据冒号:进行字符串分割
            String[] split = queryVo.getOrder().split(":");
            if (split != null && split.length == 2) {
                String orderFile = "";
                //5.2 获取排序字段 跟 排序方式
                switch (split[0]) {
                    case "1":
                        orderFile = "hotScore";
                        break;
                    case "2":
                        orderFile = "playStatNum";
                        break;
                    case "3":
                        orderFile = "createTime";
                        break;
                }
                String finalOrderFile = orderFile;
                searchRequestBuilder.sort(s -> s.field(fs -> fs.field(finalOrderFile).order("asc".equals(split[1]) ? SortOrder.Asc : SortOrder.Desc)));
            }
        }

        //6.设置检索及响应ES字段 设置请求参数中"_source" 包含需要响应的业务字段
        searchRequestBuilder.source(s -> s.filter(f -> f.excludes("attributeValueIndexList", "category3Id", "category2Id", "category1Id")));

        return searchRequestBuilder.build();
    }
    /**
     * 解析ES检索结果，封装自定义结果
     *
     * @param response ·
     * @param queryVo
     * @return
     */
    @Override
    public AlbumSearchResponseVo parseResult(SearchResponse<AlbumInfoIndex> response, AlbumIndexQuery queryVo) {
        AlbumSearchResponseVo vo = new AlbumSearchResponseVo();
        //1.封装分页信息
        long total = response.hits().total().value();
        Integer pageSize = queryVo.getPageSize();
        long totalPage = total%pageSize==0?total/pageSize:total/pageSize+1;
        vo.setTotal(total);
        vo.setPageSize(pageSize);
        vo.setTotalPages(totalPage);
        vo.setPageNo(queryVo.getPageNo());
        //2.封装业务数据
        List<Hit<AlbumInfoIndex>> hits = response.hits().hits();
        if(CollectionUtil.isNotEmpty(hits)){
            List<AlbumInfoIndexVo> albumInfoIndexVoList = hits.stream().map(hit -> {
                //获取专辑对象
                AlbumInfoIndex albumInfoIndex = hit.source();
                //处理高亮
                Map<String, List<String>> highlightMap = hit.highlight();
                if (CollectionUtil.isNotEmpty(highlightMap)) {
                    if (highlightMap.containsKey("albumTitle")) {
                        //获取高亮关键字
                        String albumTitle = highlightMap.get("albumTitle").get(0);
                        albumInfoIndex.setAlbumTitle(albumTitle);
                    }
                }
                return BeanUtil.copyProperties(albumInfoIndex, AlbumInfoIndexVo.class);
            }).collect(Collectors.toList());
            vo.setList(albumInfoIndexVoList);

        }
        return vo;
    }
    /**
     * 查询首页每个三级分类下热门专辑列表
     *
     * @param category1Id
     * @return
     */
    @Override
    public List<Map<String, Object>> getCategory3Top6Hot(Long category1Id) {
        try {
            //1.远程调用专辑服务-得到入参中一级分类下包含前7个三级分类列表
            List<BaseCategory3> category3List = albumInfoFeignClient.getTop7BaseCategory3(category1Id).getData();
            Assert.notNull(category3List,"三级分类为空");
            //1.1 获取集合中七个分类 获取7个三级分类ID
            List<Long> category3IdList = category3List.stream().map(BaseCategory3::getId).collect(Collectors.toList());
            //1.2 将三级分类集合转为Map map中key：三级分类ID  map中value:三级分类对象
            Map<Long, BaseCategory3> category3Map = category3List.stream().collect(Collectors.toMap(BaseCategory3::getId, baseCategory3 -> baseCategory3));
            //1.3 将三级分类ID转为多关键字精确查询List条件对象FieldValue
            List<FieldValue> fieldValueList = category3IdList.stream().map(c3Id -> FieldValue.of(c3Id)).collect(Collectors.toList());
            //2.构建检索DSL语句
            //2.1 创建检索请求构建器对象，指定检索索引库名称
            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
            searchRequestBuilder.index(INDEX_NAME);
            //2.2 设置查询条件-根据七个三级分类ID-多关键字精确查询
            searchRequestBuilder.query(q -> q.terms(tq -> tq.field("category3Id").terms(t -> t.value(fieldValueList))));
            //2.3 设置业务数据返回为0
            searchRequestBuilder.size(0);
            //2.4 设置三级分类聚合，子聚合（获取热度前6专辑）
            searchRequestBuilder.aggregations("category3Agg",a->a.terms(t->t.field("category3Id"))
                    .aggregations("top6",a1->a1.topHits(t->t.size(6)
                            .sort(s->s.field(f->f.field("hotScore").order(SortOrder.Asc))))));
            //3.执行检索
            SearchRequest searchRequest = searchRequestBuilder.build();
            System.err.println("本次检索聚合DSL：");
            System.err.println(searchRequest.toString());
            SearchResponse<AlbumInfoIndex> searchResponse = elasticsearchClient.search(searchRequest, AlbumInfoIndex.class);
            //4.解析ES聚合结果
            //4.1 获取ES响应聚合结果中三级分类聚合对象
            Aggregate category3Agg = searchResponse.aggregations().get("category3Agg");
            LongTermsAggregate category3IdLterms = category3Agg.lterms();
            //4.2 遍历三级分类集合桶（Bucket） 每遍历一次产生“当前分类热门专辑Map” 将Map收集List
            List<Map<String, Object>> listResult = category3IdLterms.buckets().array().stream().map(category3Bucket -> {
                Map<String, Object> map = new HashMap<>();
                //4.2.1 遍历当前三级分类ID内部，获取三级分类ID
                long category3Id = category3Bucket.key();
                //4.2.2 获取热门前6子聚合对象 得到 热门专辑列表
                List<Hit<JsonData>> top6 = category3Bucket.aggregations().get("top6").topHits().hits().hits();
                if (CollectionUtil.isNotEmpty(top6)) {
                    List<AlbumInfoIndex> hotAlbumList = top6.stream().map(hit -> {
                        //将聚合到业务数据Hit类型转为 AlbumInfoIndex类型
                        String sourceJsonStr = hit.source().toString();
                        return JSON.parseObject(sourceJsonStr, AlbumInfoIndex.class);
                    }).collect(Collectors.toList());
                    map.put("list", hotAlbumList);
                }
                map.put("baseCategory3", category3Map.get(category3Id));
                return map;
            }).collect(Collectors.toList());
            return listResult;
        } catch (Exception e) {
            log.error("[专辑服务]热门专辑异常：{}", e);
            throw new RuntimeException(e);
        }
    }

    /**
     * 新增提词库索引文档
     *
     * @param albumInfoIndex
     */
    @Override
    public void saveSuggestDoc(AlbumInfoIndex albumInfoIndex) {
        //1.处理专辑标题
        String albumTitle = albumInfoIndex.getAlbumTitle();
        SuggestIndex suggestIndexAlbumTile = new SuggestIndex();
        suggestIndexAlbumTile.setId(IdUtil.fastSimpleUUID());
        suggestIndexAlbumTile.setTitle(albumTitle);
        suggestIndexAlbumTile.setKeyword(new Completion(new String[]{albumTitle}));
        suggestIndexAlbumTile.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(albumTitle,"")}));
        suggestIndexAlbumTile.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(albumTitle, "")}));

        //2.处理作者名称
        String announcerName = albumInfoIndex.getAnnouncerName();
        SuggestIndex suggestIndexAnnouncerName = new SuggestIndex();
        suggestIndexAnnouncerName.setId(IdUtil.fastSimpleUUID());
        suggestIndexAnnouncerName.setTitle(announcerName);
        suggestIndexAnnouncerName.setKeyword(new Completion(new String[]{announcerName}));
        suggestIndexAnnouncerName.setKeywordPinyin(new Completion(new String[]{PinyinUtil.getPinyin(announcerName, "")}));
        suggestIndexAnnouncerName.setKeywordSequence(new Completion(new String[]{PinyinUtil.getFirstLetter(announcerName, "")}));

        suggestIndexRepository.saveAll(Arrays.asList(suggestIndexAlbumTile, suggestIndexAnnouncerName));

    }
    private static final String SUGGEST_INDEX = "suggestinfo";
    /**
     * 关键字自动补全
     *
     * @param keyword
     * @return
     */
    @Override
    public List<String> completeSuggest(String keyword) {
        try {
            //1.构建检索请求对象，封装检索索引库名称
            SearchRequest.Builder searchRequestBuilder = new SearchRequest.Builder();
            searchRequestBuilder.index(SUGGEST_INDEX);
            //1.1设置建议词请求体参数
            searchRequestBuilder.suggest(s->s.suggesters("suggestKeyword",f->f.prefix(keyword).completion(c->c.field("keyword").size(10).skipDuplicates(true)))
                    .suggesters("suggestKeywordPinyin",f->f.prefix(keyword).completion(c->c.field("keywordPinyin").size(10).skipDuplicates(true).fuzzy(fu->fu.fuzziness("auto"))))
                    .suggesters("suggestKeywordSquenece",f->f.prefix(keyword).completion(c->c.field("keywordSequence").size(10).skipDuplicates(true).fuzzy(fu->fu.fuzziness("auto")))));
            //2.执行检索
            SearchRequest searchRequest = searchRequestBuilder.build();
            System.err.println("提词DSL：");
            System.err.println(searchRequest.toString());
            SearchResponse<SuggestIndex> searchResponse = elasticsearchClient.search(searchRequest, SuggestIndex.class);
            //3.解析ES响应建议结果
            //3.1 准备单例集合（能去重重复建议词）
            Set<String> titleSet = new HashSet<>();
            titleSet.addAll(this.parseSuggestResult(searchResponse,"suggestKeyword"));
            titleSet.addAll(this.parseSuggestResult(searchResponse,"suggestKeywordPinyin"));
            titleSet.addAll(this.parseSuggestResult(searchResponse,"suggestKeywordSquenece"));
            //3.2 判断命中提示词数量小于10
            if(titleSet.size()<10){
                //通过普通检索 检索提词索引库 利用匹配查询返回结果
                SearchResponse<SuggestIndex> response =
                        elasticsearchClient.search(s -> s.index(SUGGEST_INDEX).query(q -> q.match(m -> m.field("title").query(keyword))), SuggestIndex.class);
                List<Hit<SuggestIndex>> hits = response.hits().hits();
                if(CollectionUtil.isNotEmpty(hits)){
                    for (Hit<SuggestIndex> hit : hits) {
                        SuggestIndex suggestIndex = hit.source();
                        titleSet.add(suggestIndex.getTitle());
                        //如果添加后大于等于10
                        if(titleSet.size()>10){
                            break;
                        }
                    }
                }
            }
            //返回列表前10个数据
            return new ArrayList<>(titleSet).subList(0,10);
        } catch (IOException e) {
            log.error("关键字自动补全失败");
            throw new RuntimeException(e);
        }
    }
    /**
     * 解析提词结果
     *
     * @param searchResponse
     * @param suggestName
     * @return
     */
    @Override
    public Collection<String> parseSuggestResult(SearchResponse<SuggestIndex> searchResponse, String suggestName) {
        List<String> list = new ArrayList<>();
        //1.获取提词结果Map
        Map<String, List<Suggestion<SuggestIndex>>> suggestMap = searchResponse.suggest();
        List<Suggestion<SuggestIndex>> suggestionList = suggestMap.get(suggestName);
        //2.获取提词结果命中选项options
        for (Suggestion<SuggestIndex> suggestIndexSuggestion : suggestionList) {
            for (CompletionSuggestOption<SuggestIndex> option : suggestIndexSuggestion.completion().options()) {
                //获取提词选项中_source
                SuggestIndex suggestIndex = option.source();
                list.add(suggestIndex.getTitle());
            }
        }
        return list;
    }
}
