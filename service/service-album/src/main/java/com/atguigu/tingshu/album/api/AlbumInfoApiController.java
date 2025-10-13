package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.AlbumInfoService;
import com.atguigu.tingshu.common.login.KingLogin;
import com.atguigu.tingshu.common.result.Result;
import com.atguigu.tingshu.common.util.AuthContextHolder;
import com.atguigu.tingshu.model.album.AlbumInfo;
import com.atguigu.tingshu.query.album.AlbumInfoQuery;
import com.atguigu.tingshu.vo.album.AlbumInfoVo;
import com.atguigu.tingshu.vo.album.AlbumListVo;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "专辑管理")
@RestController
@Slf4j
@RequestMapping("api/album")
@SuppressWarnings({"all"})
public class AlbumInfoApiController {

    @Autowired
    private AlbumInfoService albumInfoService;


    @KingLogin
    @Operation(summary = "新增专辑")
    @PostMapping("/albumInfo/saveAlbumInfo")
    public Result saveAlbumInfo(@RequestBody @Validated AlbumInfoVo albumInfoVo) {
        Long userId = AuthContextHolder.getUserId();
        albumInfoService.saveAlbumInfo(albumInfoVo, userId);
        return Result.ok();
    }


    @KingLogin
    @Operation(summary = "分页查询当前用户的专辑列表")
    @PostMapping("/albumInfo/findUserAlbumPage/{page}/{limit}")
    public Result<Page<AlbumListVo>> findUserAlbumByPage(@PathVariable int page, @PathVariable int limit, @RequestBody AlbumInfoQuery albumInfoQuery) {
       log.info("[]getUserAlbumByPage目标方法执行了");
        //1.分装用户ID查询条件
        Long userId = AuthContextHolder.getUserId();
        log.info("用户id为：{}",userId);
        albumInfoQuery.setUserId(userId);
        //2.调用业务层完成分页查询
        //2.1构建业务层或者持久层进行分页查询所需分页对象 封装两个参数：页码和页大小
        Page<AlbumListVo> pageInfo = new Page<>(page, limit);
        pageInfo = albumInfoService.getfindUserAlbumPage(pageInfo, albumInfoQuery);
        return Result.ok(pageInfo);
    }

    /*
     * /api/album/albumInfo/removeAlbumInfo/{id}
     * 根据id删除专辑
     */
    @KingLogin
    @Operation(summary = "根据id删除专辑")
    @DeleteMapping("/albumInfo/removeAlbumInfo/{id}")
    public Result removeAlbumInfo(@PathVariable Long id)                    {
        albumInfoService.removeAlbumInfo(id);
        return Result.ok();
    }

    /*
     * /api/album/albumInfo/getAlbumInfo/{id}
     * 根据id查询专辑信息
     */
   // @KingLogin
    @Operation(summary = "根据id查询编辑信息")
    @GetMapping("/albumInfo/getAlbumInfo/{id}")
    public Result<AlbumInfo> getAlbumInfo(@PathVariable Long id) {
        AlbumInfo albumInfo = albumInfoService.getAlbumInfo(id);
        return Result.ok(albumInfo);
    }

    @KingLogin
    @Operation(summary = "修改专辑")
    @PutMapping("/albumInfo/updateAlbumInfo/{id}")
    public Result updateAlbumInfo(@PathVariable("id") Long id, @RequestBody AlbumInfoVo albumInfoVo) {
        albumInfoService.updateAlbumInfo(id, albumInfoVo);
        return Result.ok();
    }
        @Operation(summary = "获取当前用户全部专辑列表")
        @GetMapping("/albumInfo/findUserAllAlbumList")
        public Result<List<AlbumInfo>> findUserAllAlbumList () {
            Long userId = AuthContextHolder.getUserId();
            List<AlbumInfo> list =  albumInfoService.findUserAllAlbumList(userId);

    return Result.ok(list);
    }
    }

