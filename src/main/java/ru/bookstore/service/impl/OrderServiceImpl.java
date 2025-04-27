package ru.bookstore.service.impl;

import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.bookstore.dao.OrderDao;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Order;
import ru.bookstore.service.OrderService;
import ru.bookstore.sorting.OrderSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderServiceImpl implements OrderService {
  private final OrderDao orderDao;

  @Override
  public Order addOrder(Order order) {
    return orderDao.addOrder(order);
  }

  @Override
  public Order getOrder(Long orderId) {
    return orderDao.findWithBooks(orderId).orElseThrow(()
        -> new EntityNotFoundException("Заказ [" + orderId + "] не найден"));
  }

  @Override
  public Order updateOrder(Order order) {
    return orderDao.update(order);
  }

  @Override
  public Order setOrderStatus(Long orderId, OrderStatus orderStatus) {
    return orderDao.setOrderStatus(orderId, orderStatus);
  }

  @Override
  public List<Order> getAllOrdersById() {
    return orderDao.getAllOrders(OrderSort.ID, null, null);
  }

  @Override
  public List<Order> getAllOrdersByDate() {
    return orderDao.getAllOrders(OrderSort.COMPLETE_DATE, null, null);
  }

  @Override
  public List<Order> getAllOrdersByPrice() {
    return orderDao.getAllOrders(OrderSort.PRICE, null, null);
  }

  @Override
  public List<Order> getAllOrdersByStatus() {
    return orderDao.getAllOrders(OrderSort.STATUS, null, null);
  }

  @Override
  public List<Order> getCompletedOrdersByDate(LocalDateTime begin, LocalDateTime end) {
    return orderDao.getAllOrders(OrderSort.COMPLETED_BY_DATE, begin, end);
  }

  @Override
  public List<Order> getCompletedOrdersByPrice(LocalDateTime begin, LocalDateTime end) {
    return orderDao.getAllOrders(OrderSort.COMPLETED_BY_PRICE, begin, end);
  }

  @Override
  public Double getEarnedSum(LocalDateTime begin, LocalDateTime end) {
    return orderDao.getEarnedSum(begin, end);
  }

  @Override
  public Long getCountCompletedOrders(LocalDateTime begin, LocalDateTime end) {
    return orderDao.getCountCompletedOrders(begin, end);
  }
}
