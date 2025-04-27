package ru.bookstore.dto.mappers;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.factory.Mappers;
import ru.bookstore.dto.RequestDTO;
import ru.bookstore.model.impl.Request;

@Mapper
public interface RequestMapper {
  RequestMapper INSTANCE = Mappers.getMapper(RequestMapper.class);

  @Mapping(source = "book.id", target = "bookId")
  RequestDTO toDTO(Request request);

  List<RequestDTO> toListDTO(List<Request> requests);
}
