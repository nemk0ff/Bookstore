package ru.bookstore.service;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;

public interface RequestService {
  Request getRequest(Long requestId);

  Long addRequest(Book book, Integer amount);

  List<Request> getAllRequests();

  LinkedHashMap<Book, Long> getRequestsByCount();

  LinkedHashMap<Book, Long> getRequestsByPrice();

  Request importRequest(Request request);

  void closeRequests(Map<Long, Integer> books);
}
