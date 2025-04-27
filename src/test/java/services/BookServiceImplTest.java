package services;

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
import java.util.List;
import java.util.Optional;
import ru.bookstore.dao.BookDao;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.model.impl.Book;
import ru.bookstore.service.impl.BookServiceImpl;
import ru.bookstore.sorting.BookSort;
import util.TestUtil;

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {
  @Mock
  private BookDao bookDao;
  @InjectMocks
  private BookServiceImpl bookService;

  private static final Long TEST_BOOK_ID = 1L;
  private static final int TEST_AMOUNT = 5;
  private static final LocalDateTime TEST_DATE_TIME = LocalDateTime.now();
  private Book testBook;


  @BeforeEach
  void setUp() {
    testBook = TestUtil.createTestBook(TEST_BOOK_ID);
  }

  @Nested
  class GetBookTests {
    @Test
    void get_whenBookExists_thenReturnBook() {
      when(bookDao.getBookById(TEST_BOOK_ID)).thenReturn(Optional.of(testBook));

      Book result = bookService.get(TEST_BOOK_ID);

      assertThat(result).isEqualTo(testBook);
      verify(bookDao).getBookById(TEST_BOOK_ID);
    }

    @Test
    void get_whenBookNotExists_thenThrowException() {
      when(bookDao.getBookById(TEST_BOOK_ID)).thenReturn(Optional.empty());

      assertThatThrownBy(() -> bookService.get(TEST_BOOK_ID))
          .isInstanceOf(EntityNotFoundException.class)
          .hasMessageContaining("Книга [" + TEST_BOOK_ID + "] не найдена");

      verify(bookDao).getBookById(TEST_BOOK_ID);
    }
  }

  @Nested
  class AddBookTests {
    @Test
    void add_whenValidData_thenReturnAddedBook() {
      when(bookDao.add(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME))
          .thenReturn(testBook);

      Book result = bookService.add(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME);

      assertThat(result).isEqualTo(testBook);
      verify(bookDao).add(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME);
    }

    @Test
    void add_whenNegativeAmount_thenThrowException() {
      assertThatThrownBy(() -> bookService.add(TEST_BOOK_ID, -1, TEST_DATE_TIME))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Количество добавленных книг должно быть положительным");
    }
  }

  @Nested
  class WriteOffBookTests {
    @Test
    void writeOff_whenValidData_thenReturnUpdatedBook() {
      when(bookDao.writeOff(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME))
          .thenReturn(testBook);

      Book result = bookService.writeOff(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME);

      assertThat(result).isEqualTo(testBook);
      verify(bookDao).writeOff(TEST_BOOK_ID, TEST_AMOUNT, TEST_DATE_TIME);
    }

    @Test
    void writeOff_whenNegativeAmount_thenPropagateDaoException() {
      assertThatThrownBy(() -> bookService.writeOff(TEST_BOOK_ID, -1, TEST_DATE_TIME))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Количество списываемых книг должно быть положительным");
    }
  }

  @Nested
  class GetBooksTests {
    @Test
    void getBooks_whenValidIds_thenReturnBooks() {
      List<Long> bookIds = List.of(1L, 2L);
      List<Book> expectedBooks = List.of(testBook);

      when(bookDao.getBooks(bookIds)).thenReturn(expectedBooks);

      List<Book> result = bookService.getBooks(bookIds);

      assertThat(result).isEqualTo(expectedBooks);
      verify(bookDao).getBooks(bookIds);
    }

    @Test
    void getBooks_whenEmptyList_thenReturnEmptyList() {
      List<Book> result = bookService.getBooks(List.of());

      assertThat(result).isEmpty();
      verify(bookDao).getBooks(List.of());
    }
  }

  @Nested
  class GetAllBooksTests {
    @Test
    void getAllBooksById_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.ID)).thenReturn(expected);

      List<Book> result = bookService.getAllBooksById();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.ID);
    }

    @Test
    void getAllBooksByName_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.NAME)).thenReturn(expected);

      List<Book> result = bookService.getAllBooksByName();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.NAME);
    }

    @Test
    void getAllBooksByDate_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.PUBLICATION_DATE)).thenReturn(expected);

      List<Book> result = bookService.getAllBooksByDate();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.PUBLICATION_DATE);
    }

    @Test
    void getAllBooksByPrice_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.PRICE)).thenReturn(expected);

      List<Book> result = bookService.getAllBooksByPrice();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.PRICE);
    }

    @Test
    void getAllBooksByAvailable_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.STATUS)).thenReturn(expected);

      List<Book> result = bookService.getAllBooksByAvailable();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.STATUS);
    }

    @Test
    void getAllStaleBooksByDate_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.STALE_BY_DATE)).thenReturn(expected);

      List<Book> result = bookService.getAllStaleBooksByDate();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.STALE_BY_DATE);
    }

    @Test
    void getAllStaleBooksByPrice_thenCallDaoWithCorrectSort() {
      List<Book> expected = List.of(testBook);
      when(bookDao.getAllBooks(BookSort.STALE_BY_PRICE)).thenReturn(expected);

      List<Book> result = bookService.getAllStaleBooksByPrice();

      assertThat(result).isEqualTo(expected);
      verify(bookDao).getAllBooks(BookSort.STALE_BY_PRICE);
    }
  }

  @Nested
  class ImportBookTests {
    @Test
    void importBook_whenValidBook_thenCallDao() {
      Book bookToImport = new Book();
      bookToImport.setId(1L);
      bookToImport.setName("Imported Book");

      bookService.importBook(bookToImport);

      verify(bookDao).importBook(bookToImport);
    }

    @Test
    void importBook_whenNullBook_thenPropagateException() {
      doThrow(new IllegalArgumentException("Book cannot be null"))
          .when(bookDao).importBook(null);

      assertThatThrownBy(() -> bookService.importBook(null))
          .isInstanceOf(IllegalArgumentException.class)
          .hasMessageContaining("Book cannot be null");
    }
  }
}
