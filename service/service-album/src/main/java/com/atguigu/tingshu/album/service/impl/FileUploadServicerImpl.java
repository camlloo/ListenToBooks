package com.atguigu.tingshu.album.service.impl;

import cn.hutool.core.date.DateUtil;
import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.IdUtil;
import com.atguigu.tingshu.album.config.MinioConstantProperties;
import com.atguigu.tingshu.album.service.FileUploadServicer;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.*;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;

/**
 * @author 杨健炜
 * 2025/8/30
 * 12:56
 **/
@Slf4j
@Service
public class FileUploadServicerImpl implements FileUploadServicer {
    @Autowired
    private  MinioClient minioClient;
    @Autowired
    private MinioConstantProperties minioConstantProperties;
    @Override
    public String uploadImage(MultipartFile file) {
        try {
            //1.校验上传文件是否为图片
            BufferedImage read = ImageIO.read(file.getInputStream());
            log.info("read:{}",read);
            if(read == null){
                throw new RuntimeException("图片格式非法!");
            }
            //2.生成MInIO中存储唯一文件名称，形式：/当前日期/文件名称.后缀名
            String folderName = DateUtil.today();
            String fileName = IdUtil.randomUUID();
            String extName = FileUtil.extName(file.getOriginalFilename());
            //对象名称=/2023-10-15/dijdio.png
            String objectName = "/"+folderName+"/"+fileName+"."+extName;
            //3.调用上传文件方法
            minioClient.putObject(
                    PutObjectArgs.builder().bucket(minioConstantProperties.getBucketName()).object(objectName).stream(
                            file.getInputStream(),file.getSize(),-1)
                            .contentType(file.getContentType())
                            .build());
            return minioConstantProperties.getEndpointUrl()+"/"+minioConstantProperties.getBucketName()+objectName;
        } catch (Exception e) {
            log.error("[专辑服务]文件上传失败：{}",e);
            throw new RuntimeException(e);
        }
    }
}
