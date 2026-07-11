package com.playground.webcrawler.service.bookcrawler;

import com.playground.webcrawler.client.WebClient;
import com.playground.webcrawler.config.BookCrawlerProperties;
import com.playground.webcrawler.exception.HttpClientException;
import com.playground.webcrawler.model.Book;
import com.playground.webcrawler.service.Crawlable;
import com.playground.webcrawler.util.UrlNormaliser;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.retry.RetryException;

public class BookCrawler implements Crawlable {

  private static final Logger logger = LoggerFactory.getLogger(BookCrawler.class);
  private final WebClient webClient;
  private final BookStore bookStore;
  private final BookCrawlerProperties properties;
  private final String host;

  public BookCrawler(WebClient webClient, BookStore bookStore, BookCrawlerProperties properties) {
    this.webClient = webClient;
    this.bookStore = bookStore;
    this.properties = properties;
    this.host = properties.host();
  }

  @Override
  public List<String> crawl(String url) {
    List<String> nextUrls = new ArrayList<>();
    String result;

    try {
      result = webClient.sendRequest(url); // send request
    } catch (RetryException retryException) {
      logger.error("Error in getting a response from target");
      throw new HttpClientException("Error in getting a response from target", retryException);
    }

    if (properties.writeToFileEnabled()) {
      writeToFile(url, result); // write results
    }

    Document document = Jsoup.parse(result); // convert raw string to Document
    // read elements with tag article and class product_pod
    Elements books = document.getElementsByAttributeValue("class", "product_pod"); // find books
    Elements next = document.getElementsByAttributeValue("class", "next"); // find the next page

    processBooks(books);

    if (!next.isEmpty()) {
      String nextPage = addCatalogueIfMissing(next.selectFirst("a").attr("href"));
      try {
        String nextUrl = UrlNormaliser.normalise(host, nextPage);
        nextUrls.add(nextUrl);
      } catch (URISyntaxException e) {
        logger.warn("Error parsing next url, ignoring");
      }
    } else {
      logger.info("Cannot find next element");
    }
    return nextUrls;
  }

  @Override
  public void finish() {
    Book[] books = bookStore.getTopBooks();
    if (books.length == 0) {
      logger.info("No books found");
      return;
    }
    logger.info("\n\n********************Top books are********************\n\n");
    logger.info("Total size of data store: {}", bookStore.getSize());
    for (int i = 1; i <= books.length; i++) {
      Book book = books[i - 1];
      logger.info("#{} - Title : {}, Price : {}", i, book.title(), book.price());
    }
    logger.info("Logged all books");
  }

  @Override
  public int getMaxDepth() {
    return properties.maxDepth();
  }

  @Override
  public String getSeedUrl() {
    return properties.host() + properties.seedPage();
  }

  private double toNumericPrice(String price) {
    return Double.parseDouble(price.replace("£", "").replace("Â", ""));
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
        bookStore.addBook(book);
      } catch (NumberFormatException numberFormatException) {
        logger.warn("Ignoring entry because of invalid price");
      }
    }
  }

  private String addCatalogueIfMissing(String path) {
    if (!path.startsWith("catalogue")) {
      return "catalogue/" + path;
    }
    return path;
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
}
