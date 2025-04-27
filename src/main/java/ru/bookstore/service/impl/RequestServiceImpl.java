package ru.bookstore.service.impl;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bookstore.dao.RequestDao;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.service.RequestService;
import ru.bookstore.sorting.RequestSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestServiceImpl implements RequestService {
  private final RequestDao requestDao;

  @Override
  public Request getRequest(Long requestId) {
    return requestDao.getRequestById(requestId).orElseThrow(()
        -> new EntityNotFoundException("Запрос [" + requestId + "] не найден"));
  }

  @Override
  public Long addRequest(Book book, Integer amount) {
    return requestDao.addRequest(book, amount);
  }

  @Override
  public List<Request> getAllRequests() {
    return requestDao.getAllRequests(RequestSort.ID);
  }

  @Override
  public LinkedHashMap<Book, Long> getRequestsByCount() {
    return requestDao.getRequests(RequestSort.COUNT);
  }

  @Override
  public LinkedHashMap<Book, Long> getRequestsByPrice() {
    return requestDao.getRequests(RequestSort.PRICE);
  }

  @Override
  public Request importRequest(Request request) {
    log.info("Импорт запроса {}...", request.getId());
    Optional<Request> findRequest = requestDao.getRequestById(request.getId());
    if (findRequest.isPresent()) {
      throw new IllegalArgumentException("Ошибка при импорте: Запрос " + request.getId()
          + " уже есть в магазине");
    } else {
      return requestDao.importRequest(request);
    }
  }

  @Override
  public void closeRequests(Map<Long, Integer> books) {
    requestDao.closeRequests(books);
  }
}
