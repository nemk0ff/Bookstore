package ru.bookstore.controllers.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.controllers.AuthController;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.dto.mappers.AuthMapper;
import ru.bookstore.security.JwtUtils;
import ru.bookstore.service.MyUserDetailsService;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/auth")
@Tag(name = "Аутентификация", description = "API для входа и регистрации пользователей в системе")
public class AuthControllerImpl implements AuthController {
  private final MyUserDetailsService userDetailsService;

  @Operation(
      summary = "Авторизация пользователя",
      description = "Позволяет пользователю авторизоваться в системе и получить JWT токен",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Данные для авторизации",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AuthDTO.class),
              examples = @ExampleObject(
                  value = "{\"username\": \"user123\", \"password\": \"password123\"}"
              )
          )
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Успешная авторизация",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(
                      type = "object",
                      description = "Ответ с токеном",
                      example = "{\"token\": \"eyJhbGci...\", \"role\": \"USER\"}"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "401",
              description = "Неверные учетные данные",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(
                      type = "object",
                      description = "Сообщение об ошибке",
                      example = "{\"error\": \"Неверный логин или пароль\"}"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные данные запроса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PostMapping("/login")
  @Override
  public ResponseEntity<?> login(@RequestBody @Valid AuthDTO request) {
    if (userDetailsService.isUserValid(request)) {
      String role = userDetailsService.getRole(request.getUsername());
      String token = JwtUtils.generateToken(request.getUsername(), role);
      return ResponseEntity.ok(Map.of("token", token, "role", role));
    }
    return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
        .body(Map.of("error", "Неверный логин или пароль"));
  }

  @Operation(
      summary = "Регистрация пользователя",
      description = "Создает нового пользователя в системе",
      requestBody = @io.swagger.v3.oas.annotations.parameters.RequestBody(
          description = "Данные для регистрации",
          required = true,
          content = @Content(
              mediaType = "application/json",
              schema = @Schema(implementation = AuthDTO.class),
              examples = @ExampleObject(
                  value = "{\"username\": \"newUser\", \"password\": \"securePassword123\"}"
              )
          )
      ),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Пользователь успешно зарегистрирован",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = AuthDTO.class),
                  examples = @ExampleObject(
                      value = "{\"username\": \"newUser\", \"password\": \"*****\"}"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные данные пользователя",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class),
                  examples = @ExampleObject(
                      value = "{\"title\": \"Ошибка валидации\", \"status\": 400, \"detail\": \"Имя пользователя уже занято\"}"
                  )
              )
          ),
          @ApiResponse(
              responseCode = "409",
              description = "Пользователь уже существует",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PostMapping("/register")
  @Override
  public ResponseEntity<?> register(@RequestBody @Valid AuthDTO request) {
    return ResponseEntity.ok(AuthMapper.INSTANCE.toDTO(userDetailsService.create(request)));
  }
}
