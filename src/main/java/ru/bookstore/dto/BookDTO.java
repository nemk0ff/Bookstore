package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.bookstore.model.BookStatus;

@Schema(description = "Сущность книги", accessMode = Schema.AccessMode.READ_ONLY)
@Builder
@Data
public class BookDTO {
  @Schema(description = "Уникальный идентификатор книги", example = "1245")
  @Positive(message = "id must be positive")
  private Long id;

  @Schema(description = "Название книги", example = "Анна Каренина")
  private String name;

  @Schema(description = "Автор книги", example = "А.С.Пушкин")
  private String author;

  @Schema(description = "Год издания книги", example = "1992")
  private Integer publicationDate;

  @Schema(description = "Количество книг на складе", example = "10")
  @Positive(message = "amount must be positive")
  private Integer amount;

  @Schema(description = "Стоимость книги", example = "750")
  private Double price;

  @Schema(description = "Дата последней поставки книги на склад", example = "2025-01-01")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime lastDeliveredDate;

  @Schema(description = "Дата последней продажи книги", example = "2025-01-01")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime lastSaleDate;

  @Schema(description = "Статус книги", allowableValues = {"AVAILABLE", "NOT_AVAILABLE"})
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BookStatus status;
}