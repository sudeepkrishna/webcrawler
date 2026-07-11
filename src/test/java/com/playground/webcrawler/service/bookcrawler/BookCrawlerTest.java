package com.playground.webcrawler.service.bookcrawler;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.playground.webcrawler.client.WebClient;
import com.playground.webcrawler.config.BookCrawlerProperties;
import com.playground.webcrawler.exception.HttpClientException;
import com.playground.webcrawler.model.Book;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.retry.RetryException;

@ExtendWith(MockitoExtension.class)
public class BookCrawlerTest {

  private static final String HOST = "https://books.toscrape.com/";
  private static final String SEED_PAGE = "index.html";
  private static final String SEED_URL = HOST + SEED_PAGE;

  @Mock private WebClient webClient;

  @Test
  void returnsNextPageUrlFromUpstreamPage() throws Exception {
    when(webClient.sendRequest(SEED_URL)).thenReturn(upstreamPageHtml());
    BookCrawler bookCrawler = new BookCrawler(webClient, new BookStore(20), properties(20));

    List<String> nextUrls = bookCrawler.crawl(SEED_URL);

    verify(webClient, times(1)).sendRequest(SEED_URL);
    assertThat(nextUrls).containsExactly("https://books.toscrape.com/catalogue/page-2.html");
  }

  @Test
  void storesTopBooksFromUpstreamPage() throws Exception {
    when(webClient.sendRequest(SEED_URL)).thenReturn(upstreamPageHtml());
    BookStore bookStore = new BookStore(3);
    BookCrawler bookCrawler = new BookCrawler(webClient, bookStore, properties(3));

    bookCrawler.crawl(SEED_URL);

    Book[] books = bookStore.getTopBooks();
    assertThat(books).extracting(Book::title).containsExactly("Book1", "Book2", "Book3");
    assertThat(books).extracting(Book::price).containsExactly(60.06, 55.55, 50.12);
  }

  @Test
  void parsesOnlyProductPodElements() throws Exception {
    BookStore bookStore = new BookStore(5);
    BookCrawler bookCrawler = crawlerFor(bookStore, upstreamPageHtml());

    bookCrawler.crawl(SEED_URL);

    Book[] books = bookStore.getTopBooks();
    assertThat(books).extracting(Book::title).containsExactly("Book1", "Book2", "Book3", "Book4");
    assertThat(books).extracting(Book::price).containsExactly(60.06, 55.55, 50.12, 45.56);
  }

  @Test
  void readsFullTitleFromTitleAttributeInsteadOfVisibleText() throws Exception {
    BookStore bookStore = new BookStore(1);
    BookCrawler bookCrawler =
        crawlerFor(
            bookStore,
            """
            <html>
              <body>
                <article class="product_pod">
                  <h3><a title="A Full Book Title That Is Not Truncated">A Full Book...</a></h3>
                  <div class="product_price"><p class="price_color">10.00</p></div>
                </article>
              </body>
            </html>
            """);

    bookCrawler.crawl(SEED_URL);

    assertThat(bookStore.getTopBooks())
        .extracting(Book::title)
        .containsExactly("A Full Book Title That Is Not Truncated");
  }

  @Test
  void parsesPricesWithEncodingPrefixFromUpstreamHtml() throws Exception {
    BookStore bookStore = new BookStore(2);
    BookCrawler bookCrawler =
        crawlerFor(
            bookStore,
            """
            <html>
              <body>
                <article class="product_pod">
                  <h3><a title="Encoded Price Book">Encoded Price Book</a></h3>
                  <div class="product_price"><p class="price_color">£51.77</p></div>
                </article>
                <article class="product_pod">
                  <h3><a title="Plain Pound Book">Plain Pound Book</a></h3>
                  <div class="product_price"><p class="price_color">Â£40.10</p></div>
                </article>
              </body>
            </html>
            """);

    bookCrawler.crawl(SEED_URL);

    assertThat(bookStore.getTopBooks()).extracting(Book::price).containsExactly(51.77, 40.10);
  }

  @Test
  void ignoresBookWhenPriceIsNotNumeric() throws Exception {
    BookStore bookStore = new BookStore(5);
    BookCrawler bookCrawler =
        crawlerFor(
            bookStore,
            """
            <html>
              <body>
                <article class="product_pod">
                  <h3><a title="Valid Book">Valid Book</a></h3>
                  <div class="product_price"><p class="price_color">£15.25</p></div>
                </article>
                <article class="product_pod">
                  <h3><a title="Invalid Book">Invalid Book</a></h3>
                  <div class="product_price"><p class="price_color">not-a-price</p></div>
                </article>
              </body>
            </html>
            """);

    bookCrawler.crawl(SEED_URL);

    Book[] books = bookStore.getTopBooks();
    assertThat(books).extracting(Book::title).containsExactly("Valid Book");
    assertThat(books).extracting(Book::price).containsExactly(15.25);
  }

  @Test
  void prefixesCatalogueWhenNextHrefIsRelativePage() throws Exception {
    BookCrawler bookCrawler =
        crawlerFor(
            new BookStore(5),
            """
            <html>
              <body>
                <li class="next"><a href="page-3.html">next</a></li>
              </body>
            </html>
            """);

    List<String> nextUrls = bookCrawler.crawl(SEED_URL);

    assertThat(nextUrls).containsExactly("https://books.toscrape.com/catalogue/page-3.html");
  }

  @Test
  void keepsCataloguePrefixWhenNextHrefAlreadyHasOne() throws Exception {
    BookCrawler bookCrawler =
        crawlerFor(
            new BookStore(5),
            """
            <html>
              <body>
                <li class="next"><a href="catalogue/page-2.html">next</a></li>
              </body>
            </html>
            """);

    List<String> nextUrls = bookCrawler.crawl(SEED_URL);

    assertThat(nextUrls).containsExactly("https://books.toscrape.com/catalogue/page-2.html");
  }

  @Test
  void returnsNoNextUrlsWhenDocumentHasNoNextElement() throws Exception {
    BookCrawler bookCrawler =
        crawlerFor(
            new BookStore(5),
            """
            <html>
              <body>
                <li class="current">Page 50 of 50</li>
              </body>
            </html>
            """);

    List<String> nextUrls = bookCrawler.crawl(SEED_URL);

    assertThat(nextUrls).isEmpty();
  }

  @Test
  void wrapsRetryExceptionInHttpClientException() throws Exception {
    RetryException retryException =
        new RetryException("Could not reach target", new RuntimeException("Connection failed"));
    when(webClient.sendRequest(SEED_URL)).thenThrow(retryException);
    BookCrawler bookCrawler = new BookCrawler(webClient, new BookStore(20), properties(20));

    assertThatThrownBy(() -> bookCrawler.crawl(SEED_URL))
        .isInstanceOf(HttpClientException.class)
        .hasCause(retryException);
  }

  @Test
  void normalisesNextPageUrl() throws Exception {
    BookCrawler bookCrawler =
        crawlerFor(
            new BookStore(5),
            """
                      <html>
                        <body>
                          <li class="next"><a href="catalogue/../page-3.html">next</a></li>
                        </body>
                      </html>
                      """);

    List<String> nextUrls = bookCrawler.crawl(SEED_URL);

    assertThat(nextUrls).containsExactly("https://books.toscrape.com/page-3.html");
  }

  private BookCrawlerProperties properties(int topN) {
    return new BookCrawlerProperties(HOST, SEED_PAGE, 100, false, topN);
  }

  private BookCrawler crawlerFor(BookStore bookStore, String html) throws RetryException {
    when(webClient.sendRequest(SEED_URL)).thenReturn(html);
    return new BookCrawler(webClient, bookStore, properties(5));
  }

  private String upstreamPageHtml() {
    return """
        <html>
          <body>
            <article class="product_pod">
              <h3><a title="Book2">Book2</a></h3>
              <div class="product_price"><p class="price_color">55.55</p></div>
            </article>
            <article class="product_pod">
              <h3><a title="Book3">Book3</a></h3>
              <div class="product_price"><p class="price_color">50.12</p></div>
            </article>
            <article class="product_pod">
              <h3><a title="Book4">Book4</a></h3>
              <div class="product_price"><p class="price_color">45.56</p></div>
            </article>
            <article class="product_pod">
              <h3><a title="Book1">Book1</a></h3>
              <div class="product_price"><p class="price_color">60.06</p></div>
            </article>
            <article class="not_product_pod">
              <h3><a title="Ignored Book">Ignored Book</a></h3>
              <div class="product_price"><p class="price_color">&#163;99.99</p></div>
            </article>
            <li class="next"><a href="catalogue/page-2.html">next</a></li>
          </body>
        </html>
        """;
  }
}
