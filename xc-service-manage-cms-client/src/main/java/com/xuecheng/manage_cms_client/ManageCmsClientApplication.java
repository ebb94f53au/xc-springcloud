package com.xuecheng.manage_cms_client;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author siyang
 * @create 2020-03-12 16:05
 */
@SpringBootApplication
@EnableAutoConfiguration(exclude={DataSourceAutoConfiguration.class})
@EntityScan("com.xuecheng.framework.domain.cms")//扫描实体类
@ComponentScan(basePackages={"com.xuecheng.manage_cms_client"})//扫描本项目下所有包
@ComponentScan(basePackages={"com.xuecheng.framework"})//扫描common工程下的类
public class ManageCmsClientApplication {
    public static void main(String[] args) { SpringApplication.run(ManageCmsClientApplication.class, args); }
}
