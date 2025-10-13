package com.atguigu.tingshu.album;

import com.atguigu.tingshu.album.impl.AlbumInfoDegradeFeignClient;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.BaseCategory3;
import com.atguigu.tingshu.model.album.BaseCategoryView;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

/**
 * <p>
 * 专辑模块远程调用Feign接口
 * </p>
 *
 * @author atguigu
 */
@FeignClient(value = "service-album", path = "/api/album", fallback = AlbumInfoDegradeFeignClient.class)
public interface AlbumInfoFeignClient {
    /*
     * 底层feign发起请求路径:GET https://service-album/api/album/albumInfo/getAlbumInfo/{id}
     * 根据id查询专辑信息
     */
    @GetMapping("/albumInfo/getAlbumInfo/{id}")
    public Result<AlbumInfo> getAlbumInfo(@PathVariable Long id) ;

    /**
     * 根据三级分ID（专辑视图主键）查询专辑信息
     *
     * @param category3Id
     * @return
     */
    @GetMapping("/category/getCategoryView/{category3Id}")
    public Result<BaseCategoryView> getCategoryViewBy3Id(@PathVariable Long category3Id);

    /**
     * 根据一级分类ID查询该一级分类下包含(前七个)三级分类列表
     *
     * @param category1Id
     * @return
     */
    @GetMapping("/category/findTopBaseCategory3/{category1Id}")
    public Result<List<BaseCategory3>> getTop7BaseCategory3(@PathVariable Long category1Id);
}
