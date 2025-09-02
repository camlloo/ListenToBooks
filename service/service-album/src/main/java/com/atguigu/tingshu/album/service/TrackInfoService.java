package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface TrackInfoService extends IService<TrackInfo> {

    Map<String, String> uploadTrack(MultipartFile file);

    void saveTrackInfo(Long userId, TrackInfoVo trackInfoVo);

    void saveTrackStat(Long id, String trackStatCollect);

    Page<TrackInfoVo> getUserTrackByPage(Page<TrackInfoVo> pageInfo, TrackInfoQuery trackInfoQuery);

}
