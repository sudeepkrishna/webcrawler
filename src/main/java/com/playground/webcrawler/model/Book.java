package com.playground.webcrawler.model;

public record Book (String upc, String title, double price, String url, double rating, String availability) {
}
