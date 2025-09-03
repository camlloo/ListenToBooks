package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.AlbumInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.mapper.TrackStatMapper;
import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.album.service.VodService;
import com.atguigu.tingshu.common.constant.SystemConstant;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.model.album.TrackStat;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.atguigu.tingshu.vo.album.TrackMediaInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Service
@SuppressWarnings({"all"})
public class TrackInfoServiceImpl extends ServiceImpl<TrackInfoMapper, TrackInfo> implements TrackInfoService {

	@Autowired
	private TrackInfoMapper trackInfoMapper;
    @Autowired
    private VodConstantProperties props;
    @Autowired
    private AlbumInfoMapper albumInfoMapper;
    @Autowired
    private VodService vodService;
    @Autowired
    private TrackStatMapper trackStatMapper;
    /**
     * 上传音视频文件到腾讯云点播服务
     *
     * @param file
     * @return
     */
    @Override
    public Map<String, String> uploadTrack(MultipartFile file) {
        try {
            //因节省费用所有将上传的音频写死，每个声音共用一个音频
            //0.将用户提交文件保存到临时目录下
            String tempFilePath = UploadFileUtil.uploadTempPath(props.getTempPath(),file);
            //1.初始化一个上传客户端对象
            VodUploadClient client = new VodUploadClient(props.getSecretId(), props.getSecretKey());
            //2.构造上传请求对象
            VodUploadRequest request = new VodUploadRequest();
            request.setMediaFilePath(tempFilePath);
            VodUploadResponse response = client.upload(props.getRegion(), request);
           if(response!=null) {
                //获取到文件地址、文件唯一标识保存在map中，构造返回参数
                String fileId = response.getFileId();
                String mediaUrl = response.getMediaUrl();
                Map<String, String> map = new HashMap<>();
                map.put("mediaFileId",fileId);
                map.put("mediaUrl", mediaUrl);
                return map;
            }
            return null;
        } catch (Exception e) {
            log.error("[云点播上传文件失败：]{}", e);
            throw new RuntimeException(e);
        }
    }
    /**
     * 新增声音
     * 1.新增声音记录
     * 2.更新专辑（专辑包含声音数量）
     * 3.初始化声音统计记录
     *
     * @param userId      用户ID
     * @param trackInfoVo 声音相关信息
     */
    @Transactional(rollbackFor = Exception.class)
    @Override
    public void saveTrackInfo(Long userId, TrackInfoVo trackInfoVo) {
        //1.新增声音记录
        //1.1 将声音VO对象拷贝到声音PO对象
        TrackInfo trackInfo = BeanUtil.copyProperties(trackInfoVo, TrackInfo.class);
        //1.2 设置用户ID
        trackInfo.setUserId(userId);
        //1.3 设置状态 默认：通过  实际上应该有审核：机审或者人工审核
        trackInfo.setStatus(SystemConstant.TRACK_STATUS_PASS);
        //1.4 设置声音来源
        trackInfo.setSource(SystemConstant.TRACK_SOURCE_USER);
        //1.5 设置声音序号 根据专辑ID查询专辑得到该专辑下已有声音数量
        AlbumInfo albumInfo = albumInfoMapper.selectById(trackInfo.getAlbumId());
        trackInfo.setOrderNum(albumInfo.getIncludeTrackCount()+1);
        //1.6 设置音频文件相关信息（文件大小，时长，文件类型）-调用腾讯云点播平台接口获取
         TrackMediaInfoVo mediaInfoVo =  vodService.getTrackMediaInfo(trackInfo.getMediaFileId());
         log.info("调用腾讯云点播平台接口获取:{}",mediaInfoVo);
         if(mediaInfoVo!=null) {
             trackInfo.setMediaSize(mediaInfoVo.getSize());
             trackInfo.setMediaDuration(BigDecimal.valueOf(mediaInfoVo.getDuration()));
             trackInfo.setMediaType(mediaInfoVo.getType());
         }
        //1.7 保存声音
        trackInfoMapper.insert(trackInfo);
        //2.更新专辑（专辑包含声音数量）
        albumInfo.setIncludeTrackCount(albumInfo.getIncludeTrackCount()+1);
        albumInfoMapper.updateById(albumInfo);
        //3.初始化声音统计记录
        this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_PLAY);
        this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_COLLECT);
        this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_PRAISE);
        this.saveTrackStat(trackInfo.getId(),SystemConstant.TRACK_STAT_COMMENT);

    }
    /**
     * 保存声音统计信息
     *
     * @param id
     * @param trackStatCollect
     */
    @Override
    public void saveTrackStat(Long id, String trackStatCollect) {
        TrackStat trackStat = new TrackStat();
        trackStat.setTrackId(id);
        trackStat.setStatType(trackStatCollect);
        trackStat.setStatNum(0);
        trackStatMapper.insert(trackStat);
    }
    /**
     * 获取当前登录声音分页列表
     * @param pageInfo MP分页对象
     * @param trackInfoQuery 查询声音条件对象
     * @return
     */
    @Override
    public Page<TrackInfoVo> getUserTrackByPage(Page<TrackInfoVo> pageInfo, TrackInfoQuery trackInfoQuery) {
        return trackInfoMapper.getUserTrackByPage(pageInfo,trackInfoQuery);
    }

    @Override
    public TrackInfo getTrackInfo(Long id) {
        return trackInfoMapper.selectById(id);
    }
@Transactional(rollbackFor = Exception.class)
    @Override
    public void updateTrackInfo(Long id, TrackInfoVo trackInfoVo) {
        //1.先获取声音信息-得到更新前声音文件唯一标识
        TrackInfo trackInfo = trackInfoMapper.selectById(id);
        String mediaFileId = trackInfo.getMediaFileId();
        BeanUtil.copyProperties(trackInfoVo,trackInfo);
        //2.调用腾讯云点播获取声音媒体信息（时长，类型，文件大小） 更新媒体信息
        if (!mediaFileId.equals(trackInfo.getMediaFileId())) {
            TrackMediaInfoVo trackMediaInfo = vodService.getTrackMediaInfo(trackInfo.getMediaFileId());
            trackInfo.setMediaType(trackMediaInfo.getType());
            trackInfo.setMediaSize(trackMediaInfo.getSize());
            trackInfo.setMediaDuration(BigDecimal.valueOf(trackMediaInfo.getDuration()));
        }
        //3.更新声音信息表
        trackInfoMapper.updateById(trackInfo);
    }

}
