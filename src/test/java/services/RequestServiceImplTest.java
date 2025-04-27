package services;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import ru.bookstore.dao.RequestDao;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.BookStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.service.impl.RequestServiceImpl;
import ru.bookstore.sorting.RequestSort;

@ExtendWith(MockitoExtension.class)
class RequestServiceImplTest {
  @Mock
  private RequestDao requestDao;
  @InjectMocks
  private RequestServiceImpl requestService;

  private Book testBook;
  private Request testRequest;
  private final Map<Long, Integer> testBooks = Map.of(1L, 3);

  @BeforeEach
  void setUp() {
    testBook = new Book();
    testBook.setId(1L);
    testBook.setName("Test Book");
    testBook.setAuthor("Test Author");
    testBook.setPublicationDate(2020);
    testBook.setAmount(10);
    testBook.setPrice(100.0);
    testBook.setStatus(BookStatus.AVAILABLE);

    testRequest = new Request();
    testRequest.setId(1L);
    testRequest.setBook(testBook);
    testRequest.setAmount(5);
  }

  @Nested
  class GetRequestTests {
    @Test
    void getRequest_shouldReturnRequestWhenExists() {
      when(requestDao.getRequestById(1L)).thenReturn(Optional.of(testRequest));

      Request result = requestService.getRequest(1L);

      verify(requestDao).getRequestById(1L);
      assertThat(result).isEqualTo(testRequest);
    }

    @Test
    void getRequest_shouldThrowExceptionWhenNotFound() {
      when(requestDao.getRequestById(1L)).thenReturn(Optional.empty());

      assertThrows(EntityNotFoundException.class, () -> requestService.getRequest(1L));
      verify(requestDao).getRequestById(1L);
    }
  }

  @Nested
  class AddRequestTests {
    @Test
    void addRequest_shouldCallDaoAndReturnId() {
      when(requestDao.addRequest(testBook, 5)).thenReturn(1L);

      Long result = requestService.addRequest(testBook, 5);

      verify(requestDao).addRequest(testBook, 5);
      assertThat(result).isEqualTo(1L);
    }
  }

  @Nested
  class GetAllRequestsTests {
    @Test
    void getAllRequests_shouldCallDaoWithCorrectSort() {
      List<Request> expectedRequests = List.of(testRequest);
      when(requestDao.getAllRequests(RequestSort.ID)).thenReturn(expectedRequests);

      List<Request> result = requestService.getAllRequests();

      verify(requestDao).getAllRequests(RequestSort.ID);
      assertThat(result).isEqualTo(expectedRequests);
    }
  }

  @Nested
  class GetRequestsByTests {
    @Test
    void getRequestsByCount_shouldCallDaoWithCorrectSort() {
      LinkedHashMap<Book, Long> expectedMap = new LinkedHashMap<>();
      expectedMap.put(testBook, 5L);
      when(requestDao.getRequests(RequestSort.COUNT)).thenReturn(expectedMap);

      LinkedHashMap<Book, Long> result = requestService.getRequestsByCount();

      verify(requestDao).getRequests(RequestSort.COUNT);
      assertThat(result).isEqualTo(expectedMap);
    }

    @Test
    void getRequestsByPrice_shouldCallDaoWithCorrectSort() {
      LinkedHashMap<Book, Long> expectedMap = new LinkedHashMap<>();
      expectedMap.put(testBook, 500L);
      when(requestDao.getRequests(RequestSort.PRICE)).thenReturn(expectedMap);

      LinkedHashMap<Book, Long> result = requestService.getRequestsByPrice();

      verify(requestDao).getRequests(RequestSort.PRICE);
      assertThat(result).isEqualTo(expectedMap);
    }
  }

  @Nested
  class ImportRequestTests {
    @Test
    void importRequest_shouldImportWhenNotExists() {
      when(requestDao.getRequestById(1L)).thenReturn(Optional.empty());
      when(requestDao.importRequest(testRequest)).thenReturn(testRequest);

      Request result = requestService.importRequest(testRequest);

      verify(requestDao).getRequestById(1L);
      verify(requestDao).importRequest(testRequest);
      assertThat(result).isEqualTo(testRequest);
    }

    @Test
    void importRequest_shouldThrowExceptionWhenExists() {
      when(requestDao.getRequestById(1L)).thenReturn(Optional.of(testRequest));

      assertThrows(IllegalArgumentException.class, () -> requestService.importRequest(testRequest));
      verify(requestDao).getRequestById(1L);
    }
  }

  @Nested
  class CloseRequestsTests {
    @Test
    void closeRequests_shouldCallDao() {
      requestService.closeRequests(testBooks);
      verify(requestDao).closeRequests(testBooks);
    }
  }
}