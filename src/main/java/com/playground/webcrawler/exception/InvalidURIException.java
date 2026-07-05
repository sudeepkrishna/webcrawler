package com.playground.webcrawler.exception;

public class InvalidURIException extends RuntimeException {

  public InvalidURIException(String message, Throwable cause) {
    super(message, cause);
  }
}
