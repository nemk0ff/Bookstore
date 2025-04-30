package ru.bookstore.controllers;

import io.jsonwebtoken.JwtException;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.ConstraintViolationException;
import java.time.Instant;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.InsufficientAuthenticationException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.context.request.ServletWebRequest;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;
import ru.bookstore.exceptions.DataAccessException;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.exceptions.ExportException;
import ru.bookstore.exceptions.ImportException;

@ControllerAdvice
@Slf4j
@Tag(name = "Обработка ошибок", description = "Глобальный обработчик исключений приложения")
public class RestResponseEntityExceptionHandler extends ResponseEntityExceptionHandler {

  @ExceptionHandler(EntityNotFoundException.class)
  @ResponseStatus(HttpStatus.NOT_FOUND)
  @ApiResponse(
      responseCode = "404",
      description = "Сущность не найдена",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class),
          examples = @ExampleObject(
              value = """
                {
                    "title": "Сущность не найдена",
                    "status": 404,
                    "detail": "Книга с id 123 не найдена",
                    "timestamp": "2023-05-20T12:34:56.789Z",
                    "path": "/books/123"
                }
                """
          )
      )
  )
  protected ResponseEntity<ProblemDetail> handleEntityNotFoundException(EntityNotFoundException ex, WebRequest request) {
    log.error("Сущность не найдена: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.NOT_FOUND, ex.getMessage());
    problemDetail.setTitle("Сущность не найдена");
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("path", ((ServletWebRequest) request).getRequest().getRequestURI());

    return new ResponseEntity<>(problemDetail, HttpStatus.NOT_FOUND);
  }

  @ExceptionHandler(DataAccessException.class)
  @ResponseStatus(HttpStatus.CONFLICT)
  @ApiResponse(
      responseCode = "409",
      description = "Конфликт при доступе к данным",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  protected ResponseEntity<ProblemDetail> handleDataAccessException(DataAccessException ex, WebRequest request) {
    log.error("Ошибка доступа к данным: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.CONFLICT, ex.getMessage());
    problemDetail.setTitle("Ошибка доступа к данным");
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, HttpStatus.CONFLICT);
  }

  @ExceptionHandler(IllegalArgumentException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400",
      description = "Некорректные входные параметры",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  protected ResponseEntity<ProblemDetail> handleIllegalArgumentException(IllegalArgumentException ex, WebRequest request) {
    log.error("Некорректный аргумент: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Некорректный аргумент");
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
  }

  @Override
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400",
      description = "Ошибка валидации входных данных",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class),
          examples = @ExampleObject(
              value = """
                {
                    "title": "Ошибка валидации",
                    "status": 400,
                    "detail": "Ошибка валидации",
                    "errors": {
                        "email": "должно иметь формат адреса электронной почты"                    },
                    "timestamp": "2023-05-20T12:34:56.789Z"
                }
                """
          )
      )
  )
  protected ResponseEntity<Object> handleMethodArgumentNotValid(
      MethodArgumentNotValidException ex, HttpHeaders headers,
      HttpStatusCode status, WebRequest request) {
    log.error("Ошибка валидации: {}", ex.getMessage(), ex);

    Map<String, String> errors = new HashMap<>();
    ex.getBindingResult().getFieldErrors().forEach(error ->
        errors.put(error.getField(), error.getDefaultMessage())
    );

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, "Ошибка валидации");
    problemDetail.setProperty("errors", errors);
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, headers, status);
  }

  @ExceptionHandler(ImportException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400",
      description = "Ошибка импорта данных",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  protected ResponseEntity<ProblemDetail> handleImportException(ImportException ex, WebRequest request) {
    log.error("Ошибка импорта: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.BAD_REQUEST, ex.getMessage());
    problemDetail.setTitle("Ошибка импорта");
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, HttpStatus.BAD_REQUEST);
  }

  @ExceptionHandler(ExportException.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ApiResponse(
      responseCode = "500",
      description = "Ошибка экспорта данных",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  protected ResponseEntity<ProblemDetail> handleExportException(ExportException ex, WebRequest request) {
    log.error("Ошибка экспорта: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, ex.getMessage());
    problemDetail.setTitle("Ошибка экспорта");
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(Exception.class)
  @ResponseStatus(HttpStatus.INTERNAL_SERVER_ERROR)
  @ApiResponse(
      responseCode = "500",
      description = "Внутренняя ошибка сервера",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class),
          examples = @ExampleObject(
              value = """
                {
                    "title": "Внутренняя ошибка",
                    "status": 500,
                    "detail": "Внутренняя ошибка сервера",
                    "timestamp": "2023-05-20T12:34:56.789Z"
                }
                """
          )
      )
  )
  protected ResponseEntity<ProblemDetail> handleGenericException(Exception ex, WebRequest request) {
    log.error("Непредвиденная ошибка: {}", ex.getMessage(), ex);

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(HttpStatus.INTERNAL_SERVER_ERROR, "Внутренняя ошибка сервера");
    problemDetail.setTitle("Внутренняя ошибка");
    problemDetail.setProperty("timestamp", Instant.now());

    return new ResponseEntity<>(problemDetail, HttpStatus.INTERNAL_SERVER_ERROR);
  }

  @ExceptionHandler(ConstraintViolationException.class)
  @ResponseStatus(HttpStatus.BAD_REQUEST)
  @ApiResponse(
      responseCode = "400",
      description = "Нарушение ограничений валидации",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = Map.class),
          examples = @ExampleObject(
              value = """
                {
                    "timestamp": "2023-05-20T12:34:56.789Z",
                    "status": 400,
                    "error": "Bad Request",
                    "message": "должно быть не null",
                    "path": "/api/users"
                }
                """
          )
      )
  )
  public Map<String, Object> handleConstraintViolationException(HttpServletRequest request, Exception ex) {
    Map<String, Object> errorResponse = new HashMap<>();

    errorResponse.put("timestamp", new Date());
    errorResponse.put("status", HttpStatus.BAD_REQUEST.value());
    errorResponse.put("error", HttpStatus.BAD_REQUEST.getReasonPhrase());
    errorResponse.put("message", ex.getMessage());
    errorResponse.put("path", request.getServletPath());

    log.error(ex.getMessage(), ex);

    return errorResponse;
  }

  @ExceptionHandler(AccessDeniedException.class)
  @ResponseStatus(HttpStatus.FORBIDDEN)
  @ApiResponse(
      responseCode = "403",
      description = "Доступ запрещен",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class)
      )
  )
  protected ResponseEntity<ProblemDetail> handleAccessDeniedException(
      AccessDeniedException ex, WebRequest request) {

    log.warn("Доступ запрещен: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.FORBIDDEN, "Недостаточно прав для выполнения операции: " + ex.getMessage());
    problemDetail.setTitle("Доступ запрещен");
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("path", ((ServletWebRequest) request).getRequest().getRequestURI());

    return new ResponseEntity<>(problemDetail, HttpStatus.FORBIDDEN);
  }

  @ExceptionHandler({
      JwtException.class,
      AuthenticationException.class,
      BadCredentialsException.class,
      InsufficientAuthenticationException.class
  })
  @ResponseStatus(HttpStatus.UNAUTHORIZED)
  @ApiResponse(
      responseCode = "401",
      description = "Ошибка аутентификации",
      content = @Content(
          mediaType = "application/json",
          schema = @Schema(implementation = ProblemDetail.class),
          examples = @ExampleObject(
              value = """
                {
                    "title": "Ошибка аутентификации",
                    "status": 401,
                    "detail": "Требуется авторизация: Неверные учетные данные",
                    "timestamp": "2023-05-20T12:34:56.789Z",
                    "path": "/api/auth/login"
                }
                """
          )
      )
  )
  protected ResponseEntity<ProblemDetail> handleAuthenticationException(
      RuntimeException ex, WebRequest request) {

    log.warn("Ошибка аутентификации: {}", ex.getMessage());

    ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(
        HttpStatus.UNAUTHORIZED, "Требуется авторизация: " + ex.getMessage());
    problemDetail.setTitle("Ошибка аутентификации");
    problemDetail.setProperty("timestamp", Instant.now());
    problemDetail.setProperty("path", ((ServletWebRequest) request).getRequest().getRequestURI());

    return new ResponseEntity<>(problemDetail, HttpStatus.UNAUTHORIZED);
  }
}
