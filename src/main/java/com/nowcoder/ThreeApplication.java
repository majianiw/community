package com.nowcoder;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import javax.annotation.PostConstruct;

@MapperScan(basePackages="com.nowcoder.community.dao")
@SpringBootApplication
public class ThreeApplication {

    //在Spring IoC容器构造bean之后调用@PostConstruct注释的方法
    @PostConstruct
    public void init(){
        // 解决netty启动冲突问题
        // see Netty4Utils.setAvailableProcessors()
        System.setProperty("es.set.netty.runtime.available.processors", "false");
    }

    public static void main(String[] args) {
        SpringApplication.run(ThreeApplication.class, args);
    }

}
