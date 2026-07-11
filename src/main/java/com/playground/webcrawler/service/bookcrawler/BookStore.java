package com.playground.webcrawler.service.bookcrawler;

import com.playground.webcrawler.model.Book;
import java.util.Comparator;
import java.util.PriorityQueue;
import org.springframework.stereotype.Repository;

@Repository
public class BookStore {

  private final int limit;

  private final PriorityQueue<Book> minHeap =
      new PriorityQueue<>(Comparator.comparingDouble(Book::price));

  public BookStore(int limit) {
    this.limit = limit;
  }

  public void addBook(Book book) {
    minHeap.offer(book);
    if (minHeap.size() > limit) {
      minHeap.poll();
    }
  }

  public Book[] getTopBooks() {
    Book[] books = new Book[minHeap.size()];
    for (int i = minHeap.size(); i > 0; i--) {
      books[i - 1] = minHeap.poll();
    }
    return books;
  }

  public int getSize() {
    return minHeap.size();
  }
}
