package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface AlbumInfoMapper extends BaseMapper<AlbumInfo> {
    /**
     * 根据条件查询专辑列表
     * @param pageInfo MP框架发现参数值中包含分页对象，自动完成分页查询（自动查询总记录数，总页数，当前页数据自动封装到page对象中）
     * @param albumInfoQuery 查询条件
     * @return
     */
    Page<AlbumListVo> getfindUserAlbumPage(Page<AlbumListVo> pageInfo, @Param("vo") AlbumInfoQuery albumInfoQuery);
}
