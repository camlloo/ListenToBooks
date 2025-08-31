package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.collection.CollectionUtil;
import com.atguigu.tingshu.album.mapper.AlbumAttributeValueMapper;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.AlbumStatMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.model.album.AlbumAttributeValue;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.AlbumStat;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumAttributeValueVo;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class AlbumInfoServiceImpl extends ServiceImpl<AlbumInfoMapper, AlbumInfo> implements AlbumInfoService {
	@Autowired
	private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private AlbumAttributeValueMapper albumAttributeValueMapper;
    @Autowired
    private AlbumStatMapper albumStatMapper;
    @Autowired
    private TrackInfoMapper trackInfoMapper;
    /**
     * 新增专辑
     * 1.向专辑信息表新增一条记录
     * 2.向专辑属性关系表新增若干条记录
     * 3.向专辑统计表中新增四条记录
     *
     * @param albumInfoVo 专辑相关信息
     * @param userId      用户ID
     */
    @Transactional(rollbackFor = Exception.class)      //Spring事务管理默认捕获RuntimeException才会进行事务回滚
    @Override
    public void saveAlbumInfo(AlbumInfoVo albumInfoVo, Long userId) {
        //1.向专辑信息表新增一条记录
        //1.1 将前端提交专辑VO对象转为专辑PO对象
        AlbumInfo albumInfo = BeanUtil.copyProperties(albumInfoVo, AlbumInfo.class);
        //1.2 部分属性赋值
        albumInfo.setUserId(userId);
        //付费类型为非免费的专辑 免费试听集数为5，试听秒数(不限制)
        if(!SystemConstant.ALBUM_PAY_TYPE_FREE.equals(albumInfo.getPayType())){
            albumInfo.setTracksForFree(5);
        }
        //目前没有平台审核端暂时写为通过
        albumInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);
        //1.3 保存专辑 得到专辑ID
        albumInfoMapper.insert(albumInfo);
        Long albumId = albumInfo.getId();
        //2.向专辑属性关系表新增若干条记录
        List<AlbumAttributeValueVo> albumAttributeValueVoList = albumInfoVo.getAlbumAttributeValueVoList();
        //2.1.遍历VO集合 将VO转为PO
        if(CollectionUtil.isNotEmpty(albumAttributeValueVoList)){
            albumAttributeValueVoList.forEach(albumAttributeValueVo -> {
                AlbumAttributeValue albumAttributeValue = BeanUtil.copyProperties(albumAttributeValueVo, AlbumAttributeValue.class);
                //2.2.关联专辑ID
                albumAttributeValue.setAlbumId(albumId);
                //2.3.新增专辑属性
                albumAttributeValueMapper.insert(albumAttributeValue);
            });
        }
        //3.向专辑统计表中新增四条记录(播放数，订阅数，购买数，评论数)
        this.saveAlbumStat(albumId,SystemConstant.TRACK_STAT_PLAY);
        this.saveAlbumStat(albumId,SystemConstant.TRACK_STAT_COLLECT);
        this.saveAlbumStat(albumId,SystemConstant.TRACK_STAT_PRAISE);
        this.saveAlbumStat(albumId,SystemConstant.TRACK_STAT_COMMENT);
    }
    /**
     * 初始化保存专辑统计信息
     * @param albumId 专辑ID
     * @param statType 统计类型
     */
    @Override
    public void saveAlbumStat(Long albumId, String statType) {
        AlbumStat albumStat = new AlbumStat();
        albumStat.setAlbumId(albumId);
        albumStat.setStatType(statType);
        albumStat.setStatNum(0);
        albumStatMapper.insert(albumStat);
    }

    /**
     * 分页查询当前用户专辑列表
     * @param pageInfo MP的分页对象
     * @param albumInfoQuery 查询条件
     * @return
     */
    @Override
    public Page<AlbumListVo> getfindUserAlbumPage(Page<AlbumListVo> pageInfo, AlbumInfoQuery albumInfoQuery) {
        return albumInfoMapper.getfindUserAlbumPage(pageInfo,albumInfoQuery);
    }

    /**
     * 根据专辑ID删除专辑
     * 1.根据主键ID删除专辑
     * 2.根据专辑ID删除统计列表
     * 3.根据专辑ID删除专辑属性列表
     * 4.根据专辑ID删除声音列表
     * @param id
     * @return
     */
    @Override
    @Transactional(rollbackFor = Exception.class)
    public void removeAlbumInfo(Long id) {
        //1.根据主键ID删除专辑
        albumInfoMapper.deleteById(id);
        //2.根据专辑ID删除统计列表
        LambdaQueryWrapper<AlbumStat> lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AlbumStat::getAlbumId,id);
        albumStatMapper.delete(lambdaQueryWrapper);
        // 3.根据专辑ID删除专辑属性列表
        LambdaQueryWrapper<AlbumAttributeValue> attributeValueLambdaQueryWrapper = new LambdaQueryWrapper<>();
        attributeValueLambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId,id);
        albumAttributeValueMapper.delete(attributeValueLambdaQueryWrapper);
        //4.根据专辑ID删除声音列表
        LambdaQueryWrapper<TrackInfo>  trackInfoLambdaQueryWrapper = new LambdaQueryWrapper<>();
        trackInfoLambdaQueryWrapper.eq(TrackInfo::getAlbumId,id);
        trackInfoMapper.delete(trackInfoLambdaQueryWrapper);
    }
    /**
     * 根据专辑ID查询专辑信息包含专辑属性列表
     *
     * @param id
     * @return
     */
    @Override
    public AlbumInfo getAlbumInfo(Long id) {
        //1.根据主键查询专辑信息
        AlbumInfo albumInfo = albumInfoMapper.selectById(id);
        //2.根据专辑ID查询专辑属性列表
        LambdaQueryWrapper<AlbumAttributeValue>  lambdaQueryWrapper = new LambdaQueryWrapper<>();
        lambdaQueryWrapper.eq(AlbumAttributeValue::getAlbumId,id);
        List<AlbumAttributeValue> albumAttributeValues = albumAttributeValueMapper.selectList(lambdaQueryWrapper);
        albumInfo.setAlbumAttributeValueVoList(albumAttributeValues);
        return albumInfo;
    }

}
