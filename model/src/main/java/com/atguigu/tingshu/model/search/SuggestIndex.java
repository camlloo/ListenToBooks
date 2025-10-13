package com.atguigu.tingshu.model.search;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.CompletionField;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.core.suggest.Completion;

@Data
@Document(indexName = "suggestinfo")
@JsonIgnoreProperties(ignoreUnknown = true)//目的：防止json字符串转成实体对象时因未识别字段报错
public class SuggestIndex {

    @Id
    private String id;//提示文档唯一标识
//原始内容：经典留声机
    @Field(type = FieldType.Text, analyzer = "standard")
    private String title;//存放专辑标题或者主播名称，采用标准分词器：将一个汉字拆为一个词，es内部拆分：经 典 留 声 机
/*
建议词字段一：ES内部字段拆分：经 典 留 声 机
 */
    @CompletionField(analyzer = "standard", searchAnalyzer = "standard", maxInputLength = 20)
    private Completion keyword;
/*
建议词字段二
ES内部字段拆分jingdianliushengji
 */
    @CompletionField(analyzer = "standard", searchAnalyzer = "standard", maxInputLength = 20)
    private Completion keywordPinyin;
/*
建议词字段三
ES内部字段拆分：jdlsj
 */
    @CompletionField(analyzer = "standard", searchAnalyzer = "standard", maxInputLength = 20)
    private Completion keywordSequence;

}
