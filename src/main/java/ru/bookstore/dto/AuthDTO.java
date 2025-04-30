package ru.bookstore.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Schema(description = "Данные аутентификации пользователя", requiredMode = Schema.RequiredMode.REQUIRED)
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthDTO {
  @Schema(description = "Имя пользователя", example = "ivanov@bookstore.ru")
  @NotBlank(message = "Имя пользователя не может быть пустым")
  private String username;

  @Schema(description = "Пароль пользователя", example = "password123")
  @NotBlank(message = "Пароль не может быть пустым")
  private String password;
}