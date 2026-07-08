package com.playground.webcrawler.util;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

import java.util.stream.Stream;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class UrlNormaliserTest {

  @ParameterizedTest(name = "{0}")
  @MethodSource("normalisableUrls")
  void normalisesUrls(String description, String rawUrl, String normalisedUrl) {
    assertThat(UrlNormaliser.normalise(rawUrl)).isEqualTo(normalisedUrl);
  }

  @ParameterizedTest(name = "{0}")
  @MethodSource("invalidUrls")
  void rejectsInvalidUrls(String description, String rawUrl) {
    assertThatExceptionOfType(IllegalArgumentException.class)
        .isThrownBy(() -> UrlNormaliser.normalise(rawUrl));
  }

  private static Stream<Arguments> normalisableUrls() {
    return Stream.of(
        Arguments.of(
            "removes parent path segments",
            "https://example.com/books/fiction/../history",
            "https://example.com/books/history"),
        Arguments.of(
            "removes current path segments",
            "https://example.com/books/./history",
            "https://example.com/books/history"),
        Arguments.of(
            "collapses repeated separators when resolving relative path segments",
            "https://example.com/books//fiction/../history",
            "https://example.com/books/history"),
        Arguments.of(
            "lowercases scheme, host, and path characters",
            "HTTP://EXAMPLE.COM/Books/../History",
            "http://example.com/history"),
        Arguments.of(
            "lowercases query characters while preserving percent-encoded spaces",
            "https://example.com/search?q=Tom%20%26%20Jerry",
            "https://example.com/search?q=tom%20%26%20jerry"),
        Arguments.of(
            "lowercases html-encoded query characters",
            "https://example.com/search?q=Tom&AMP;page=1",
            "https://example.com/search?q=tom&amp;page=1"),
        Arguments.of(
            "lowercases fragments while normalising paths",
            "https://example.com/docs/./reference/../guide#Section-2",
            "https://example.com/docs/guide#section-2"),
        Arguments.of(
            "preserves explicit ports",
            "https://example.com:8443/a/b/../c",
            "https://example.com:8443/a/c"),
        Arguments.of(
            "keeps already encoded path separators encoded during path normalisation",
            "https://example.com/path%2Fsegment/../done",
            "https://example.com/done"),
        Arguments.of(
            "preserves unicode path characters",
            "https://example.com/books/ÜBER/../CAFÉ",
            "https://example.com/books/café"),
        Arguments.of(
            "preserves unicode host characters",
            "https://BÜCHER.example/fiction/../history",
            "https://bücher.example/history"));
  }

  private static Stream<Arguments> invalidUrls() {
    return Stream.of(
        Arguments.of("rejects spaces in paths", "https://example.com/books with spaces"),
        Arguments.of("rejects malformed schemes", "https://example .com/books"));
  }
}
