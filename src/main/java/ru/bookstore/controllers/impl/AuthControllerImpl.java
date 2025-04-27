package ru.bookstore.controllers.impl;

import jakarta.validation.Valid;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
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
public class AuthControllerImpl implements AuthController {
  private final MyUserDetailsService userDetailsService;

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

  @PostMapping("/register")
  @Override
  public ResponseEntity<?> register(@RequestBody @Valid AuthDTO request) {
    return ResponseEntity.ok(AuthMapper.INSTANCE.toDTO(userDetailsService.create(request)));
  }

//  @PostMapping("/logout")
//  public ResponseEntity<?> logout(HttpServletRequest request) {
//    String token = null;
//    String header = request.getHeader("Authorization");
//    if (header != null && header.startsWith("Bearer")) {
//      token = header.substring(7);
//    }
//    SecurityContextHolder.clearContext();
//    return ResponseEntity.ok().body(Map.of(
//        "message", "logout successful.",
//        "invalidated_token", token
//    ));
//  }
}
