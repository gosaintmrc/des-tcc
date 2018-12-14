package com.cmos.destcc;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * @author gosaint
 */
@SpringBootApplication
@MapperScan("com.cmos.destcc.com.cmos.dao")
public class DesTccApplication {

    public static void main(String[] args) {
        SpringApplication.run(DesTccApplication.class, args);
    }
}
