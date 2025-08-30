package com.atguigu.tingshu.album.config;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author 杨健炜
 * 2025/8/30
 * 12:48
 **/
@Configuration
public class MinIOConfig {
    @Autowired
    private MinioConstantProperties minioConstantProperties;
    @Bean
    public MinioClient minioClient(){
        return MinioClient.builder()
                .endpoint(minioConstantProperties.getEndpointUrl())//操作MinIO地址 端口9000
                .credentials(minioConstantProperties.getAccessKey(),minioConstantProperties.getSecreKey())
                .build();
    }
}
