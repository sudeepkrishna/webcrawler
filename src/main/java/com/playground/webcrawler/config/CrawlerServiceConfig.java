package com.playground.webcrawler.config;

import com.playground.webcrawler.client.WebClient;
import com.playground.webcrawler.service.Crawlable;
import com.playground.webcrawler.service.bookcrawler.BookCrawler;
import com.playground.webcrawler.service.bookcrawler.BookStore;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class CrawlerServiceConfig {

  @Bean
  public BookStore bookStore(BookCrawlerProperties properties) {
    return new BookStore(properties.topN());
  }

  @Bean
  public Crawlable bookCrawler(
      WebClient webClient, BookStore bookStore, BookCrawlerProperties properties) {
    return new BookCrawler(webClient, bookStore, properties);
  }
}
