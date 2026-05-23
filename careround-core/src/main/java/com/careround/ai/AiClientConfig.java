package com.careround.ai;

import io.netty.channel.ChannelOption;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.client.RestClient;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;

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

    @Bean
    WebClient aiWebClient(AiProperties props) {
        HttpClient httpClient = HttpClient.create()
                .option(ChannelOption.CONNECT_TIMEOUT_MILLIS, props.connectTimeoutSeconds() * 1000)
                .responseTimeout(Duration.ofSeconds(props.readTimeoutSeconds()));

        return WebClient.builder()
                .baseUrl(props.serviceUrl())
                .clientConnector(new ReactorClientHttpConnector(httpClient))
                .build();
    }
}
