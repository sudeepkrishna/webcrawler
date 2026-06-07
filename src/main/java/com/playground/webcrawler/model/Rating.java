package com.playground.webcrawler.model;

public enum Rating {
  ONE("One"),
  TWO("Two"),
  THREE("Three"),
  FOUR("Four"),
  FIVE("Five");

  private String value;

  Rating(String value) {
    this.value = value;
  }
}
