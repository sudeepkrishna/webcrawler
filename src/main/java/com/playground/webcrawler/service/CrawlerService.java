package com.playground.webcrawler.service;

import com.playground.webcrawler.client.WebClient;
import com.playground.webcrawler.model.Book;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.http.HttpRequest;
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
import org.springframework.stereotype.Component;

@Component
public class CrawlerService {

  private static final Logger logger = LoggerFactory.getLogger(CrawlerService.class);

  private final WebClient webClient;
  private final DataStore dataStore;
  private final Set<String> visitedUrls;
  private final Deque<String> pagesToVisit;

  @Value("${seed.host}")
  private String host;

  @Value("${seed.page}")
  private String seedPage;

  @Value("${crawler.output}")
  private String outputDirectory;

  public CrawlerService(WebClient webClient, DataStore dataStore) {
    this.webClient = webClient;
    this.dataStore = dataStore;
    visitedUrls = new HashSet<>();
    pagesToVisit = new ArrayDeque<>();
  }

  public void crawl() throws URISyntaxException, IOException {
    pagesToVisit.offer(seedPage);
    visit();
  }

  private void visit() throws URISyntaxException {
    while (!pagesToVisit.isEmpty()) {
      String page = pagesToVisit.pollFirst();
      if (visitedUrls.contains(page)) { // check if the page is already visited
        continue;
      }
      visitedUrls.add(page); // mark page as visited

      String result = webClient.sendRequest(buildRequest(page)); // send request

      // writeToFile(page, result); //write results

      Document document = Jsoup.parse(result); // convert raw string to Document
      // read elements with tag article and class product_pod
      Elements books = document.getElementsByAttributeValue("class", "product_pod"); // find books
      Elements next = document.getElementsByAttributeValue("class", "next"); // find the next page

      if (next.isEmpty()) {
        logger.info("Cannot find next element");
        continue;
      }

      for (Element e : books) {
        // logger.info("Element {}:", e);
        String title = e.select("h3 a").attr("title");
        String price =
            e.getElementsByAttributeValue("class", "product_price").selectFirst("p").text();
        Book book = new Book(title, toNumericPrice(price));
        dataStore.addBook(book);
      }

      if (!next.isEmpty()) {
        pagesToVisit.add(addCatalogueIfMissing(next.selectFirst("a").attr("href")));
      }
    }
    logger.info("\n\n\n\n\n\n********************Top N books are********************");
    logBooks(dataStore.getTopBooks());
  }

  private HttpRequest buildRequest(String page) throws URISyntaxException {
    return HttpRequest.newBuilder().uri(new URI(host + page)).GET().build();
  }

  private void writeToFile(String relativePath, String content) throws IOException {
    logger.info("Writing page {}", relativePath);

    Path file = Paths.get("crawler-output").resolve(relativePath);

    Files.createDirectories(file.getParent());

    Files.writeString(file, content);
  }

  private double toNumericPrice(String price) {
    return Double.parseDouble(price.substring(1));
  }

  private void logBooks(Book[] books) {
    for (int i = 1; i <= books.length; i++) {
      Book book = books[i - 1];
      logger.info("#{} - Title : {}, Price : {}", i, book.title(), book.price());
    }
  }

  private String addCatalogueIfMissing(String path) {
    if (!path.startsWith("catalogue")) {
      return "catalogue/" + path;
    }
    return path;
  }
}
