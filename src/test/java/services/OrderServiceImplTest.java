package services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import org.junit.jupiter.api.Nested;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import ru.bookstore.dao.OrderDao;
import ru.bookstore.exceptions.DataAccessException;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Order;
import ru.bookstore.service.impl.OrderServiceImpl;
import ru.bookstore.sorting.OrderSort;
import util.TestUtil;

@ExtendWith(MockitoExtension.class)
class OrderServiceImplTest {

  @Mock
  private OrderDao orderDao;

  @InjectMocks
  private OrderServiceImpl orderService;

  private final Order testOrder = TestUtil.createTestOrder(1L);
  private final LocalDateTime testBegin = LocalDateTime.of(2024, 1, 1, 0, 0);
  private final LocalDateTime testEnd = LocalDateTime.of(2024, 12, 31, 23, 59);

  @Nested
  class MainCrudTests {
    @Test
    void addOrder_whenValidOrder_thenReturnSavedOrder() {
      when(orderDao.addOrder(testOrder)).thenReturn(testOrder);

      Order result = orderService.addOrder(testOrder);

      assertThat(result).isEqualTo(testOrder);
      verify(orderDao).addOrder(testOrder);
    }

    @Test
    void getOrder_whenExists_thenReturnOrder() {
      when(orderDao.findWithBooks(1L)).thenReturn(Optional.of(testOrder));

      Order result = orderService.getOrder(1L);

      assertThat(result).isEqualTo(testOrder);
      verify(orderDao).findWithBooks(1L);
    }

    @Test
    void getOrder_whenDaoThrowsException_thenPropagate() {
      when(orderDao.findWithBooks(anyLong()))
          .thenThrow(new DataAccessException("Dao throws exception", new Exception()));

      assertThatThrownBy(() -> orderService.getOrder(1L))
          .isInstanceOf(DataAccessException.class)
          .hasMessageContaining("Dao throws exception");
    }

    @Test
    void getOrder_whenNotExists_thenThrowException() {
      when(orderDao.findWithBooks(1L)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> orderService.getOrder(1L))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Заказ [1] не найден");
    }

    @Test
    void updateOrder_whenValidOrder_thenReturnUpdatedOrder() {
      when(orderDao.update(testOrder)).thenReturn(testOrder);

      Order result = orderService.updateOrder(testOrder);

      assertThat(result).isEqualTo(testOrder);
      verify(orderDao).update(testOrder);
    }

    @Test
    void setOrderStatus_whenValidData_thenReturnUpdatedOrder() {
      when(orderDao.setOrderStatus(1L, OrderStatus.COMPLETED))
          .thenReturn(testOrder);

      Order result = orderService.setOrderStatus(1L, OrderStatus.COMPLETED);

      assertThat(result).isEqualTo(testOrder);
      verify(orderDao).setOrderStatus(1L, OrderStatus.COMPLETED);
    }
  }

  @Nested
  class GetAllOrdersTests {
    @Test
    void getAllOrdersById_thenCallDaoWithCorrectSort() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.ID, null, null)).thenReturn(expected);

      List<Order> result = orderService.getAllOrdersById();

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.ID, null, null);
    }

    @Test
    void getAllOrdersByDate_thenCallDaoWithCorrectSort() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.COMPLETE_DATE, null, null)).thenReturn(expected);

      List<Order> result = orderService.getAllOrdersByDate();

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.COMPLETE_DATE, null, null);
    }

    @Test
    void getAllOrdersByPrice_thenCallDaoWithCorrectSort() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.PRICE, null, null)).thenReturn(expected);

      List<Order> result = orderService.getAllOrdersByPrice();

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.PRICE, null, null);
    }

    @Test
    void getAllOrdersByStatus_thenCallDaoWithCorrectSort() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.STATUS, null, null)).thenReturn(expected);

      List<Order> result = orderService.getAllOrdersByStatus();

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.STATUS, null, null);
    }

    @Test
    void getAllOrders_whenEmptyResult_thenReturnEmptyList() {
      when(orderDao.getAllOrders(any(), any(), any())).thenReturn(List.of());

      assertThat(orderService.getAllOrdersById()).isEmpty();
      assertThat(orderService.getAllOrdersByDate()).isEmpty();
      assertThat(orderService.getAllOrdersByPrice()).isEmpty();
      assertThat(orderService.getAllOrdersByStatus()).isEmpty();
      assertThat(orderService.getCompletedOrdersByDate(testBegin, testEnd)).isEmpty();
      assertThat(orderService.getCompletedOrdersByPrice(testBegin, testEnd)).isEmpty();

      verify(orderDao).getAllOrders(OrderSort.ID, null, null);
      verify(orderDao).getAllOrders(OrderSort.COMPLETE_DATE, null, null);
      verify(orderDao).getAllOrders(OrderSort.PRICE, null, null);
      verify(orderDao).getAllOrders(OrderSort.STATUS, null, null);
      verify(orderDao).getAllOrders(OrderSort.COMPLETED_BY_DATE, testBegin, testEnd);
      verify(orderDao).getAllOrders(OrderSort.COMPLETED_BY_PRICE, testBegin, testEnd);
    }
  }

  @Nested
  class CompletedOrdersTests {
    @Test
    void getCompletedOrdersByDate_whenValidPeriod_thenReturnOrders() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.COMPLETED_BY_DATE, testBegin, testEnd))
          .thenReturn(expected);

      List<Order> result = orderService.getCompletedOrdersByDate(testBegin, testEnd);

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.COMPLETED_BY_DATE, testBegin, testEnd);
    }

    @Test
    void getCompletedOrdersByPrice_whenValidPeriod_thenReturnOrders() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(OrderSort.COMPLETED_BY_PRICE, testBegin, testEnd))
          .thenReturn(expected);

      List<Order> result = orderService.getCompletedOrdersByPrice(testBegin, testEnd);

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.COMPLETED_BY_PRICE, testBegin, testEnd);
    }

    @Test
    void getEarnedSum_whenValidPeriod_thenReturnSum() {
      Double expectedSum = 1000.0;
      when(orderDao.getEarnedSum(testBegin, testEnd)).thenReturn(expectedSum);

      Double result = orderService.getEarnedSum(testBegin, testEnd);

      assertThat(result).isEqualTo(expectedSum);
      verify(orderDao).getEarnedSum(testBegin, testEnd);
    }

    @Test
    void getCountCompletedOrders_whenValidPeriod_thenReturnCount() {
      Long expectedCount = 5L;
      when(orderDao.getCountCompletedOrders(testBegin, testEnd)).thenReturn(expectedCount);

      Long result = orderService.getCountCompletedOrders(testBegin, testEnd);

      assertThat(result).isEqualTo(expectedCount);
      verify(orderDao).getCountCompletedOrders(testBegin, testEnd);
    }

    @Test
    void getCompletedOrders_whenNullDates_thenHandleCorrectly() {
      List<Order> expected = List.of(testOrder);
      when(orderDao.getAllOrders(any(), isNull(), isNull())).thenReturn(expected);

      List<Order> result = orderService.getCompletedOrdersByDate(null, null);

      assertThat(result).isEqualTo(expected);
      verify(orderDao).getAllOrders(OrderSort.COMPLETED_BY_DATE, null, null);
    }
  }
}
