package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bookstore.model.OrderStatus;

@Schema(description = "Сущность заказа")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
  @Schema(description = "Уникальный идентификатор заказа", example = "17")
  private Long id;

  @Schema(description = "Статус заказа", allowableValues = {"NEW", "COMPLETED", "CANCELED"})
  private OrderStatus status;

  @Schema(description = "Общая стоимость заказа", example = "1500.50")
  private Double price;

  @Schema(description = "Дата создания заказа", example = "10:30:45 15-05-2023")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime orderDate;

  @Schema(description = "Дата завершения заказа", example = "14:20:10 16-05-2023")
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime completeDate;

  @Schema(description = "Имя клиента", example = "ivanov@bookstore.ru")
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private String clientName;

  @Schema(description = "Список книг в заказе (ID книги -> количество)",
      example = "{\"1\": 2, \"3\": 1}")
  @NotEmpty
  private Map<@Min(value = 1, message = "Id must be greater than 0") Long,
      @Positive(message = "Amount must be positive") Integer> books;
}
