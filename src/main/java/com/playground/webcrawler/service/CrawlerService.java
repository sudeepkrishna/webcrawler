package com.playground.webcrawler.service;

import com.playground.webcrawler.client.WebClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;

@Component
public class CrawlerService {

    private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

    private final WebClient webClient;

    @Value("${seed.url}")
    private String seedUrl;

    public CrawlerService(WebClient webClient) {
        this.webClient = webClient;
    }

    public void crawl() {
        HttpRequest request;

        try {
            request = HttpRequest.newBuilder()
                    .uri(new URI(seedUrl))
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            logger.error("Could not read URL, try again");
            return;
        }

        String result = webClient.sendRequest(request);

        logger.info("Result of crawl: {}", result);
    }
}
