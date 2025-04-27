package ru.bookstore.dto;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.bookstore.model.RequestStatus;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RequestDTO {
  private Long id;
  private Long bookId;
  private Integer amount;
  @JsonFormat(shape = JsonFormat.Shape.STRING)
  private RequestStatus status;
}
