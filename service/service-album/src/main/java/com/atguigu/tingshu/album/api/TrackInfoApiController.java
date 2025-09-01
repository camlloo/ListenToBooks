package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;
@Slf4j
@Tag(name = "声音管理")
@RestController
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class TrackInfoApiController {

	@Autowired
	private TrackInfoService trackInfoService;

    @Operation(summary = "上传音视频文件到腾讯云点播服务")
    @PostMapping("/trackInfo/uploadTrack")
    public Result<Map<String,String>> uploadTrack(MultipartFile file){
       Map<String,String> fileMap = trackInfoService.uploadTrack(file);
    return Result.ok(fileMap);
    }
    @Operation(summary = "新增声音")
    @PostMapping("/trackInfo/saveTrackInfo")
    public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo){
        log.info("新增声音:{}",trackInfoVo);
        //1.获取用户id
        Long userId = AuthContextHolder.getUserId();
        //2.调用业务层保存声音
        trackInfoService.saveTrackInfo(userId,trackInfoVo);
        return Result.ok();
    }
}

