package ru.bookstore.facade.impl;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.facade.OrderFacade;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Order;
import ru.bookstore.service.BookService;
import ru.bookstore.service.MyUserDetailsService;
import ru.bookstore.service.OrderService;
import ru.bookstore.service.RequestService;
import ru.bookstore.sorting.OrderSort;

@Service
@RequiredArgsConstructor
@Slf4j
public class OrderFacadeImpl implements OrderFacade {
  @Value("${mark.orders.completed}")
  @Setter
  private boolean markOrdersCompleted;

  private final OrderService orderService;
  private final BookService bookService;
  private final RequestService requestService;
  private final MyUserDetailsService userDetailsService;

  @Transactional(readOnly = true)
  @Override
  public Order get(Long orderId) {
    return orderService.getOrder(orderId);
  }

  @Transactional(readOnly = true)
  @Override
  public Order setOrderStatus(Long id, OrderStatus orderStatus) {
    return orderService.setOrderStatus(id, orderStatus);
  }

  @Transactional(readOnly = true)
  @Override
  public Double getEarnedSum(LocalDateTime begin, LocalDateTime end) {
    return orderService.getEarnedSum(begin, end);
  }

  @Transactional(readOnly = true)
  @Override
  public Long getCountCompletedOrders(LocalDateTime begin, LocalDateTime end) {
    return orderService.getCountCompletedOrders(begin, end);
  }

  @Transactional(readOnly = true)
  @Override
  public List<Order> getAll(OrderSort orderSort) {
    return switch (orderSort) {
      case COMPLETE_DATE -> orderService.getAllOrdersByDate();
      case PRICE -> orderService.getAllOrdersByPrice();
      case STATUS -> orderService.getAllOrdersByStatus();
      default -> orderService.getAllOrdersById();
    };
  }

  @Transactional(readOnly = true)
  @Override
  public List<Order> getCompleted(OrderSort orderSort,
                                  LocalDateTime begin, LocalDateTime end) {
    if (orderSort == OrderSort.COMPLETED_BY_DATE) {
      return orderService.getCompletedOrdersByDate(begin, end);
    } else {
      return orderService.getCompletedOrdersByPrice(begin, end);
    }
  }

  @Transactional
  @Override
  public Order createOrder(Map<Long, Integer> booksIds, String clientName,
                           LocalDateTime orderDate) {
    if (booksIds.isEmpty()) {
      throw new IllegalArgumentException("Список книг не может быть пустым.");
    } else if (!userDetailsService.existsByUsername(clientName)) {
      throw new IllegalArgumentException("Клиент не зарегистрирован.");
    }
    Order createdOrder = orderService.addOrder(
        new Order(booksIds, bookService.getBooks(booksIds.keySet()
                .stream()
                .toList())
            .stream()
            .mapToDouble(book -> book.getPrice() * booksIds.get(book.getId()))
            .sum(),
            OrderStatus.NEW, orderDate, clientName));
    createRequests(createdOrder);
    updateOrder(createdOrder, LocalDateTime.now());
    return createdOrder;
  }

  @Transactional
  @Override
  public Order cancelOrder(long orderId) {
    log.debug("Отменяем заказ [{}]...", orderId);
    Order order = orderService.getOrder(orderId);
    if (order.getStatus() == OrderStatus.NEW) {
      order = orderService.setOrderStatus(orderId, OrderStatus.CANCELED);
      requestService.closeRequests(order.getBooks());
      log.info("Заказ [{}] успешно отменен", orderId);
      return order;
    } else {
      throw new IllegalArgumentException("Невозможно отменить заказ, статус которого не NEW");
    }
  }

  @Transactional
  @Override
  public Order importOrder(Order order) {
    try {
      Order findOrder = orderService.getOrder(order.getId());
      requestService.closeRequests(findOrder.getBooks());
      orderService.updateOrder(order);
      updateOrder(order, LocalDateTime.now());
    } catch (EntityNotFoundException e) {
      orderService.addOrder(order);
    } finally {
      createRequests(order);
    }
    return order;
  }

  private void createRequests(Order order) {
    log.info("Создание запросов для заказа [{}]...", order);
    for (Map.Entry<Long, Integer> entry : order.getBooks().entrySet()) {
      Book book = bookService.get(entry.getKey());
      requestService.addRequest(book, entry.getValue());
    }
    log.info("Созданы запросы для заказа [{}].", order);
  }

  @Transactional
  @Override
  public void updateOrders() {
    if (markOrdersCompleted) {
      log.info("Обновление всех заказов...");
      getAll(OrderSort.ID).forEach(order -> updateOrder(order, LocalDateTime.now()));
      log.info("Все заказы успено обновлены.");
    }
  }

  public void updateOrder(Order order, LocalDateTime updateDate) {
    boolean needBeCompleted = true;
    log.debug("Обновляем заказ: {}...", order);
    if (order.getStatus() == OrderStatus.NEW) {
      for (Map.Entry<Long, Integer> entry : order.getBooks().entrySet()) {
        Book book = bookService.get(entry.getKey());
        if (book.getAmount() < entry.getValue()) {
          log.debug("В заказе [{}] есть {} книг [{}], но на складе таких книг только {}",
              order.getId(), entry.getValue(), entry.getValue(), book.getAmount());
          needBeCompleted = false;
          break;
        }
      }
      if (!needBeCompleted) {
        return;
      }
      log.info("Есть все необходимые книги для заказа [{}], выполняем заказ...",
          order.getId());
      completeOrder(order, updateDate);
      log.info("Заказ [{}] успешно выполнен.", order.getId());
    }
    log.debug("Заказ успешно обновлен: {}.", order);
  }

  private void completeOrder(Order order, LocalDateTime completeDate) {
    orderService.setOrderStatus(order.getId(), OrderStatus.COMPLETED);
    requestService.closeRequests(order.getBooks());
    for (Map.Entry<Long, Integer> entry : order.getBooks().entrySet()) {
      bookService.writeOff(entry.getKey(), entry.getValue(), completeDate);
    }
  }
}
