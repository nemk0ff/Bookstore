package controllers;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
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
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;
import ru.bookstore.config.HibernateConfig;
import ru.bookstore.config.HibernateWebConfig;
import ru.bookstore.config.SecurityConfig;
import ru.bookstore.config.TestConfig;
import ru.bookstore.controllers.impl.importexport.ExportController;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.dto.BookDTO;
import ru.bookstore.dto.RequestDTO;
import ru.bookstore.dto.mappers.BookMapper;
import ru.bookstore.dto.mappers.RequestMapper;
import ru.bookstore.facade.RequestFacade;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Request;
import ru.bookstore.sorting.RequestSort;
import util.TestUtil;

@ActiveProfiles("test")
@SpringBootTest(classes = {
    SecurityConfig.class,
    TestConfig.class,
    HibernateConfig.class
})
@AutoConfigureMockMvc
class RequestsControllerImplTest {
  @Autowired
  private RequestFacade requestFacade;
  @Autowired
  private ImportController importController;
  @Autowired
  private WebApplicationContext webApplicationContext;

  private MockMvc mockMvc;

  @BeforeEach
  void setUp() {
    mockMvc = MockMvcBuilders
        .webAppContextSetup(webApplicationContext)
        .apply(springSecurity())
        .build();

    Mockito.reset(requestFacade, importController);
  }

  @Nested
  class CreateRequestTests {
    @Test
    void whenAdminCreatesRequest_ShouldAllowAccess() throws Exception {
      Request mockRequest = TestUtil.createTestRequest(1L);
      RequestDTO expectedRequest = RequestMapper.INSTANCE.toDTO(mockRequest);
      when(requestFacade.add(anyLong(), anyInt())).thenReturn(1L);
      when(requestFacade.get(1L)).thenReturn(mockRequest);

      mockMvc.perform(post("/requests")
              .param("bookId", "1")
              .param("amount", "5")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedRequest)));
    }

    @Test
    void whenUserCreatesRequest_ShouldDenyAccess() throws Exception {
      mockMvc.perform(post("/requests")
              .param("bookId", "1")
              .param("amount", "5")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetRequestsTests {
    @Test
    void whenAdminGetsRequests_ShouldAllowAccess() throws Exception {
      LinkedHashMap<Book, Long> requestsMap = new LinkedHashMap<>();
      requestsMap.put(TestUtil.createTestBook(1L), 2L);
      LinkedHashMap<BookDTO, Long> expectedMap = new LinkedHashMap<>();
      expectedMap.put(BookMapper.INSTANCE.toDTO(TestUtil.createTestBook(1L)), 2L);

      when(requestFacade.getRequests(any(RequestSort.class))).thenReturn(requestsMap);

      mockMvc.perform(get("/requests")
              .param("sort", "ID")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(
              requestsMap.entrySet().stream()
                  .collect(Collectors.toMap(
                      e -> BookMapper.INSTANCE.toDTO(e.getKey()),
                      Map.Entry::getValue,
                      (e1, e2) -> e1,
                      LinkedHashMap::new
                  ))
          )));
    }

    @Test
    void whenUserGetsRequests_ShouldDenyAccess() throws Exception {
      mockMvc.perform(get("/requests")
              .param("sort", "ID")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class GetAllRequestsTests {
    @Test
    void whenAdminGetsAllRequests_ShouldAllowAccess() throws Exception {
      List<Request> mockRequests = List.of(
          TestUtil.createTestRequest(1L),
          TestUtil.createTestRequest(2L)
      );
      List<RequestDTO> expectedList = RequestMapper.INSTANCE.toListDTO(mockRequests);

      when(requestFacade.getAllRequests()).thenReturn(mockRequests);

      mockMvc.perform(get("/requests/getAll")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }

    @Test
    void whenUserGetsAllRequests_ShouldDenyAccess() throws Exception {
      mockMvc.perform(get("/requests/getAll")
              .with(user("user").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ImportExportTests {
    @Test
    void whenAdminImportsRequest_ShouldAllowAccess() throws Exception {
      Request mockRequest = TestUtil.createTestRequest(1L);
      RequestDTO expectedDto = RequestMapper.INSTANCE.toDTO(mockRequest);

      try (MockedStatic<ImportController> importMock = Mockito.mockStatic(ImportController.class)) {
        importMock.when(() -> ImportController.findItemInFile(eq(1L), anyString(), any()))
            .thenReturn(mockRequest);

        when(requestFacade.importRequest(any(Request.class))).thenReturn(mockRequest);

        mockMvc.perform(put("/requests/import/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
      }
    }

    @Test
    void whenAdminExportsRequest_ShouldAllowAccess() throws Exception {
      Request mockRequest = TestUtil.createTestRequest(1L);
      RequestDTO expectedDto = RequestMapper.INSTANCE.toDTO(mockRequest);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        when(requestFacade.get(1L)).thenReturn(mockRequest);
        exportMock.when(() -> ExportController.exportItemToFile(any(), anyString(), anyString()))
            .thenAnswer(inv -> null);

        mockMvc.perform(put("/requests/export/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
      }
    }

    @Test
    void whenAdminImportsAllRequests_ShouldAllowAccess() throws Exception {
      List<Request> mockRequests = List.of(TestUtil.createTestRequest(1L));
      List<RequestDTO> expectedList = RequestMapper.INSTANCE.toListDTO(mockRequests);

      try (MockedStatic<ImportController> importMock = Mockito.mockStatic(ImportController.class)) {
        importMock.when(() -> ImportController.importAllItemsFromFile(anyString(), any()))
            .thenReturn(mockRequests);

        when(requestFacade.importRequest(any(Request.class))).thenAnswer(inv -> inv.getArgument(0));

        mockMvc.perform(put("/requests/import")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
      }
    }

    @Test
    void whenAdminExportsAllRequests_ShouldAllowAccess() throws Exception {
      List<Request> mockRequests = List.of(TestUtil.createTestRequest(1L));
      List<RequestDTO> expectedList = RequestMapper.INSTANCE.toListDTO(mockRequests);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        when(requestFacade.getAllRequests()).thenReturn(mockRequests);
        exportMock.when(() -> ExportController.exportAll(anyList(), anyString(), anyString()))
            .thenAnswer(inv -> null);

        mockMvc.perform(put("/requests/export")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
      }
    }
  }
}