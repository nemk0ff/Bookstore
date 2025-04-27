package ru.bookstore.service;

import org.springframework.security.core.userdetails.UserDetailsService;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.model.impl.User;

public interface MyUserDetailsService extends UserDetailsService {
  boolean existsByUsername(String username);

  void save(User user);

  User create(AuthDTO userInfo);

  boolean isUserValid(AuthDTO userInfo);

  String getRole(String username);
}
