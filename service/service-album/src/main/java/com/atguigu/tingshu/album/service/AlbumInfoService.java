package com.atguigu.tingshu.album.service;

import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;

public interface AlbumInfoService extends IService<AlbumInfo> {


    void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId);
    /**
     * 初始化保存专辑统计信息
     * @param albumId 专辑ID
     * @param statType 统计类型
     */

    void saveAlbumStat(Long albumId, String statType);

    Page<AlbumListVo> getfindUserAlbumPage(Page<AlbumListVo> pageInfo, AlbumInfoQuery albumInfoQuery);

    void removeAlbumInfo(Long id);

    AlbumInfo getAlbumInfo(Long id);
}
