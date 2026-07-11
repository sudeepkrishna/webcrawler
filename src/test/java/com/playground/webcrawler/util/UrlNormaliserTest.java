package com.playground.webcrawler.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.net.URISyntaxException;
import java.util.stream.Stream;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UrlNormaliserTest {

  private static final String EXAMPLE_BOOK_HOST = "https://example.com/books/";

  @ParameterizedTest
  @MethodSource("normalisableUrls")
  void normalisesUrls(String description, String host, String relativePath, String normalisedUrl)
      throws URISyntaxException {
    assertThat(UrlNormaliser.normalise(host, relativePath)).isEqualTo(normalisedUrl);
  }

  @ParameterizedTest
  @MethodSource("invalidUrls")
  void rejectsInvalidUrls(String description, String host, String relativePath) {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> UrlNormaliser.normalise(host, relativePath));
  }

  @Test
  void lowercasesOnlySchemeAndHost() throws URISyntaxException {
    String result = UrlNormaliser.normalise("HTTP://EXAMPLE.COM/Books/", "History");

    assertThat(result).isEqualTo("http://example.com/Books/History");
  }

  @Test
  void removesDefaultPortForHttp() throws URISyntaxException {
    String result = UrlNormaliser.normalise("http://example.com:80/books/", "history");

    assertThat(result).isEqualTo("http://example.com/books/history");
  }

  @Test
  void removesDefaultPortForHttps() throws URISyntaxException {
    String result = UrlNormaliser.normalise("https://example.com:443/books/", "history");

    assertThat(result).isEqualTo("https://example.com/books/history");
  }

  @Test
  void removesUserInfo() throws URISyntaxException {
    String result = UrlNormaliser.normalise("https://first.last@example.com/books/", "history");

    assertThat(result).isEqualTo("https://example.com/books/history");
  }

  private static Stream<Arguments> normalisableUrls() {
    return Stream.of(
        Arguments.of(
            "removes parent path segments",
            EXAMPLE_BOOK_HOST,
            "fiction/../history",
            "https://example.com/books/history"),
        Arguments.of(
            "removes current path segments",
            EXAMPLE_BOOK_HOST,
            "./history",
            "https://example.com/books/history"),
        Arguments.of(
            "collapses repeated separators when resolving relative path segments",
            EXAMPLE_BOOK_HOST,
            "https://example.com/books//fiction/../history",
            "https://example.com/books/history"),
        Arguments.of(
            "removes fragments",
            EXAMPLE_BOOK_HOST,
            "guide#section-2",
            "https://example.com/books/guide"),
        Arguments.of(
            "preserves explicit ports",
            "https://example.com:8443/",
            "a/b/../c",
            "https://example.com:8443/a/c"),
        Arguments.of(
            "keeps already encoded path separators encoded during path normalisation",
            EXAMPLE_BOOK_HOST,
            "../done",
            "https://example.com/done"),
        Arguments.of(
            "preserves unicode path characters",
            EXAMPLE_BOOK_HOST,
            "ÜBER/../CAFÉ",
            "https://example.com/books/CAFÉ"));
  }

  private static Stream<Arguments> invalidUrls() {
    return Stream.of(
        Arguments.of("rejects spaces in paths", "https://example.com/", "books with spaces"),
        Arguments.of("rejects malformed schemes", "https://example .com/", "books"));
  }
}
