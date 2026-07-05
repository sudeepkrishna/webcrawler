package com.playground.webcrawler.config;

import java.net.http.HttpClient;
import java.time.Duration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.retry.RetryPolicy;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.web.client.RestClient;

@Configuration
public class WebConfig {

  @Bean
  public HttpClient httpClient() {
    return HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10L)).build();
  }

  @Bean
  public ClientHttpRequestFactory clientHttpRequestFactory(HttpClient httpClient) {
    return new JdkClientHttpRequestFactory(httpClient);
  }

  @Bean
  public RetryPolicy retryPolicy() {
    return RetryPolicy.builder().maxRetries(3).build();
  }

  @Bean
  public RetryTemplate retryTemplate(RetryPolicy retryPolicy) {
    return new RetryTemplate(retryPolicy);
  }

  @Bean
  public RestClient restClient(ClientHttpRequestFactory requestFactory) {
    // set timeout through a request factory. This uses Java's HttpClient as the http client engine
    return RestClient.builder().requestFactory(requestFactory).build();
  }
}
