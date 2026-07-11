package com.playground.webcrawler.service;

import java.util.List;

public interface Crawlable {

  List<String> crawl(String page);

  void finish();

  int getMaxDepth();

  String getSeedUrl();
}
