package ru.bookstore.facade.impl;

import java.util.LinkedHashMap;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.facade.RequestFacade;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.service.BookService;
import ru.bookstore.service.RequestService;
import ru.bookstore.sorting.RequestSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class RequestFacadeImpl implements RequestFacade {
  private final BookService bookService;
  private final RequestService requestService;

  @Transactional
  @Override
  public Long add(Long bookId, Integer amount) {
    return requestService.addRequest(bookService.get(bookId), amount);
  }

  @Transactional(readOnly = true)
  @Override
  public Request get(Long id) {
    return requestService.getRequest(id);
  }

  @Transactional(readOnly = true)
  @Override
  public LinkedHashMap<Book, Long> getRequests(RequestSort requestSort) {
    if (requestSort == RequestSort.COUNT) {
      return requestService.getRequestsByCount();
    }
    return requestService.getRequestsByPrice();
  }

  @Transactional(readOnly = true)
  @Override
  public List<Request> getAllRequests() {
    return requestService.getAllRequests();
  }

  @Transactional
  @Override
  public Request importRequest(Request request) {
    return requestService.importRequest(request);
  }
}
