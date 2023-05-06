package com.nowcoder.community.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * @Date: create in 11:32 2023/3/27
 * @describe: 线程池的配置类
 */
@Configuration
@EnableScheduling
@EnableAsync
public class ThreadPoolConfig {

}
