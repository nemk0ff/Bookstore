package ru.bookstore.facade.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.facade.BookFacade;
import ru.bookstore.model.impl.Book;
import ru.bookstore.service.BookService;
import ru.bookstore.sorting.BookSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookFacadeImpl implements BookFacade {
  private final BookService bookService;

  @Transactional
  @Override
  public Book addBook(Long id, Integer amount, LocalDateTime deliveredDate) {
    return bookService.add(id, amount, deliveredDate);
  }

  @Transactional
  @Override
  public Book writeOff(Long id, Integer amount, LocalDateTime writeOffDate) {
    return bookService.writeOff(id, amount, writeOffDate);
  }

  @Transactional(readOnly = true)
  @Override
  public Book get(Long bookId) {
    return bookService.get(bookId);
  }

  @Transactional
  @Override
  public void importBook(Book book) {
    bookService.importBook(book);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Book> getAll(BookSort sortType) {
    return switch (sortType) {
      case NAME -> bookService.getAllBooksByName();
      case PUBLICATION_DATE -> bookService.getAllBooksByDate();
      case PRICE -> bookService.getAllBooksByPrice();
      case STATUS -> bookService.getAllBooksByAvailable();
      default -> bookService.getAllBooksById();
    };
  }

  @Transactional(readOnly = true)
  @Override
  public List<Book> getStale(BookSort sortType) {
    return switch (sortType) {
      case STALE_BY_DATE -> bookService.getAllStaleBooksByDate();
      default -> bookService.getAllStaleBooksByPrice();
    };
  }
}
