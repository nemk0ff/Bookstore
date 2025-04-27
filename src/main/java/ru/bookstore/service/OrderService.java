package ru.bookstore.service;

import java.time.LocalDateTime;
import java.util.List;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Order;

public interface OrderService {
  Order getOrder(Long orderId);

  Order addOrder(Order order);

  Order updateOrder(Order order);

  Order setOrderStatus(Long orderId, OrderStatus orderStatus);

  List<Order> getAllOrdersById();

  List<Order> getAllOrdersByDate();

  List<Order> getAllOrdersByPrice();

  List<Order> getAllOrdersByStatus();

  List<Order> getCompletedOrdersByDate(LocalDateTime begin, LocalDateTime end);

  List<Order> getCompletedOrdersByPrice(LocalDateTime begin, LocalDateTime end);

  Long getCountCompletedOrders(LocalDateTime begin, LocalDateTime end);

  Double getEarnedSum(LocalDateTime begin, LocalDateTime end);
}
