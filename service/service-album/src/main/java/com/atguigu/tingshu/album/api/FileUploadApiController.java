package com.atguigu.tingshu.album.api;

import com.atguigu.tingshu.album.service.FileUploadServicer;
import com.atguigu.tingshu.common.result.Result;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@Slf4j
@Tag(name = "上传管理接口")
@RestController
@RequestMapping("api/album")
public class FileUploadApiController {
    @Autowired
    private FileUploadServicer fileUploadServicer;

    /**
     * 文件上传接口
     * /api/album/fileUpload
     *
     * @param file
     * @return 文件在线地址
     */
    @Operation(summary = "文件上传")
    @RequestMapping("/fileUpload")
    public Result<String> fileUpload(MultipartFile file) {
        log.info("上传文件为{}", file);
        String fileUrl = fileUploadServicer.uploadImage(file);
        log.info("返回数据:{}", fileUrl);
        return Result.ok(fileUrl);
    }
}
