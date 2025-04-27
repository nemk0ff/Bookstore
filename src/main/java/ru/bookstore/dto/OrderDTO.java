package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
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

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OrderDTO {
  private Long id;
  private OrderStatus status;
  private Double price;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime orderDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime completeDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private String clientName;
  @NotEmpty
  private Map<@Min(value = 1, message = "Id must be greater than 0") Long,
      @Positive(message = "Amount must be positive") Integer> books;
}
