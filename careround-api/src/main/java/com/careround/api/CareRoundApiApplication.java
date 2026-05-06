package com.careround.api;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EntityScan(basePackages = "com.careround.common.entity")
@EnableJpaRepositories(basePackages = "com.careround.common.repository")
@EnableScheduling   // for the @Scheduled OutboxPoller only
public class CareRoundApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareRoundApiApplication.class, args);
    }
}
