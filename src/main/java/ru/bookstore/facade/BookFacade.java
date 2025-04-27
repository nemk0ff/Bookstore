package ru.bookstore.facade;

import java.time.LocalDateTime;
import java.util.List;
import ru.bookstore.model.impl.Book;
import ru.bookstore.sorting.BookSort;

public interface BookFacade {

  Book addBook(Long bookId, Integer amount, LocalDateTime addDate);

  Book writeOff(Long bookId, Integer amount, LocalDateTime saleDate);

  Book get(Long bookId);

  List<Book> getAll(BookSort sortType);

  List<Book> getStale(BookSort sortType);

  void importBook(Book book);
}
