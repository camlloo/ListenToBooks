package com.atguigu.tingshu.album.service;

import org.springframework.web.multipart.MultipartFile;

/**
 * @author 杨健炜
 * 2025/8/30
 * 12:56
 **/
public interface FileUploadServicer {
    String uploadImage(MultipartFile file);
}
