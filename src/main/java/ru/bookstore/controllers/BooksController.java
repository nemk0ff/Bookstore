package ru.bookstore.controllers;

import org.springframework.http.ResponseEntity;
import ru.bookstore.sorting.BookSort;

public interface BooksController {
  ResponseEntity<?> addBook(Long id, Integer amount);

  ResponseEntity<?> writeOff(Long id, Integer amount);

  ResponseEntity<?> showBookDetails(Long id);

  ResponseEntity<?> getBooks(BookSort bookSort);

  ResponseEntity<?> getStaleBooks(BookSort bookSort);

  ResponseEntity<?> importAll();

  ResponseEntity<?> exportAll();

  ResponseEntity<?> importBook(Long id);

  ResponseEntity<?> exportBook(Long id);
}
