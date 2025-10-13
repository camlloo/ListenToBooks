package com.atguigu.tingshu.common.config.thread;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.*;

/**
 * @author 杨健炜
 * 2025/10/11
 * 13:26
 **/

@Configuration
public class ThreadPoolConfig {
    /*
     *注册自定义线程池
     *
     * @return
     */
    @Bean
    public ThreadPoolExecutor theadPoolExecutor(){
        //获取逻辑处理器个数
        int processorsCount = Runtime.getRuntime().availableProcessors();
        processorsCount= processorsCount*2;

        //1.创建线程池对象
        ThreadPoolExecutor threadPoolExecutor = new ThreadPoolExecutor(
                processorsCount,
                processorsCount,
                0,
                TimeUnit.SECONDS,
                new ArrayBlockingQueue<>(200),
                Executors.defaultThreadFactory(),
                (r, t) -> {
                    //触发自定义拒绝策略，要求任务必须执行
                    try {
                        Thread.sleep(200);
                    } catch (InterruptedException e) {
                        throw new RuntimeException(e);
                    }
                    //将被拒绝任务，再次尝试提交给线程池执行
                    t.submit(r);
                }
        );
        //2.如果项目启动需要立即处理大量任务，提交将核心线程创建
        threadPoolExecutor.prestartAllCoreThreads();

        return threadPoolExecutor;
    }
}
