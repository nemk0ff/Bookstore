package ru.bookstore.controllers;

import org.springframework.http.ResponseEntity;
import ru.bookstore.sorting.RequestSort;

public interface RequestsController {
  ResponseEntity<?> createRequest(Long bookId, Integer bookAmount);

  ResponseEntity<?> getRequests(RequestSort requestSort);

  ResponseEntity<?> getAllRequests();

  ResponseEntity<?> exportRequest(Long id);

  ResponseEntity<?> importRequest(Long id);

  ResponseEntity<?> importAll();

  ResponseEntity<?> exportAll();
}
