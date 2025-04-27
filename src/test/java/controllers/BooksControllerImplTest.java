package controllers;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.security.test.web.servlet.setup.SecurityMockMvcConfigurers.springSecurity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.context.web.WebAppConfiguration;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.bookstore.config.HibernateConfig;
import ru.bookstore.config.SecurityConfig;
import ru.bookstore.config.TestConfig;
import ru.bookstore.controllers.impl.importexport.ExportController;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.dto.BookDTO;
import ru.bookstore.dto.mappers.BookMapper;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.facade.BookFacade;
import ru.bookstore.facade.OrderFacade;
import ru.bookstore.model.impl.Book;
import ru.bookstore.sorting.BookSort;
import util.TestUtil;

@ActiveProfiles("test")
@SpringBootTest(classes = {
    SecurityConfig.class,
    TestConfig.class,
    HibernateConfig.class
})
@AutoConfigureMockMvc
class BooksControllerImplTest {
  @Autowired
  private BookFacade bookFacade;
  @Autowired
  private OrderFacade orderFacade;
  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();

    Mockito.reset(bookFacade, orderFacade);
  }

  @Nested
  class ShowBookDetailsTests {
    @Test
    void whenBookExists_ShouldReturnBookDetails() throws Exception {
      Book mockBook = TestUtil.createTestBook(1L);
      BookDTO expectedDto = BookMapper.INSTANCE.toDTO(mockBook);
      when(bookFacade.get(1L)).thenReturn(mockBook);

      mockMvc.perform(get("/books/1")
              .with(user("user").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenBookNotExists_ShouldReturnNotFound() throws Exception {
      when(bookFacade.get(999L)).thenThrow(new EntityNotFoundException("Book not found"));

      mockMvc.perform(get("/books/999")
              .with(user("user").roles("USER")))
          .andExpect(status().isNotFound());
    }

    @Test
    void whenInvalidIdFormat_ShouldReturnBadRequest() throws Exception {
      mockMvc.perform(get("/books/abc")
              .with(user("user").roles("USER")))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class AddBookEndpointTest {
    @Test
    void whenAdmin_ShouldAllowAccess() throws Exception {
      Book book = TestUtil.createTestBook(1L);
      BookDTO expectedDto = BookMapper.INSTANCE.toDTO(book);
      when(bookFacade.addBook(eq(1L), eq(10), any(LocalDateTime.class))).thenReturn(book);

      mockMvc.perform(patch("/books/add")
              .param("id", "1")
              .param("amount", "10")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenUser_ShouldDenyAccess() throws Exception {
      mockMvc.perform(patch("/books/add")
              .param("id", "1")
              .param("amount", "10")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class WriteOffEndpointTest {
    @Test
    void whenAdmin_ShouldAllowAccess() throws Exception {
      Book book = TestUtil.createTestBook(1L);
      BookDTO expectedDto = BookMapper.INSTANCE.toDTO(book);
      when(bookFacade.writeOff(eq(1L), eq(5), any(LocalDateTime.class))).thenReturn(book);

      mockMvc.perform(patch("/books/writeOff")
              .param("id", "1")
              .param("amount", "5")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenUser_ShouldDenyAccess() throws Exception {
      mockMvc.perform(patch("/books/writeOff")
              .param("id", "1")
              .param("amount", "5")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetStaleBooksEndpointTest {
    @Test
    void whenAdmin_ShouldAllowAccess() throws Exception {
      List<Book> bookList = Collections.singletonList(TestUtil.createTestBook(1L));
      List<BookDTO> expectedList = BookMapper.INSTANCE.toListDTO(bookList);
      when(bookFacade.getStale(BookSort.ID)).thenReturn(bookList);

      mockMvc.perform(get("/books/stale")
              .param("sort", "ID")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }

    @Test
    void whenUser_ShouldDenyAccess() throws Exception {
      mockMvc.perform(get("/books/stale")
              .param("sort", "ID")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ImportExportAllEndpointTest {
    @Test
    void whenAdminImportAllBooks_ShouldAllowAccess() throws Exception {
      List<Book> mockBooks = List.of(
          TestUtil.createTestBook(1L),
          TestUtil.createTestBook(2L)
      );
      List<BookDTO> expectedList = BookMapper.INSTANCE.toListDTO(mockBooks);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        exportMock.when(() -> ExportController.exportAll(anyList(), anyString(), anyString()))
            .thenAnswer(invocation -> null);

        when(bookFacade.getAll(BookSort.ID)).thenReturn(mockBooks);

        mockMvc.perform(put("/books/export")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));

        exportMock.verify(() ->
            ExportController.exportAll(eq(mockBooks), anyString(), anyString()));
      }
    }

    @Test
    void whenAdminExportAllBooks_ShouldAllowAccess() throws Exception {
      List<Book> mockBooks = List.of(
          TestUtil.createTestBook(1L),
          TestUtil.createTestBook(2L)
      );
      List<BookDTO> expectedList = BookMapper.INSTANCE.toListDTO(mockBooks);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        exportMock.when(() -> ExportController.exportAll(anyList(), anyString(), anyString()))
            .thenAnswer(invocation -> null);

        when(bookFacade.getAll(BookSort.ID)).thenReturn(mockBooks);

        mockMvc.perform(put("/books/export")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));

        exportMock.verify(() ->
            ExportController.exportAll(eq(mockBooks), anyString(), anyString()));
      }
    }

    @Test
    void whenUserImportAll_ShouldDenyAccess() throws Exception {
      mockMvc.perform(put("/books/import")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenUserExportAll_ShouldDenyAccess() throws Exception {
      mockMvc.perform(put("/books/export")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ImportExportBookEndpointTest {
    @Test
    void whenAdminImportBook_ShouldAllowAccess() throws Exception {
      Book mockBook = TestUtil.createTestBook(1L);
      BookDTO expectedDto = BookMapper.INSTANCE.toDTO(mockBook);

      try (MockedStatic<ImportController> importMock = Mockito.mockStatic(ImportController.class)) {
        importMock.when(() -> ImportController.findItemInFile(eq(1L), anyString(), any()))
            .thenReturn(mockBook);

        doNothing().when(bookFacade).importBook(any());
        doNothing().when(orderFacade).updateOrders();

        mockMvc.perform(put("/books/import/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
      }
    }

    @Test
    void whenAdminExportBook_ShouldAllowAccess() throws Exception {
      Book mockBook = TestUtil.createTestBook(1L);
      BookDTO expectedDto = BookMapper.INSTANCE.toDTO(mockBook);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        exportMock.when(() -> ExportController.exportItemToFile(any(Book.class), anyString(), anyString()))
            .thenAnswer(invocation -> null);

        when(bookFacade.get(1L)).thenReturn(mockBook);

        mockMvc.perform(put("/books/export/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));

        exportMock.verify(() ->
            ExportController.exportItemToFile(eq(mockBook), anyString(), anyString()));
      }
    }

    @Test
    void whenUserImportBook_ShouldDenyAccess() throws Exception {
      mockMvc.perform(put("/books/import/1")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenUserExportBook_ShouldDenyAccess() throws Exception {
      mockMvc.perform(put("/books/export/1")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetBooksEndpointTest {
    @Test
    void whenAdmin_ShouldAllowAccess() throws Exception {
      List<Book> mockBooks = List.of(
          TestUtil.createTestBook(1L),
          TestUtil.createTestBook(2L)
      );
      List<BookDTO> expectedList = BookMapper.INSTANCE.toListDTO(mockBooks);

      when(bookFacade.getAll(BookSort.ID)).thenReturn(mockBooks);

      mockMvc.perform(get("/books")
              .param("sort", "ID")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }

    @Test
    void whenUser_ShouldAllowAccess() throws Exception {
      List<Book> mockBooks = List.of(
          TestUtil.createTestBook(1L),
          TestUtil.createTestBook(2L)
      );
      List<BookDTO> expectedList = BookMapper.INSTANCE.toListDTO(mockBooks);

      when(bookFacade.getAll(BookSort.ID)).thenReturn(mockBooks);

      mockMvc.perform(get("/books")
              .param("sort", "ID")
              .with(user("user").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }
  }
}