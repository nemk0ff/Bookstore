package util;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import ru.bookstore.dto.OrderDTO;
import ru.bookstore.model.BookStatus;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.RequestStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Order;
import ru.bookstore.model.impl.Request;

public class TestUtil {
  public static final ObjectMapper objectMapper = new ObjectMapper()
      .registerModule(new JavaTimeModule())
      .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
      .setDateFormat(new SimpleDateFormat("HH:mm:ss dd-MM-yyyy"));

  public static Book createTestBook(Long id) {
    return Book.builder()
        .id(id)
        .name("Test Book " + id)
        .author("Author " + id)
        .publicationDate(2020)
        .amount(10)
        .price(19.99)
        .lastDeliveredDate(LocalDateTime.now().minusDays(30))
        .lastSaleDate(LocalDateTime.now().minusDays(10))
        .status(BookStatus.AVAILABLE)
        .build();
  }

  public static Order createTestOrder(Long id) {
    Map<Long, Integer> books = new HashMap<>();
    books.put(1L, 2);
    books.put(2L, 1);

    return new Order(id, OrderStatus.NEW, 100.0,
        LocalDateTime.of(2023, 1, 1, 12, 0),
        null, "test_client", books);
  }

  public static OrderDTO createTestOrderDTO(Long id) {
    Map<Long, Integer> books = new HashMap<>();
    books.put(1L, 2);
    books.put(2L, 1);

    return OrderDTO.builder()
        .id(id)
        .status(OrderStatus.NEW)
        .price(100.0)
        .orderDate(LocalDateTime.of(2023, 1, 1, 12, 0))
        .completeDate(null)
        .clientName("test_client")
        .books(books)
        .build();
  }

  public static Request createTestRequest(Long id) {
    Book book = createTestBook(1L);
    return new Request(id, book, 2, RequestStatus.OPEN);
  }

  public static String toJson(Object object) throws JsonProcessingException {
    return objectMapper.writeValueAsString(object);
  }
}
