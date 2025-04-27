package ru.bookstore.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bookstore.dao.BookDao;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.impl.Book;
import ru.bookstore.service.BookService;
import ru.bookstore.sorting.BookSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class BookServiceImpl implements BookService {
  protected final BookDao bookDao;

  @Override
  public Book get(Long id) {
    return bookDao.getBookById(id).orElseThrow(()
        -> new EntityNotFoundException("Книга [" + id + "] не найдена"));
  }

  @Override
  public Book add(Long id, Integer amount, LocalDateTime addTime) {
    if (amount < 0) {
      throw new IllegalArgumentException("Количество добавленных книг должно быть положительным");
    }
    return bookDao.add(id, amount, addTime);
  }

  @Override
  public Book writeOff(Long id, Integer amount, LocalDateTime addTime) {
    if (amount <= 0) {
      throw new IllegalArgumentException("Количество списываемых книг должно быть положительным");
    }
    return bookDao.writeOff(id, amount, addTime);
  }

  @Override
  public List<Book> getBooks(List<Long> bookIds) {
    return bookDao.getBooks(bookIds);
  }

  @Override
  public List<Book> getAllBooksById() {
    return bookDao.getAllBooks(BookSort.ID);
  }

  @Override
  public List<Book> getAllBooksByName() {
    return bookDao.getAllBooks(BookSort.NAME);
  }

  @Override
  public List<Book> getAllBooksByDate() {
    return bookDao.getAllBooks(BookSort.PUBLICATION_DATE);
  }

  @Override
  public List<Book> getAllBooksByPrice() {
    return bookDao.getAllBooks(BookSort.PRICE);
  }

  @Override
  public List<Book> getAllBooksByAvailable() {
    return bookDao.getAllBooks(BookSort.STATUS);
  }

  @Override
  public List<Book> getAllStaleBooksByDate() {
    return bookDao.getAllBooks(BookSort.STALE_BY_DATE);
  }

  @Override
  public List<Book> getAllStaleBooksByPrice() {
    return bookDao.getAllBooks(BookSort.STALE_BY_PRICE);
  }

  @Override
  public void importBook(Book book) {
    bookDao.importBook(book);
  }
}
