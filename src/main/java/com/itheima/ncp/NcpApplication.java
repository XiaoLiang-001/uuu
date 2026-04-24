package com.itheima.ncp;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.itheima.ncp.mapper")
public class NcpApplication {

    public static void main(String[] args) {
        SpringApplication.run(NcpApplication.class, args);
    }

}
