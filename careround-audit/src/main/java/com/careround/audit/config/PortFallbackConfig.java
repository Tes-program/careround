package com.careround.audit.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.server.ConfigurableWebServerFactory;
import org.springframework.boot.web.server.WebServerFactoryCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.net.ServerSocket;

/**
 * Tries to bind on the primary server port (default 8082).
 * If that port is already in use it transparently falls back to
 * {@code app.server.fallback-port} (default 8083) so that a
 * developer can still start the audit service without having to
 * hunt down and kill whatever owns 8082.
 */
@Slf4j
@Configuration
public class PortFallbackConfig {

    @Value("${server.port:8082}")
    private int primaryPort;

    @Value("${app.server.fallback-port:8083}")
    private int fallbackPort;

    @Bean
    public WebServerFactoryCustomizer<ConfigurableWebServerFactory> portFallbackCustomizer() {
        return factory -> {
            if (isPortAvailable(primaryPort)) {
                log.info("Audit service starting on primary port {}", primaryPort);
            } else {
                log.warn("Port {} is already in use – falling back to port {}", primaryPort, fallbackPort);
                factory.setPort(fallbackPort);
            }
        };
    }

    private boolean isPortAvailable(int port) {
        try (ServerSocket ss = new ServerSocket(port)) {
            ss.setReuseAddress(true);
            return true;
        } catch (IOException e) {
            return false;
        }
    }
}
