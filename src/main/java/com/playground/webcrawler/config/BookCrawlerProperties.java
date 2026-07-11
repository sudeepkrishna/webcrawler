package com.playground.webcrawler.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "book-crawler")
public record BookCrawlerProperties(
    String host, String seedPage, int maxDepth, boolean writeToFileEnabled, int topN) {}
