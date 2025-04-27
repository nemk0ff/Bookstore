package ru.bookstore.dto.mappers;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.bookstore.dto.BookDTO;
import ru.bookstore.model.impl.Book;

@Mapper
public interface BookMapper {
  BookMapper INSTANCE = Mappers.getMapper(BookMapper.class);

  BookDTO toDTO(Book book);

  List<BookDTO> toListDTO(List<Book> books);
}
