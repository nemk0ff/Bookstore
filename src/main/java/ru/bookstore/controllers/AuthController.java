package ru.bookstore.controllers;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.dto.AuthDTO;

@RestController
public interface AuthController {
  ResponseEntity<?> login(AuthDTO request);

  ResponseEntity<?> register(AuthDTO request);
}