package com.studio.jwt;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/** 启动入口 */
@SpringBootApplication
@MapperScan("com.studio.jwt.mapper")
public class JwtWorkshopApplication {

    public static void main(String[] args) {
        SpringApplication.run(JwtWorkshopApplication.class, args);
    }
}
