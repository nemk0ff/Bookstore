package ru.bookstore.dto.mappers;

import java.util.List;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;
import ru.bookstore.dto.OrderDTO;
import ru.bookstore.model.impl.Order;

@Mapper
public interface OrderMapper {
  OrderMapper INSTANCE = Mappers.getMapper(OrderMapper.class);

  OrderDTO toDTO(Order order);

  List<OrderDTO> toListDTO(List<Order> orders);
}