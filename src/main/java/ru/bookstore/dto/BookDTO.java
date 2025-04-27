package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.constraints.Positive;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Data;
import ru.bookstore.model.BookStatus;

@Builder
@Data
public class BookDTO {
  @Positive(message = "id must be positive")
  private Long id;
  private String name;
  private String author;
  private Integer publicationDate;
  @Positive(message = "amount must be positive")
  private Integer amount;
  private Double price;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime lastDeliveredDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime lastSaleDate;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private BookStatus status;
}