package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.TrackInfoService;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.TrackInfo;
import com.atguigu.tingshu.query.album.TrackInfoQuery;
import com.atguigu.tingshu.vo.album.TrackInfoVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
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
    public Result<Map<String, String>> uploadTrack(MultipartFile file) {
        Map<String, String> fileMap = trackInfoService.uploadTrack(file);
        return Result.ok(fileMap);
    }

    @Operation(summary = "新增声音")
    @PostMapping("/trackInfo/saveTrackInfo")
    public Result saveTrackInfo(@RequestBody @Validated TrackInfoVo trackInfoVo) {
        log.info("新增声音:{}", trackInfoVo);
        //1.获取用户id
        Long userId = AuthContextHolder.getUserId();
        //2.调用业务层保存声音
        trackInfoService.saveTrackInfo(userId, trackInfoVo);
        return Result.ok();
    }

    @Operation(summary = "获取当前登录声音分页列表")
    @PostMapping("/trackInfo/findUserTrackPage/{page}/{limit}")
    public Result<Page<TrackInfoVo>> getUserTrackByPage(@PathVariable int page, @PathVariable int limit, @RequestBody TrackInfoQuery trackInfoQuery) {
        //获取用户Id封装到分页查询条件对象中
        Long userId = AuthContextHolder.getUserId();
        trackInfoQuery.setUserId(userId);
        //调用接口
        Page<TrackInfoVo> pageInfo = new Page<>(page, limit);
        pageInfo = trackInfoService.getUserTrackByPage(pageInfo, trackInfoQuery);
        return Result.ok(pageInfo);
    }

    @Operation(summary = "根据声音ID查询声音信息")
    @GetMapping("/trackInfo/getTrackInfo/{id}")
    public Result<TrackInfo> getTrackInfo(@PathVariable Long id) {
        TrackInfo trackInfo = trackInfoService.getTrackInfo(id);
        return Result.ok(trackInfo);
    }
    @Operation(summary = "修改声音信息")
    @PutMapping("/trackInfo/updateTrackInfo/{id}")
    public Result updateTrackInfo(@PathVariable Long id, @RequestBody @Validated TrackInfoVo trackInfoVo) {
        trackInfoService.updateTrackInfo(id,trackInfoVo);
        return Result.ok();
    }

}

