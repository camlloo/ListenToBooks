package com.atguigu.tingshu.album.service.impl;

import com.atguigu.tingshu.album.config.VodConstantProperties;
import com.atguigu.tingshu.album.mapper.TrackInfoMapper;
import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.util.UploadFileUtil;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.qcloud.vod.VodUploadClient;
import com.qcloud.vod.model.VodUploadRequest;
import com.qcloud.vod.model.VodUploadResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

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
    /**
     * 上传音视频文件到腾讯云点播服务
     *
     * @param file
     * @return
     */
    @Override
    public Map<String, String> uploadTrack(MultipartFile file) {
        try {
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
                Map<String, String> map = new HashMap<>();
                map.put("mediaFileId", response.getFileId());
                map.put("mediaUrl", response.getMediaUrl());
                return map;
            }
            return null;
        } catch (Exception e) {
            log.error("[云点播上传文件失败：]{}", e);
            throw new RuntimeException(e);
        }
    }
}
