package ru.bookstore.dto.mappers;

import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.bookstore.dto.AuthDTO;
import ru.bookstore.model.impl.User;

@Mapper
public interface AuthMapper {
  AuthMapper INSTANCE = Mappers.getMapper(AuthMapper.class);

  AuthDTO toDTO(User user);
}
