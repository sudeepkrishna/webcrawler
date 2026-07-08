package com.playground.webcrawler.service;

import com.playground.webcrawler.client.WebClient;
import com.playground.webcrawler.exception.HttpClientException;
import com.playground.webcrawler.model.Book;
import com.playground.webcrawler.util.UrlNormaliser;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashSet;
import java.util.Set;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.retry.RetryException;
import org.springframework.stereotype.Component;

@Component
public class CrawlerService {

  private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

  private final WebClient webClient;
  private final DataStore dataStore;
  private final Set<String> visitedUrls;
  private final Deque<String> pagesToVisit;

  @Value("${seed.page}")
  private String seedPage;

  @Value("${crawler.output}")
  private String outputDirectory;

  @Value("${top.N}")
  private int topN;

  @Value("${crawler.pagesToCrawl}")
  private int pagesToCrawl;

  @Value("${crawler.writeToFile}")
  private boolean isWriteToFileEnabled;

  public CrawlerService(WebClient webClient, DataStore dataStore) {
    this.webClient = webClient;
    this.dataStore = dataStore;
    visitedUrls = new HashSet<>();
    pagesToVisit = new ArrayDeque<>();
  }

  public void crawl() {
    pagesToVisit.offer(seedPage);
    visit();
  }

  private void visit() {
    int pagesVisited = 0;
    while (!pagesToVisit.isEmpty() && pagesVisited < pagesToCrawl) {
      String page = pagesToVisit.pollFirst();
      pagesVisited++;
      if (visitedUrls.contains(page)) { // check if the page is already visited
        continue;
      }
      visitedUrls.add(page); // mark page as visited

      String result;
      try {
        result = webClient.sendRequest(page); // send request
      } catch (RetryException retryException) {
        logger.error("Error in getting a response from target");
        throw new HttpClientException("Error in getting a response from target", retryException);
      }

      if (isWriteToFileEnabled) {
        writeToFile(page, result); // write results
      }

      Document document = Jsoup.parse(result); // convert raw string to Document
      // read elements with tag article and class product_pod
      Elements books = document.getElementsByAttributeValue("class", "product_pod"); // find books
      Elements next = document.getElementsByAttributeValue("class", "next"); // find the next page

      processBooks(books);

      if (!next.isEmpty()) {
        String nextPageUrl = addCatalogueIfMissing(next.selectFirst("a").attr("href"));
        pagesToVisit.add(UrlNormaliser.normalise(nextPageUrl));
      } else {
        logger.info("Cannot find next element");
      }
    }
    logger.info("\n\n\n\n\n\n********************Top {} books are********************\n\n", topN);
    logger.info("Total size of data store: {}", dataStore.getSize());
    logger.info("Pages visited: {}", pagesVisited);
    logBooks(dataStore.getTopBooks());
  }

  private void writeToFile(String relativePath, String content) {
    logger.info("Writing page {}", relativePath);

    try {
      Path file = Paths.get("crawler-output").resolve(relativePath);
      Files.createDirectories(file.getParent());
      Files.writeString(file, content);
    } catch (IOException e) {
      logger.warn("Could not write contents to file at: {}", relativePath);
    }
  }

  private void processBooks(Elements books) {
    for (Element e : books) {
      // logger.info("Element {}:", e);
      String title = e.select("h3 a").attr("title");
      String price =
          e.getElementsByAttributeValue("class", "product_price").selectFirst("p").text();
      try {
        double numericPrice = toNumericPrice(price);
        Book book = new Book(title, numericPrice);
        dataStore.addBook(book);
      } catch (NumberFormatException numberFormatException) {
        logger.warn("Ignoring entry because of invalid price");
      }
    }
  }

  private double toNumericPrice(String price) {
    return Double.parseDouble(price.replace("£", "").replace("Â", ""));
  }

  private void logBooks(Book[] books) {
    if (books.length == 0) {
      logger.info("No books found");
      return;
    }

    for (int i = 1; i <= books.length; i++) {
      Book book = books[i - 1];
      logger.info("#{} - Title : {}, Price : {}", i, book.title(), book.price());
    }
    logger.info("Logged all books");
  }

  private String addCatalogueIfMissing(String path) {
    if (!path.startsWith("catalogue")) {
      return "catalogue/" + path;
    }
    return path;
  }
}
