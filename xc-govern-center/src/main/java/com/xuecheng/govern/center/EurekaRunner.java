package com.xuecheng.govern.center;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

/**
 * @author siyang
 * @create 2020-03-18 14:13
 */
@SpringBootApplication
@EnableEurekaServer
public class EurekaRunner {
    public static void main(String[] args) {
        SpringApplication.run(EurekaRunner.class,args);
    }
}
