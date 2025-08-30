package com.atguigu.tingshu.album.api;

import com.alibaba.fastjson.JSONObject;
import com.atguigu.tingshu.album.service.BaseCategoryService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.BaseAttribute;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;


@Tag(name = "分类管理")
@RestController
@RequestMapping(value = "/api/album")
@SuppressWarnings({"all"})
public class BaseCategoryApiController {

    @Autowired
    private BaseCategoryService baseCategoryService;

    /*
    查询所有分类（1，2，3级分类）
    1.从视图中获取所以三级分类列表
    /api/album/category/getBaseCategoryList
     */
    @Operation(summary = "查询所以分类")
    @GetMapping("/category/getBaseCategoryList")
    public Result<List<JSONObject>> getBaseCategoryList() {
        List<JSONObject> lsit = baseCategoryService.getBaseCategoryList();
        return Result.ok(lsit);
    }
   /**
    * /api/album/category/findAttribute/{category1Id}
    * 根据一级分类Id获取分类属性以及属性值（标签名，标签值）列表
    */
   @Operation(summary = "根据一级分类Id获取分类属性以及属性值")
   @GetMapping("/category/findAttribute/{category1Id}")
   public Result<List<BaseAttribute>> getAttributeByCategory1Id(@PathVariable("category1Id") Long category1Id){
      List<BaseAttribute>  list=  baseCategoryService.getAttributeByCategory1Id(category1Id);
   return Result.ok(list);
   }
}

