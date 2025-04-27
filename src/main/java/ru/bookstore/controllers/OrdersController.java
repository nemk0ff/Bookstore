package ru.bookstore.controllers;

import java.time.LocalDateTime;
import org.springframework.http.ResponseEntity;
import ru.bookstore.dto.OrderDTO;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.sorting.OrderSort;

public interface OrdersController {
  ResponseEntity<?> createOrder(OrderDTO orderDTO);

  ResponseEntity<?> cancelOrder(Long id);

  ResponseEntity<?> showOrderDetails(Long id);

  ResponseEntity<?> setOrderStatus(Long id, OrderStatus newStatus);

  ResponseEntity<?> getOrders(OrderSort orderSort);


  ResponseEntity<?> getCompleted(OrderSort orderSort, LocalDateTime begin, LocalDateTime end);

  ResponseEntity<?> getCountCompletedOrders(LocalDateTime begin, LocalDateTime end);

  ResponseEntity<?> getEarnedSum(LocalDateTime begin, LocalDateTime end);

  ResponseEntity<?> importAll();

  ResponseEntity<?> exportAll();

  ResponseEntity<?> importOrder(Long id);

  ResponseEntity<?> exportOrder(Long id);
}
