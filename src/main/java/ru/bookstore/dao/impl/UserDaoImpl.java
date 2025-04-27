package ru.bookstore.dao.impl;

import java.util.Optional;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.HibernateException;
import org.hibernate.SessionFactory;
import org.springframework.stereotype.Repository;
import ru.bookstore.dao.UserDao;
import ru.bookstore.exceptions.DataAccessException;
import ru.bookstore.model.impl.User;

@Repository
@Slf4j
public class UserDaoImpl extends HibernateAbstractDao<User> implements UserDao {

  public UserDaoImpl(SessionFactory sessionFactory) {
    super(User.class);
    this.sessionFactory = sessionFactory;
    log.debug("UserDao инициализирован");
  }

  @Override
  public Optional<User> findByUsername(String username) {
    try {
      return Optional.ofNullable(
          sessionFactory.getCurrentSession()
              .createQuery("FROM User WHERE username = :username", User.class)
              .setParameter("username", username)
              .uniqueResult()
      );
    } catch (HibernateException e) {
      throw new DataAccessException("Ошибка при поиске пользователя с username " + username, e);
    }
  }

  @Override
  public void save(User user) {
    log.debug("Сохранение пользователя: {}", user.getUsername());
    try {
      sessionFactory.getCurrentSession().persist(user);
    } catch (HibernateException e) {
      throw new DataAccessException("Ошибка сохранения пользователя " + user.getUsername(), e);
    }
  }
}