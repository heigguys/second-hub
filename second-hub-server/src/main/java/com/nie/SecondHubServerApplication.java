package com.nie;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@MapperScan("com.nie.secondhub.mapper")
@ConfigurationPropertiesScan("com.nie.secondhub.config")
public class SecondHubServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(SecondHubServerApplication.class, args);
    }

}
