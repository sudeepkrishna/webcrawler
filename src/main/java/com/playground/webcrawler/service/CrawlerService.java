package com.playground.webcrawler.service;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class CrawlerService {

  private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

  private final Set<String> visitedUrls;
  private final Deque<String> queue;
  private final int maxDepth;

  private Crawlable crawlableTask;

  public CrawlerService(Crawlable crawlableTask) {
    this.visitedUrls = new HashSet<>();
    this.queue = new ArrayDeque<>();
    this.crawlableTask = crawlableTask;
    this.maxDepth = crawlableTask.getMaxDepth();
  }

  public List<String> crawl() {
    queue.offer(crawlableTask.getSeedUrl());
    visit();
    crawlableTask.finish();
    return new ArrayList<>(visitedUrls);
  }

  private void visit() {
    while (!queue.isEmpty()) {
      String page = queue.pollFirst();
      if (visitedUrls.contains(page)) { // check if the page is already visited
        continue;
      }
      visitedUrls.add(page); // mark page as visited

      if (visitedUrls.size() + queue.size() < maxDepth) {
        List<String> nextUrls = crawlableTask.crawl(page);

        Iterator<String> iterator = nextUrls.iterator();
        while (iterator.hasNext() && visitedUrls.size() + queue.size() < maxDepth) {
          queue.offer(iterator.next());
        }
      }
    }
  }
}
