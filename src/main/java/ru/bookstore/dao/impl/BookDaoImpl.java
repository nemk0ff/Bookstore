package ru.bookstore.dao.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.hibernate.query.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Repository;
import ru.bookstore.dao.BookDao;
import ru.bookstore.exceptions.DataAccessException;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.exceptions.ImportException;
import ru.bookstore.model.impl.Book;
import ru.bookstore.sorting.BookSort;

@Repository
@EqualsAndHashCode(callSuper = true)
@Slf4j
@Data
public class BookDaoImpl extends HibernateAbstractDao<Book> implements BookDao {
  @Value("${book.stale.months}")
  private int staleBookMonths;

  @Autowired
  public BookDaoImpl(SessionFactory sessionFactory) {
    super(Book.class);
    this.sessionFactory = sessionFactory;
    log.debug("BookDaoImpl инициализирован, staleBookMonths = {}", staleBookMonths);
  }

  @Override
  public Book add(long bookId, int amount, LocalDateTime deliveredDate) {
    log.debug("Добавляем {} книг [{}]...", amount, bookId);
    try {
      Book book = sessionFactory.getCurrentSession().get(Book.class, bookId);
      if (book == null) {
        throw new EntityNotFoundException("Такой книги нет в магазине");
      }
      book.setAmount(book.getAmount() + amount);
      book.setLastDeliveredDate(deliveredDate);

      book = sessionFactory.getCurrentSession().merge(book);
      log.info("Успешно добавлено {} книг [{}], дата поставки: {}", amount, bookId, deliveredDate);
      return book;
    } catch (Exception e) {
      throw new DataAccessException("Не удалось добавить книги [" + bookId + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public Book writeOff(long bookId, int amount, LocalDateTime saleDate)
      throws IllegalArgumentException {
    log.info("Списываем {} книг [{}]...", amount, bookId);
    try {
      Book book = sessionFactory.getCurrentSession().get(Book.class, bookId);
      if (book == null) {
        throw new IllegalArgumentException("Попытка списать книги с несуществующим id: " + bookId);
      }
      if (book.getAmount() < amount) {
        throw new IllegalArgumentException("Попытка списать " + amount + " книг [" + bookId + "], но доступно только " + book.getAmount());
      }
      book.setAmount(book.getAmount() - amount);
      book.setLastSaleDate(saleDate);

      book = sessionFactory.getCurrentSession().merge(book);
      log.info("Списано {} книг [{}], дата продажи: {}", amount, bookId, saleDate);
      return book;
    } catch (Exception e) {
      throw new DataAccessException("Не удалось списать книги [" + bookId + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public List<Book> getAllBooks(BookSort sortType) {
    log.debug("Получаем все книги, отсортированные по {}...", sortType);
    try {
      Query<Book> query = sessionFactory.getCurrentSession().createQuery(getQuery(sortType), Book.class);
      if (sortType == BookSort.STALE_BY_DATE || sortType == BookSort.STALE_BY_PRICE) {
        query.setParameter("staleBookMonths", staleBookMonths);
      }
      List<Book> books = query.list();
      log.info("Успешно получено {} книг, отсортированных по {}", books.size(), sortType);
      return books;
    } catch (Exception e) {
      throw new DataAccessException("Ошибка при получении всех книг: " + e.getMessage(), e);
    }
  }

  private String getQuery(BookSort sortType) {
    return switch (sortType) {
      case ID -> "FROM Book ORDER BY id";
      case NAME -> "FROM Book ORDER BY name";
      case PUBLICATION_DATE -> "FROM Book ORDER BY publicationDate";
      case PRICE -> "FROM Book ORDER BY price";
      case STATUS -> "FROM Book ORDER BY status";
      case STALE_BY_DATE -> "FROM Book WHERE "
          + "(lastSaleDate IS NULL AND DATEDIFF(CURRENT_DATE, lastDeliveredDate) >= "
          + ":staleBookMonths) OR (lastSaleDate IS NOT NULL AND DATEDIFF(CURRENT_DATE, "
          + "lastSaleDate) >= :staleBookMonths) ORDER BY lastDeliveredDate";
      case STALE_BY_PRICE -> "FROM Book WHERE "
          + "(lastSaleDate IS NULL AND DATEDIFF(CURRENT_DATE, lastDeliveredDate) >= " +
          ":staleBookMonths) OR (lastSaleDate IS NOT NULL AND DATEDIFF(CURRENT_DATE,"
          + "lastSaleDate) >= :staleBookMonths) ORDER BY price";
    };
  }

  @Override
  public List<Book> getBooks(List<Long> bookIds) {
    log.debug("Получаем книги [{}]...", bookIds);
    try {
      List<Book> books = sessionFactory.getCurrentSession()
          .createQuery("FROM Book WHERE id IN (:bookIds)", Book.class)
          .setParameterList("bookIds", bookIds)
          .list();
      if (books.size() != bookIds.size()) {
        throw new IllegalArgumentException("В списке есть несуществующие книги: " + bookIds);
      }
      log.debug("Успешно получено {} книг [{}]", books.size(), bookIds);
      return books;
    } catch (Exception e) {
      throw new DataAccessException("Ошибка при получении книг ["
          + bookIds + "]: " + e.getMessage(), e);
    }
  }

  @Override
  public void importBook(Book book) {
    log.info("Импортируем книгу: {}...", book);
    try {
      sessionFactory.getCurrentSession().merge(book);
      log.info("Книга успешно импортирована: {}", book);
    } catch (Exception e) {
      throw new ImportException("Не удалось импортировать книгу: " + book, e);
    }
  }

  @Override
  public Optional<Book> getBookById(long bookId) {
    return find(bookId);
  }
}
