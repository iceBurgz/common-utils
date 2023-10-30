package com.zhubin.commonutils;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class CommonUtilsApplication {

    public static void main(String[] args) {
        SpringApplication.run(CommonUtilsApplication.class, args);
    }

}
