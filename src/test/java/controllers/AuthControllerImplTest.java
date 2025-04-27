package controllers;

import static org.assertj.core.api.Assertions.assertThat;
import org.assertj.core.api.InstanceOfAssertFactories;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.Nested;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import ru.bookstore.controllers.impl.AuthControllerImpl;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.exceptions.UserAlreadyExistsException;
import ru.bookstore.model.Role;
import ru.bookstore.model.impl.User;
import ru.bookstore.service.MyUserDetailsService;

@ExtendWith(MockitoExtension.class)
class AuthControllerImplTest {
  @Mock
  private MyUserDetailsService userDetailsService;

  @InjectMocks
  private AuthControllerImpl authController;

  private final String testUsername = "testUser";
  private final String testPassword = "testPass";
  private final AuthDTO testAuthDTO = new AuthDTO(testUsername, testPassword);
  private final User testUser = new User(1L, testUsername, "encodedPass", Role.USER);

  @Nested
  class LoginTests {
    @Test
    void login_shouldReturnTokenWhenCredentialsValid() {
      when(userDetailsService.isUserValid(testAuthDTO)).thenReturn(true);
      when(userDetailsService.getRole(testUsername)).thenReturn("ROLE_USER");

      ResponseEntity<?> response = authController.login(testAuthDTO);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody())
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .containsOnlyKeys("token", "role")
          .containsEntry("role", "ROLE_USER")
          .hasEntrySatisfying("token", token -> assertThat(token).isNotNull());
    }

    @Test
    void login_shouldReturnUnauthorizedWhenCredentialsInvalid() {
      when(userDetailsService.isUserValid(testAuthDTO)).thenReturn(false);

      ResponseEntity<?> response = authController.login(testAuthDTO);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
      assertThat(response.getBody())
          .asInstanceOf(InstanceOfAssertFactories.MAP)
          .containsOnlyKeys("error")
          .containsEntry("error", "Неверный логин или пароль");
    }
  }

  @Nested
  class RegisterTests {
    @Test
    void register_shouldCreateNewUserAndReturnOk() {
      when(userDetailsService.create(testAuthDTO)).thenReturn(testUser);

      ResponseEntity<?> response = authController.register(testAuthDTO);

      assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
      assertThat(response.getBody()).isNotNull();
      verify(userDetailsService).create(testAuthDTO);
    }

    @Test
    void register_shouldThrowExceptionWhenUserExists() {
      when(userDetailsService.create(testAuthDTO))
          .thenThrow(new UserAlreadyExistsException("Пользователь уже существует"));

      UserAlreadyExistsException exception = assertThrows(
          UserAlreadyExistsException.class,
          () -> authController.register(testAuthDTO)
      );

      assertThat(exception.getMessage()).isEqualTo("Пользователь уже существует");
      verify(userDetailsService).create(testAuthDTO);
    }
  }
}
