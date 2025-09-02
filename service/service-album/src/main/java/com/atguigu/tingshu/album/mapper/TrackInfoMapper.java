package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {

    /**
     * MP框架发现参数中包含分页对象，自动完成分页查询（自动查询总记录数，总页数，当前页数据自动封装到Page对象中）
     * @param pageInfo
     * @param trackInfoQuery
     * @return
     */
    Page<TrackInfoVo> getUserTrackByPage(Page<TrackInfoVo> pageInfo,@Param("vo")  TrackInfoQuery trackInfoQuery);
}
