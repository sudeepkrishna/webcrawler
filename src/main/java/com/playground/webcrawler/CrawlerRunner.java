package com.playground.webcrawler;

import com.playground.webcrawler.service.CrawlerService;
import java.io.IOException;
import java.net.URISyntaxException;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;

@Component
public class CrawlerRunner implements ApplicationRunner {

  private final CrawlerService crawlerService;

  public CrawlerRunner(CrawlerService crawlerService) {
    this.crawlerService = crawlerService;
  }

  @Override
  public void run(ApplicationArguments args) throws URISyntaxException, IOException {
    crawlerService.crawl();
  }
}
