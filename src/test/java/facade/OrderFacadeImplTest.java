package facade;

import util.TestUtil;
import static org.assertj.core.api.Assertions.*;
import org.junit.jupiter.api.Nested;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.*;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.facade.impl.OrderFacadeImpl;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Order;
import ru.bookstore.service.BookService;
import ru.bookstore.service.MyUserDetailsService;
import ru.bookstore.service.OrderService;
import ru.bookstore.service.RequestService;

@ExtendWith(MockitoExtension.class)
class OrderFacadeImplTest {
  @Mock
  private OrderService orderService;
  @Mock
  private BookService bookService;
  @Mock
  private MyUserDetailsService userDetailsService;
  @Mock
  private RequestService requestService;
  @InjectMocks
  private OrderFacadeImpl orderFacade;

  private static final Long TEST_ORDER_ID = 1L;
  private static final Long TEST_BOOK_ID = 1L;
  private static final int TEST_BOOK_AMOUNT = 2;
  private static final String TEST_CLIENT_NAME = "Test Client";
  private static final LocalDateTime TEST_DATE = LocalDateTime.now();

  private Order testOrder;
  private Book testBook;

  @BeforeEach
  void setUp() {
    testBook = TestUtil.createTestBook(TEST_BOOK_ID);
    testBook.setAmount(10); // Устанавливаем количество книг

    testOrder = TestUtil.createTestOrder(TEST_ORDER_ID);
    testOrder.setBooks(Map.of(TEST_BOOK_ID, TEST_BOOK_AMOUNT)); // Устанавливаем книги в заказе
    testOrder.setStatus(OrderStatus.NEW); // Устанавливаем статус
  }

  @Nested
  class CreateOrderTests {
    @Test
    void createOrder_whenValidData_thenCreateOrderAndRequests() {
      Map<Long, Integer> books = Map.of(TEST_BOOK_ID, TEST_BOOK_AMOUNT);

      when(userDetailsService.existsByUsername(TEST_CLIENT_NAME)).thenReturn(true);
      when(bookService.getBooks(anyList())).thenReturn(List.of(testBook));
      when(bookService.get(TEST_BOOK_ID)).thenReturn(testBook);

      when(orderService.addOrder(any(Order.class))).thenAnswer(ans -> {
        Order order = ans.getArgument(0);
        order.setId(TEST_ORDER_ID);
        return order;
      });

      when(orderService.setOrderStatus(TEST_ORDER_ID, OrderStatus.COMPLETED))
          .thenReturn(testOrder);

      Order result = orderFacade.createOrder(books, TEST_CLIENT_NAME, TEST_DATE);

      assertThat(result.getId()).isEqualTo(TEST_ORDER_ID);
      assertThat(result.getPrice()).isEqualTo(testBook.getPrice() * TEST_BOOK_AMOUNT);
      verify(requestService).addRequest(testBook, TEST_BOOK_AMOUNT);
      verify(orderService).addOrder(any(Order.class));
      verify(orderService).setOrderStatus(TEST_ORDER_ID, OrderStatus.COMPLETED);
    }

    @Test
    void createOrder_whenEmptyBookList_thenThrowException() {
      assertThatThrownBy(() -> orderFacade.createOrder(Map.of(), TEST_CLIENT_NAME, TEST_DATE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Список книг не может быть пустым.");

      verify(orderService, never()).addOrder(any());
    }

    @Test
    void createOrder_whenUserNotRegistered_thenThrowException() {
      Map<Long, Integer> books = Map.of(TEST_BOOK_ID, TEST_BOOK_AMOUNT);
      when(userDetailsService.existsByUsername(TEST_CLIENT_NAME)).thenReturn(false);

      assertThatThrownBy(() -> orderFacade.createOrder(books, TEST_CLIENT_NAME, TEST_DATE))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Клиент не зарегистрирован.");

      verify(orderService, never()).addOrder(any());
      verify(bookService, never()).getBooks(any());
    }
  }

  @Nested
  class CancelOrderTests {
    @Test
    void cancelOrder_whenOrderIsNew_thenCancelSuccessfully() {
      testOrder.setStatus(OrderStatus.NEW);
      when(orderService.getOrder(TEST_ORDER_ID)).thenReturn(testOrder);
      when(orderService.setOrderStatus(TEST_ORDER_ID, OrderStatus.CANCELED))
          .thenReturn(testOrder);
      doNothing().when(requestService).closeRequests(testOrder.getBooks());

      Order result = orderFacade.cancelOrder(TEST_ORDER_ID);

      assertThat(result).isEqualTo(testOrder);
      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(orderService).setOrderStatus(TEST_ORDER_ID, OrderStatus.CANCELED);
      verify(requestService).closeRequests(testOrder.getBooks());
    }

    @Test
    void cancelOrder_whenOrderIsCompleted_thenThrowException() {
      testOrder.setStatus(OrderStatus.COMPLETED);
      when(orderService.getOrder(TEST_ORDER_ID)).thenReturn(testOrder);

      assertThatThrownBy(() -> orderFacade.cancelOrder(TEST_ORDER_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Невозможно отменить заказ, статус которого не NEW");

      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(orderService, never()).setOrderStatus(anyLong(), any());
      verify(requestService, never()).closeRequests(anyMap());
    }

    @Test
    void cancelOrder_whenOrderIsCanceled_thenThrowException() {
      testOrder.setStatus(OrderStatus.CANCELED);
      when(orderService.getOrder(TEST_ORDER_ID)).thenReturn(testOrder);

      assertThatThrownBy(() -> orderFacade.cancelOrder(TEST_ORDER_ID))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Невозможно отменить заказ, статус которого не NEW");

      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(orderService, never()).setOrderStatus(anyLong(), any());
      verify(requestService, never()).closeRequests(anyMap());
    }

    @Test
    void cancelOrder_whenOrderNotFound_thenThrowException() {
      when(orderService.getOrder(TEST_ORDER_ID))
          .thenThrow(new EntityNotFoundException("Заказ не найден"));

      assertThatThrownBy(() -> orderFacade.cancelOrder(TEST_ORDER_ID))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Заказ не найден");

      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(orderService, never()).setOrderStatus(anyLong(), any());
      verify(requestService, never()).closeRequests(anyMap());
    }
  }

  @Nested
  class ImportOrderTests {
    @Test
    void importOrder_whenOrderExists_thenUpdateAndCreateRequests() {
      Book testBook = TestUtil.createTestBook(TEST_BOOK_ID);
      testBook.setAmount(10);

      when(orderService.getOrder(TEST_ORDER_ID)).thenReturn(testOrder);
      when(bookService.get(anyLong())).thenReturn(testBook);
      doNothing().when(requestService).closeRequests(testOrder.getBooks());
      when(orderService.updateOrder(testOrder)).thenReturn(testOrder);

      Order result = orderFacade.importOrder(testOrder);

      assertThat(result).isEqualTo(testOrder);
      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(bookService, times(2)).get(anyLong());
      verify(requestService, times(2)).closeRequests(testOrder.getBooks());
      verify(orderService).updateOrder(testOrder);
    }

    @Test
    void importOrder_whenOrderNotExists_thenAddAndCreateRequests() {
      when(orderService.getOrder(TEST_ORDER_ID))
          .thenThrow(new EntityNotFoundException("Order not found"));
      when(orderService.addOrder(testOrder)).thenReturn(testOrder);

      Order result = orderFacade.importOrder(testOrder);

      assertThat(result).isEqualTo(testOrder);
      verify(orderService).getOrder(TEST_ORDER_ID);
      verify(orderService).addOrder(testOrder);
      verify(requestService, never()).closeRequests(anyMap());
      verify(orderService, never()).updateOrder(any());
    }
  }

  @Nested
  class UpdateOrdersTests {
    @Test
    void updateOrders_whenMarkOrdersCompletedTrue_thenUpdateAllOrders() {
      orderFacade.setMarkOrdersCompleted(true);
      List<Order> orders = List.of(testOrder);

      when(orderService.getAllOrdersById()).thenReturn(orders);
      when(bookService.get(eq(TEST_BOOK_ID))).thenReturn(testBook);
      when(orderService.setOrderStatus(eq(TEST_ORDER_ID), eq(OrderStatus.COMPLETED)))
          .thenReturn(testOrder);
      doNothing().when(requestService).closeRequests(eq(testOrder.getBooks()));
      when(bookService.writeOff(
          eq(TEST_BOOK_ID),
          eq(TEST_BOOK_AMOUNT),
          any(LocalDateTime.class)
      )).thenReturn(testBook);

      orderFacade.updateOrders();

      verify(orderService).getAllOrdersById();
      verify(bookService).get(eq(TEST_BOOK_ID));
      verify(orderService).setOrderStatus(eq(TEST_ORDER_ID), eq(OrderStatus.COMPLETED));
      verify(requestService).closeRequests(eq(testOrder.getBooks()));
      verify(bookService).writeOff(
          eq(TEST_BOOK_ID),
          eq(TEST_BOOK_AMOUNT),
          any(LocalDateTime.class)
      );
    }
  }

  @Test
  void updateOrders_whenMarkOrdersCompletedFalse_thenDoNothing() {
    orderFacade.setMarkOrdersCompleted(false);

    orderFacade.updateOrders();

    verify(orderService, never()).getAllOrdersById();
    verify(orderService, never()).setOrderStatus(anyLong(), any());
    verify(requestService, never()).closeRequests(anyMap());
    verify(bookService, never()).writeOff(anyLong(), anyInt(), any());
  }
}
