package com.renthouse;

import com.renthouse.common.config.AppSecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableConfigurationProperties(AppSecurityProperties.class)
@EnableScheduling
public class RentHouseApplication {
    public static void main(String[] args) {
        SpringApplication.run(RentHouseApplication.class, args);
    }
}
