package ru.bookstore.exceptions;

public class ExportException extends RuntimeException {
  public ExportException(String message, Throwable cause) {
    super(message, cause);
  }
}
