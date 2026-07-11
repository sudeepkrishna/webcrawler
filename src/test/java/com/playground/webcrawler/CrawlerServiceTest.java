package com.playground.webcrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.playground.webcrawler.service.Crawlable;
import com.playground.webcrawler.service.CrawlerService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CrawlerServiceTest {

  @Mock private Crawlable crawlableTask;

  @Test
  void exitsWhenQueueIsEmpty() {
    when(crawlableTask.getSeedUrl()).thenReturn("page1");
    when(crawlableTask.getMaxDepth()).thenReturn(5);
    when(crawlableTask.crawl(any())).thenReturn(List.of());

    CrawlerService crawlerService = new CrawlerService(crawlableTask);

    List<String> visitedUrls = crawlerService.crawl();

    verify(crawlableTask, times(1)).crawl(any());
    assertThat(visitedUrls.size()).isEqualTo(1);
  }

  @Test
  void doesNotVisitSeenUrl() {
    when(crawlableTask.getSeedUrl()).thenReturn("page1");
    when(crawlableTask.getMaxDepth()).thenReturn(5);
    when(crawlableTask.crawl(any())).thenReturn(List.of("page1", "page1"));

    CrawlerService crawlerService = new CrawlerService(crawlableTask);

    List<String> visitedUrls = crawlerService.crawl();

    verify(crawlableTask, times(1)).crawl(any());
    assertThat(visitedUrls.size()).isEqualTo(1);
  }

  @Test
  void stopsCrawlingWhenNumberOfVisitedUrlsAndUrlsToVisitExceedMaxDepth() {
    when(crawlableTask.getSeedUrl()).thenReturn("page1");
    when(crawlableTask.getMaxDepth()).thenReturn(3);
    when(crawlableTask.crawl(any())).thenReturn(List.of("page2", "page3", "page4", "page5"));

    CrawlerService crawlerService = new CrawlerService(crawlableTask);

    List<String> visitedUrls = crawlerService.crawl();

    assertThat(visitedUrls.size()).isEqualTo(3);
  }
}
