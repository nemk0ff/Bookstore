package ru.bookstore.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class AuthDTO {
  @NotBlank(message = "Имя пользователя не может быть пустым")
  private String username;
  @NotBlank(message = "Пароль не может быть пустым")
  private String password;
}