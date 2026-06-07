package com.playground.webcrawler.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.net.http.HttpClient;
import java.time.Duration;

@Configuration
public class WebConfig {

    @Bean
    public HttpClient httpClient() {
        return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
    }
}
