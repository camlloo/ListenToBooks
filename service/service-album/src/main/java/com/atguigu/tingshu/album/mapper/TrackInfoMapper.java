package com.atguigu.tingshu.album.mapper;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

@Mapper
public interface TrackInfoMapper extends BaseMapper<TrackInfo> {

    /**
     * MP框架发现参数中包含分页对象，自动完成分页查询（自动查询总记录数，总页数，当前页数据自动封装到Page对象中）
     * @param pageInfo
     * @param trackInfoQuery
     * @return
     */
    Page<TrackInfoVo> getUserTrackByPage(Page<TrackInfoVo> pageInfo,@Param("vo")  TrackInfoQuery trackInfoQuery);
    /**
     * 批量修改声音序号
     * @param albumId
     * @param orderNum
     */
@Update("update track_info set order_num = order_num-1 where album_id =#{albumId} and order_num>#{orderNum} and is_deleted = 0")
    void updateTrackNum(@Param("albumId") Long albumId, @Param("orderNum") Integer orderNum);
}
