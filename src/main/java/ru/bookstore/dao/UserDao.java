package ru.bookstore.dao;

import java.util.Optional;
import ru.bookstore.model.impl.User;

public interface UserDao {
  Optional<User> findByUsername(String username);

  void save(User user);
}
