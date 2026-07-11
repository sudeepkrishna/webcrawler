package com.playground.webcrawler.util;

import java.net.URI;
import java.net.URISyntaxException;

public class UrlNormaliser {

  public static String normalise(String originalHost, String relativePath)
      throws URISyntaxException {
    URI hostUri = URI.create(originalHost);
    URI newUrl = hostUri.resolve(relativePath);

    String scheme = newUrl.getScheme().toLowerCase();
    String host = newUrl.getHost().toLowerCase();
    int originalPort = newUrl.getPort();
    String path = newUrl.getPath();
    String query = newUrl.getQuery();

    int newPort = isDefaultPort(scheme, originalPort) ? -1 : originalPort;

    URI normalisedUri = new URI(scheme, null, host, newPort, path, query, null);

    URI normalisedUriAfterPathCompression = normalisedUri.normalize();

    return normalisedUriAfterPathCompression.toString();
  }

  private static boolean isDefaultPort(String scheme, int port) {
    return scheme.equals("http") && port == 80 || scheme.equals("https") && port == 443;
  }
}
