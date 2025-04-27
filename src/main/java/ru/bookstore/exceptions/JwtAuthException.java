package ru.bookstore.exceptions;

import io.jsonwebtoken.JwtException;

public class JwtAuthException extends JwtException {

  public JwtAuthException(String message) {
    super(message);
  }

  public JwtAuthException(String message, Throwable cause) {
    super(message, cause);
  }
}
