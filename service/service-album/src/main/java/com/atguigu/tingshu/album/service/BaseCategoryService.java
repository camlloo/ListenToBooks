package com.atguigu.tingshu.album.service;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.model.album.BaseAttribute;
import com.atguigu.tingshu.model.album.BaseCategory1;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

public interface BaseCategoryService extends IService<BaseCategory1> {


    List<JSONObject> getBaseCategoryList();

    List<BaseAttribute> getAttributeByCategory1Id(Long category1Id);

    BaseCategoryView getCategoryViewBy3Id(Long category3Id);
    /**
     * 根据一级分类ID查询该一级分类下包含所有三级分类列表
     *
     * @param category1Id
     * @return
     */
    List<BaseCategory3> getTop7BaseCategory3(Long category1Id);
    /**
     * 根据一级分类ID查询当前分类包含所有二级分类以及二级分类下三级分类列表
     *
     * @param category1Id
     * @return
     */
    JSONObject getCategoryListByCategory1Id(Long category1Id);
}
