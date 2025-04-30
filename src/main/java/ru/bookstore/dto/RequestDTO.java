package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bookstore.model.RequestStatus;

@Schema(description = "Сущность запроса на книгу")
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
  @Schema(description = "Уникальный идентификатор запроса", example = "1")
  private Long id;

  @Schema(description = "ID запрашиваемой книги", example = "5")
  private Long bookId;

  @Schema(description = "Запрашиваемое количество книг", example = "10")
  private Integer amount;

  @Schema(description = "Статус запроса", allowableValues = {"OPEN", "CLOSED"})
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private RequestStatus status;
}
