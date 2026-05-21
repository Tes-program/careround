package com.careround.ai;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
@EnableConfigurationProperties(AiProperties.class)
public class AiClientConfig {

    @Bean
    RestClient aiRestClient(AiProperties props) {
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(props.connectTimeoutSeconds()));
        factory.setReadTimeout(Duration.ofSeconds(props.readTimeoutSeconds()));

        return RestClient.builder()
                .baseUrl(props.serviceUrl())
                .requestFactory(factory)
                .build();
    }
}
