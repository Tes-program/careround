package com.careround.scheduler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

/**
 * Standalone background scheduler — no HTTP server.
 * server.port=-1 is set in application.yml to suppress the web server.
 * Single instance only — Quartz JDBC JobStore provides cluster safety.
 */
@SpringBootApplication
@EntityScan(basePackages = "com.careround.common.entity")
@EnableJpaRepositories(basePackages = "com.careround.common.repository")
public class CareRoundSchedulerApplication {

    public static void main(String[] args) {
        SpringApplication.run(CareRoundSchedulerApplication.class, args);
    }
}
