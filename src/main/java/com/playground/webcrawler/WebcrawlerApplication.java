package com.playground.webcrawler;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.resilience.annotation.EnableResilientMethods;

@SpringBootApplication
@EnableResilientMethods
public class WebcrawlerApplication {

  public static void main(String[] args) {
    SpringApplication.run(WebcrawlerApplication.class, args);
  }
}
