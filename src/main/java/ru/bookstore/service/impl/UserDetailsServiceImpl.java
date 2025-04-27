package ru.bookstore.service.impl;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.dao.UserDao;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.exceptions.UserAlreadyExistsException;
import ru.bookstore.model.Role;
import ru.bookstore.model.impl.User;
import ru.bookstore.service.MyUserDetailsService;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserDetailsServiceImpl implements MyUserDetailsService {
  protected final UserDao userDao;
  private final PasswordEncoder passwordEncoder;

  @Override
  @Transactional(readOnly = true)
  public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
    log.debug("Поиск пользователя по username: {}", username);
    User user = userDao.findByUsername(username)
        .orElseThrow(
            () -> new UsernameNotFoundException("Пользователь " + username + " не найден"));
    log.debug("Пользователь найден: {}", user);
    return org.springframework.security.core.userdetails.User.builder()
        .username(user.getUsername())
        .password(user.getPassword())
        .roles(user.getRole().name())
        .build();
  }

  @Transactional(readOnly = true)
  @Override
  public boolean existsByUsername(String username) {
    return userDao.findByUsername(username).isPresent();
  }

  @Transactional
  @Override
  public void save(User user) {
    userDao.save(user);
  }

  @Transactional
  @Override
  public User create(AuthDTO userInfo) {
    log.info("Регистрируем нового пользователя {}...", userInfo.getUsername());
    if (existsByUsername(userInfo.getUsername())) {
      throw new UserAlreadyExistsException("Пользователь " + userInfo.getUsername()
          + " уже существует");
    }
    User user = new User();
    user.setUsername(userInfo.getUsername());
    user.setPassword(passwordEncoder.encode(userInfo.getPassword()));
    user.setRole(Role.USER);

    save(user);
    log.info("Пользователь {} успешно зарегистрирован.", userInfo.getUsername());
    return user;
  }

  @Transactional
  @Override
  public boolean isUserValid(AuthDTO userInfo) {
    log.info("Проверяем логин и пароль пользователя {}...", userInfo.getUsername());
    UserDetails correctDetails = loadUserByUsername(userInfo.getUsername());
    if (passwordEncoder.matches(userInfo.getPassword(), correctDetails.getPassword())) {
      log.info("Пользователь ввёл корректные данные");
      return true;
    }
    log.info("Пользователь ввёл неверный пароль");
    return false;
  }

  @Transactional
  @Override
  public String getRole(String username) {
    log.info("Ищем роль пользователя {}...", username);
    String role = loadUserByUsername(username).getAuthorities().iterator().next().getAuthority();
    log.info("Роль для {} найдена: {}", username, role);
    return role;
  }
}
