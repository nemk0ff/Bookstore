package services;

import java.util.Optional;
import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import ru.bookstore.dao.UserDao;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.exceptions.UserAlreadyExistsException;
import ru.bookstore.model.Role;
import ru.bookstore.model.impl.User;
import ru.bookstore.service.impl.UserDetailsServiceImpl;

@ExtendWith(MockitoExtension.class)
class UserDetailsServiceImplTest {
  @Mock
  private UserDao userDao;
  @Mock
  private PasswordEncoder passwordEncoder;
  @InjectMocks
  private UserDetailsServiceImpl userDetailsService;

  private final String testUsername = "testUser";
  private final String testPassword = "testPass";
  private final User testUser = new User(1L, testUsername, "userPassword", Role.USER);
  private final AuthDTO testAuthDTO = new AuthDTO(testUsername, testPassword);

  @Nested
  class LoadUserByUsernameTests {
    @Test
    void loadUserByUsername_shouldReturnUserDetailsWhenUserExists() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      UserDetails result = userDetailsService.loadUserByUsername(testUsername);

      assertThat(result.getUsername()).isEqualTo(testUsername);
      assertThat(result.getPassword()).isEqualTo(testUser.getPassword());
      assertThat(result.getAuthorities())
          .extracting("authority")
          .containsExactly("ROLE_" + testUser.getRole().name());
      verify(userDao).findByUsername(testUsername);
    }

    @Test
    void loadUserByUsername_shouldThrowExceptionWhenUserNotFound() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.empty());

      assertThrows(UsernameNotFoundException.class,
          () -> userDetailsService.loadUserByUsername(testUsername));
      verify(userDao).findByUsername(testUsername);
    }
  }

  @Nested
  class ExistenceCheckTests {
    @Test
    void existsByUsername_shouldReturnTrueWhenUserExists() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      boolean result = userDetailsService.existsByUsername(testUsername);

      assertTrue(result);
      verify(userDao).findByUsername(testUsername);
    }

    @Test
    void existsByUsername_shouldReturnFalseWhenUserNotExists() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.empty());

      boolean result = userDetailsService.existsByUsername(testUsername);

      assertFalse(result);
      verify(userDao).findByUsername(testUsername);
    }
  }

  @Nested
  class UserCreationTests {
    @Test
    void save_shouldCallDaoSave() {
      userDetailsService.save(testUser);

      verify(userDao).save(testUser);
    }

    @Test
    void create_shouldCreateNewUserWhenNotExists() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.empty());
      when(passwordEncoder.encode(testPassword)).thenReturn("userPassword");

      doAnswer(invocation -> {
        User user = invocation.getArgument(0);
        assertThat(user.getUsername()).isEqualTo(testUsername);
        assertThat(user.getPassword()).isEqualTo("userPassword");
        assertThat(user.getRole()).isEqualTo(Role.USER);
        return null;
      }).when(userDao).save(any(User.class));

      User result = userDetailsService.create(testAuthDTO);

      assertThat(result.getUsername()).isEqualTo(testUsername);
      assertThat(result.getPassword()).isEqualTo("userPassword");
      assertThat(result.getRole()).isEqualTo(Role.USER);
      verify(userDao).save(any(User.class));
    }

    @Test
    void create_shouldThrowExceptionWhenUserExists() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      assertThrows(UserAlreadyExistsException.class,
          () -> userDetailsService.create(testAuthDTO));
      verify(userDao).findByUsername(testUsername);
    }
  }

  @Nested
  class AuthenticationTests {
    @Test
    void isUserValid_shouldReturnTrueWhenCredentialsMatch() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(testPassword, testUser.getPassword())).thenReturn(true);

      boolean result = userDetailsService.isUserValid(testAuthDTO);

      assertTrue(result);
      verify(passwordEncoder).matches(testPassword, testUser.getPassword());
    }

    @Test
    void isUserValid_shouldReturnFalseWhenPasswordNotMatch() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));
      when(passwordEncoder.matches(testPassword, testUser.getPassword())).thenReturn(false);

      boolean result = userDetailsService.isUserValid(testAuthDTO);

      assertFalse(result);
      verify(passwordEncoder).matches(testPassword, testUser.getPassword());
    }
  }

  @Nested
  class RoleCheckTests {
    @Test
    void getRole_shouldReturnUserRole() {
      when(userDao.findByUsername(testUsername)).thenReturn(Optional.of(testUser));

      String result = userDetailsService.getRole(testUsername);

      assertThat(result).isEqualTo("ROLE_USER");
      verify(userDao).findByUsername(testUsername);
    }
  }
}