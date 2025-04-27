package ru.bookstore.dao;

import java.util.Optional;
import ru.bookstore.model.Item;

public interface GenericDao<T extends Item> {
  T update(T entity);

  Optional<T> find(Long id);
}
