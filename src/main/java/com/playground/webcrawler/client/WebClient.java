package com.playground.webcrawler.client;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;
import org.springframework.core.retry.RetryTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

@Component
public class WebClient {

  private static final Logger logger = LoggerFactory.getLogger(WebClient.class);
  private final RestClient restClient;
  private final RetryTemplate retryTemplate;

  public WebClient(RestClient restClient, RetryTemplate retryTemplate) {
    this.restClient = restClient;
    this.retryTemplate = retryTemplate;
  }

  public String sendRequest(String url) throws RetryException {
    logger.info("Sending request to {}", url);
    return retryTemplate.execute(() -> restClient.get().uri(url).retrieve().body(String.class));
  }
}
