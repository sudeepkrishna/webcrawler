package com.playground.webcrawler.util;

import java.net.URI;
import java.util.Locale;

public class UrlNormaliser {

  public static String normalise(String rawUrl) {
    URI uri = URI.create(rawUrl);
    return uri.normalize().toString().toLowerCase(Locale.ROOT);
  }
}
