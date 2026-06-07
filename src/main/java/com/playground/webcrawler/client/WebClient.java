package com.playground.webcrawler.client;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class WebClient {

  private static final Logger logger = LoggerFactory.getLogger(WebClient.class);
  private final HttpClient httpClient;

  public WebClient(HttpClient httpClient) {
    this.httpClient = httpClient;
  }

  public String sendRequest(HttpRequest request) {
    try {
      return httpClient.send(request, HttpResponse.BodyHandlers.ofString()).body();
    } catch (IOException | InterruptedException e) {
      logger.info("WebClient: Error while sending request");
      return null;
    }
  }
}
