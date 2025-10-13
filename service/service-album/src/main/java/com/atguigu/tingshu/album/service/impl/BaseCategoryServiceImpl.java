package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.collection.CollectionUtil;
import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.mapper.*;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.model.album.*;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@SuppressWarnings({"all"})
public class BaseCategoryServiceImpl extends ServiceImpl<BaseCategory1Mapper, BaseCategory1> implements BaseCategoryService {

	@Autowired
	private BaseCategory1Mapper baseCategory1Mapper;

	@Autowired
	private BaseCategory2Mapper baseCategory2Mapper;

	@Autowired
	private BaseCategory3Mapper baseCategory3Mapper;
    @Autowired
    private BaseCategoryViewMapper baseCategoryViewMapper;
    @Autowired
    private BaseAttributeMapper baseAttributeMapper;

/*
查询所以分类
从视图中获取所有三级分类列表
 */
    @Override
    public List<JSONObject> getBaseCategoryList() {
        List<JSONObject> listResult = new ArrayList<>();
        //1.查询分类视图获取所有视图表中的记录共401条分类数据
        List<BaseCategoryView> allCategoryView = baseCategoryViewMapper.selectList(null);
        //2.处理一级分类，封装所有一级分类JSONObject，将一级分类对象加入到List结果
        if(CollectionUtil.isNotEmpty(allCategoryView)){
            //2.1 采用JDK8中Stream流中分组API对集合中元素按照一级分类ID分组，得到Map<key, val> key:分组字段category1Id  val：一级分类对应List列表
            Map<Long, List<BaseCategoryView>> cateogry1MapList = allCategoryView.stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory1Id));
            //2.2 遍历Map集合中元素，每遍历Map处理当前一级分类数据
            for (Map.Entry<Long,List<BaseCategoryView>> entry1 : cateogry1MapList.entrySet()) {
                //2.2.1 获取分组字段key得到一级分类ID
                Long category1Id = entry1.getKey();
                //2.2.2 获取分组后值第一个元素得到一级分类名称
                String category1Name = entry1.getValue().get(0).getCategory1Name();
                //2.2.2 构建一级分类JSON对象
                JSONObject category1 = new JSONObject();
                category1.put("categoryId",category1Id);
                category1.put("categoryName",category1Name);
                //3.在一级分类集合内部，封装当前分类下二级分类列表，封装二级分类JSONObject，将二级分类集合加入到一级分类“categoryChild”属性中
                //3.1 对一级分类列表采用Stream流分组，分组字段：二级分类ID
                Map<Long, List<BaseCategoryView>> cateogry2MapList = entry1.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory2Id));
                //3.2 遍历当前一级分类中包含二级分类Map
                List<JSONObject> category2List = new ArrayList<>();
                for (Map.Entry<Long, List<BaseCategoryView>> entry2 : cateogry2MapList.entrySet()) {
                    // 3.2.1 获取分组后key 就是二级分类ID
                    Long category2Id = entry2.getKey();
                    // 3.2.2 获取分组后val 就是二级分类列表 从第一元素中获取二级分类名称
                    String category2Name = entry2.getValue().get(0).getCategory2Name();
                    // 3.2.3 构建二级分类对象
                    JSONObject category2 = new JSONObject();
                    category2.put("categoryId",category2Id);
                    category2.put("categoryName",category2Name);
                    //4.二分类分类集合中，处理三级分类，封装三级分类JSONObject，将三级分类集合加入到二级分类“categoryChild”属性中
                    Map<Long, List<BaseCategoryView>> category3MapList = entry2.getValue().stream().collect(Collectors.groupingBy(BaseCategoryView::getCategory3Id));
                    List<JSONObject>category3List = new ArrayList<>();
                    for (Map.Entry<Long, List<BaseCategoryView>> entry3 : category3MapList.entrySet()) {
                        Long category3Id= entry3.getKey();
                        String category3Name = entry3.getValue().get(0).getCategory3Name();
                        JSONObject category3 = new JSONObject();
                        category3.put("categoryId",category3Id);
                        category3.put("categoryName",category3Name);
                        category3List.add(category3);
                    }
                    category2.put("categoryChild",category3List);
                    category2List.add(category2);
                }
                category1.put("categoryChild",category2List);
                listResult.add(category1);
            }
        }
        return listResult;
    }

    @Override
    public List<BaseAttribute> getAttributeByCategory1Id(Long category1Id) {
        return baseAttributeMapper.getAttributeByCategory1Id(category1Id);
    }

    @Override
    public BaseCategoryView getCategoryViewBy3Id(Long category3Id) {
        return baseCategoryViewMapper.selectById(category3Id);
    }

    @Override
    public List<BaseCategory3> getTop7BaseCategory3(Long category1Id) {
        //1.根据一级分类ID查询二级分类ID集合
        LambdaQueryWrapper<BaseCategory2> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(BaseCategory2::getCategory1Id,category1Id);
        queryWrapper.select(BaseCategory2::getId);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(queryWrapper);
        //2.根据二级分类ID查询三级分类列表
        if(CollectionUtil.isNotEmpty(baseCategory2List)){
            List<Long> category2IdList = baseCategory2List.stream().map(BaseCategory2::getId).collect(Collectors.toList());
            LambdaQueryWrapper<BaseCategory3> baseCategory3LambdaQueryWrapper = new LambdaQueryWrapper<>();
            baseCategory3LambdaQueryWrapper.in(BaseCategory3::getCategory2Id,category2IdList);
            baseCategory3LambdaQueryWrapper.orderByAsc(BaseCategory3::getId);
            baseCategory3LambdaQueryWrapper.last("limit 7");
            return baseCategory3Mapper.selectList(baseCategory3LambdaQueryWrapper);
        }
        return null;
    }

    @Override
    public JSONObject getCategoryListByCategory1Id(Long category1Id) {
        //1.根据一级分类ID查询到一级分类信息
        BaseCategory1 baseCategory1 = baseCategory1Mapper.selectById(category1Id);
        //1.1 构建一级分类对象
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("categoryId", baseCategory1.getId());
        jsonObject.put("categoryName", baseCategory1.getName());
        //2.根据一级分类ID查询二级分类列表
        LambdaQueryWrapper<BaseCategory2> baseCategory2LambdaQueryWrapper = new LambdaQueryWrapper<>();
        baseCategory2LambdaQueryWrapper.eq(BaseCategory2::getCategory1Id, category1Id);
        List<BaseCategory2> baseCategory2List = baseCategory2Mapper.selectList(baseCategory2LambdaQueryWrapper);
        if (CollectionUtil.isNotEmpty(baseCategory2List)) {
            List<JSONObject> category2List = baseCategory2List.stream().map(baseCategory2 -> {
                JSONObject jsonObject2 = new JSONObject();
                jsonObject2.put("categoryId", baseCategory2.getId());
                jsonObject2.put("categoryName", baseCategory2.getName());

                //3.根据二级分类ID查询三级分类列表
                LambdaQueryWrapper<BaseCategory3> baseCategory3LambdaQueryWrapper = new LambdaQueryWrapper<>();
                baseCategory3LambdaQueryWrapper.eq(BaseCategory3::getCategory2Id, baseCategory2.getId());
                List<BaseCategory3> baseCategory3List = baseCategory3Mapper.selectList(baseCategory3LambdaQueryWrapper);
                if (CollectionUtil.isNotEmpty(baseCategory3List)) {
                    List<JSONObject> category3List = baseCategory3List.stream().map(baseCategory3 -> {
                        JSONObject jsonObject3 = new JSONObject();
                        jsonObject3.put("categoryId", baseCategory3.getId());
                        jsonObject3.put("categoryName", baseCategory3.getName());
                        return jsonObject3;
                    }).collect(Collectors.toList());
                    jsonObject2.put("categoryChild", category3List);
                }
                return jsonObject2;
            }).collect(Collectors.toList());
            jsonObject.put("categoryChild", category2List);
        }
        return jsonObject;
    }
}
