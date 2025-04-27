package ru.bookstore.dao.impl;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.bookstore.dao.RequestDao;
import ru.bookstore.exceptions.DataAccessException;
import ru.bookstore.exceptions.ImportException;
import ru.bookstore.model.RequestStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.sorting.RequestSort;

@Repository
@Slf4j
public class RequestDaoImpl extends HibernateAbstractDao<Request> implements RequestDao {
  public RequestDaoImpl(SessionFactory sessionFactory) {
    super(Request.class);
    this.sessionFactory = sessionFactory;
    log.debug("RequestDaoImpl инициализирован");
  }

  @Override
  public List<Request> getAllRequests(RequestSort typeSort) {
    log.info("Получаем все запросы, отсортированные по {}...", typeSort);
    try {
      List<Request> requests = sessionFactory.getCurrentSession()
          .createQuery("FROM Request ORDER BY id", Request.class)
          .getResultList();
      log.info("Успешно получено {} запросов", requests.size());
      return requests;
    } catch (Exception e) {
      throw new DataAccessException("Ошибка при получении всех запросов: " + e.getMessage(), e);
    }
  }

  @Override
  public LinkedHashMap<Book, Long> getRequests(RequestSort typeSort) {
    log.debug("Получаем запросы, отсортированные по {}...", typeSort);

    if (typeSort == RequestSort.COUNT) {
      return getRequestsSortedByCount();
    } else if (typeSort == RequestSort.PRICE) {
      return getRequestsSortedByPrice();
    } else {
      throw new IllegalArgumentException("Неизвестный тип сортировки: " + typeSort);
    }
  }

  private LinkedHashMap<Book, Long> getRequestsSortedByCount() {
    String hql = "SELECT r.book, COUNT(r) FROM Request r "
        + "WHERE r.status = 'OPEN' GROUP BY r.book ORDER BY COUNT(r)";

    List<Object[]> resultList = sessionFactory.getCurrentSession().createQuery(hql, Object[].class)
        .getResultList();
    return getBookLongLinkedHashMap(resultList);
  }

  private LinkedHashMap<Book, Long> getRequestsSortedByPrice() {
    Map<Book, Double> bookPrices = getBookPrices();

    String hql = "SELECT r.book, COUNT(r) FROM Request r WHERE r.status = 'OPEN' GROUP BY r.book";
    List<Object[]> resultList = sessionFactory.getCurrentSession().createQuery(hql, Object[].class)
        .getResultList();

    List<Object[]> sortedResultList = resultList.stream()
        .sorted(Comparator.comparingDouble(
            result -> bookPrices.getOrDefault((Book) result[0], 0.0)))
        .toList();

    return getBookLongLinkedHashMap(sortedResultList);
  }

  private LinkedHashMap<Book, Long> getBookLongLinkedHashMap(List<Object[]> resultList) {
    LinkedHashMap<Book, Long> requests = new LinkedHashMap<>();

    for (Object[] result : resultList) {
      Book book = (Book) result[0];
      Long count = ((Number) result[1]).longValue();
      requests.put(book, count);
    }

    return requests;
  }

  private Map<Book, Double> getBookPrices() {
    String hql = "SELECT b.id, b.price FROM Book b";
    List<Object[]> resultList = sessionFactory.getCurrentSession().createQuery(hql, Object[].class)
        .getResultList();

    return resultList.stream()
        .collect(Collectors.toMap(
            result -> sessionFactory.getCurrentSession().find(Book.class, result[0]),
            result -> (Double) result[1]
        ));
  }

  @Override
  public Optional<Request> getRequestById(long requestId) {
    return find(requestId);
  }

  @Override
  public long addRequest(Book book, int amount) {
    log.debug("Добавляем запрос для bookId={} и amount={}", book.getId(), amount);
    try {
      Request request = new Request();
      request.setBook(book);
      request.setAmount(amount);
      request.setStatus(RequestStatus.OPEN);

      sessionFactory.getCurrentSession().persist(request);
      return request.getId();
    } catch (Exception e) {
      throw new DataAccessException("Не удалось добавить запрос для bookId=" + book.getId()
          + " и amount=" + amount + " :" + e.getMessage(), e);
    }
  }

  @Override
  public Request importRequest(Request request) {
    log.debug("Импортируем запрос: {}...", request);
    try {
      Request importedRequest = update(request);
      log.debug("Запрос успешно импортирован: {}", request);
      return importedRequest;
    } catch (Exception e) {
      throw new ImportException("Не удалось импортировать запрос: " + request, e);
    }
  }

  @Override
  public void closeRequests(Map<Long, Integer> books) {
    log.info("Закрываем запросы для книг: {}...", books);
    try {
      for (Map.Entry<Long, Integer> bookEntry : books.entrySet()) {
        Long bookId = bookEntry.getKey();
        Integer amount = bookEntry.getValue();
        Book book = sessionFactory.getCurrentSession().find(Book.class, bookId);
        if (book == null) {
          log.warn("Книга [{}] не найдена. Пропускаем закрытие запроса.", bookId);
          continue;
        }
        Request request = sessionFactory.getCurrentSession().createQuery("FROM Request WHERE book = :book " +
                "AND amount = :amount", Request.class)
            .setParameter("book", book)
            .setParameter("amount", amount)
            .setMaxResults(1)
            .uniqueResult();
        if (request != null) {
          request.setStatus(RequestStatus.CLOSED);
          sessionFactory.getCurrentSession().persist(request);
          log.debug("Закрываем запрос [{}] для bookId={} и amount={}",
              request.getId(), bookId, amount);
        }
      }
      log.info("Запросы для книг {} успешно закрыты.", books);
    } catch (Exception e) {
      throw new DataAccessException("Не удалось закрыть запросы для книг "
          + books + ": " + e.getMessage(), e);
    }
  }
}
