package ru.bookstore.facade;

import java.util.LinkedHashMap;
import java.util.List;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.sorting.RequestSort;

public interface RequestFacade {
  Long add(Long bookId, Integer amount);

  Request get(Long id);

  LinkedHashMap<Book, Long> getRequests(RequestSort requestSort);

  List<Request> getAllRequests();

  Request importRequest(Request request);
}
