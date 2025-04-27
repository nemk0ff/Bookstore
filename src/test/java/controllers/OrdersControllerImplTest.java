package controllers;

import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import static org.mockito.Mockito.when;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
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
import ru.bookstore.dto.OrderDTO;
import ru.bookstore.dto.mappers.OrderMapper;
import ru.bookstore.exceptions.EntityNotFoundException;
import ru.bookstore.facade.OrderFacade;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Order;
import ru.bookstore.sorting.OrderSort;
import util.TestUtil;

@ActiveProfiles("test")
@SpringBootTest(classes = {
    SecurityConfig.class,
    TestConfig.class,
    HibernateConfig.class
})
@AutoConfigureMockMvc
class OrdersControllerImplTest {
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

    Mockito.reset(orderFacade);
  }

  @Nested
  class CreateOrderTests {
    @Test
    void whenValidOrder_ShouldCreateOrder() throws Exception {
      OrderDTO orderDTO = TestUtil.createTestOrderDTO(1L);
      Order mockOrder = TestUtil.createTestOrder(1L);

      when(orderFacade.createOrder(anyMap(), anyString(), any(LocalDateTime.class)))
          .thenReturn(mockOrder);

      mockMvc.perform(post("/orders")
              .contentType(MediaType.APPLICATION_JSON)
              .content(TestUtil.toJson(orderDTO))
              .with(user("test_client").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(orderDTO)));
    }

    @Test
    void whenDifferentClientName_ShouldDenyAccess() throws Exception {
      OrderDTO orderDTO = TestUtil.createTestOrderDTO(1L);
      orderDTO.setClientName("another_client");

      mockMvc.perform(post("/orders")
              .contentType(MediaType.APPLICATION_JSON)
              .content(TestUtil.toJson(orderDTO))
              .with(user("client").roles("USER")))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenInvalidOrder_ShouldReturnBadRequest() throws Exception {
      OrderDTO invalidOrder = new OrderDTO();

      mockMvc.perform(post("/orders")
              .contentType(MediaType.APPLICATION_JSON)
              .content(TestUtil.toJson(invalidOrder))
              .with(user("client").roles("USER")))
          .andExpect(status().isBadRequest());
    }
  }

  @Nested
  class CancelOrderTests {
    @Test
    void whenOrderExists_ShouldCancelOrder() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      OrderDTO expectedDto = TestUtil.createTestOrderDTO(1L);

      when(orderFacade.get(1L)).thenReturn(mockOrder);
      when(orderFacade.cancelOrder(1L)).thenReturn(mockOrder);

      mockMvc.perform(post("/orders/cancelOrder/1")
              .with(user("test_client").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenDifferentClient_ShouldDenyAccess() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      mockOrder.setClientName("another_client");

      when(orderFacade.get(1L)).thenReturn(mockOrder);

      mockMvc.perform(post("/orders/cancelOrder/1")
              .with(user("client").roles("USER")))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenOrderNotExists_ShouldReturnNotFound() throws Exception {
      when(orderFacade.get(1L)).thenThrow(new EntityNotFoundException("Order not found"));

      mockMvc.perform(post("/orders/cancelOrder/1")
              .with(user("client").roles("USER")))
          .andExpect(status().isNotFound());
    }
  }

  @Nested
  class ShowOrderDetailsTests {
    @Test
    void whenOrderExists_ShouldReturnOrderDetails() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      OrderDTO expectedDto = OrderMapper.INSTANCE.toDTO(mockOrder);

      when(orderFacade.get(1L)).thenReturn(mockOrder);

      mockMvc.perform(get("/orders/1")
              .with(user("test_client").roles("USER")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenDifferentClient_ShouldDenyAccess() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      mockOrder.setClientName("another_client");

      when(orderFacade.get(1L)).thenReturn(mockOrder);

      mockMvc.perform(get("/orders/1")
              .with(user("client").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class AdminOperationsTests {
    @Test
    void whenAdminSetsStatus_ShouldAllowAccess() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      OrderDTO expectedDto = OrderMapper.INSTANCE.toDTO(mockOrder);

      when(orderFacade.get(1L)).thenReturn(mockOrder);
      when(orderFacade.setOrderStatus(1L, OrderStatus.COMPLETED)).thenReturn(mockOrder);

      mockMvc.perform(post("/orders/setOrderStatus")
              .param("id", "1")
              .param("status", "COMPLETED")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
    }

    @Test
    void whenUserSetsStatus_ShouldDenyAccess() throws Exception {
      mockMvc.perform(post("/orders/setOrderStatus")
              .param("id", "1")
              .param("status", "COMPLETED")
              .with(user("client").roles("USER")))
          .andExpect(status().isForbidden());
    }

    @Test
    void whenAdminGetsOrders_ShouldAllowAccess() throws Exception {
      List<Order> mockOrders = List.of(
          TestUtil.createTestOrder(1L),
          TestUtil.createTestOrder(2L)
      );
      List<OrderDTO> expectedList = OrderMapper.INSTANCE.toListDTO(mockOrders);

      when(orderFacade.getAll(OrderSort.ID)).thenReturn(mockOrders);

      mockMvc.perform(get("/orders")
              .param("sort", "ID")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }

    @Test
    void whenUserGetsOrders_ShouldDenyAccess() throws Exception {
      mockMvc.perform(get("/orders")
              .param("sort", "ID")
              .with(user("client").roles("USER")))
          .andExpect(status().isForbidden());
    }
  }

  @Nested
  class ImportExportTests {
    @Test
    void whenAdminImportsOrder_ShouldAllowAccess() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      OrderDTO expectedDto = OrderMapper.INSTANCE.toDTO(mockOrder);

      try (MockedStatic<ImportController> importMock = Mockito.mockStatic(ImportController.class)) {
        importMock.when(() -> ImportController.findItemInFile(eq(1L), anyString(), any()))
            .thenReturn(mockOrder);

        when(orderFacade.importOrder(any(Order.class))).thenReturn(mockOrder);

        mockMvc.perform(put("/orders/import/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
      }
    }

    @Test
    void whenAdminExportsOrder_ShouldAllowAccess() throws Exception {
      Order mockOrder = TestUtil.createTestOrder(1L);
      OrderDTO expectedDto = OrderMapper.INSTANCE.toDTO(mockOrder);

      try (MockedStatic<ExportController> exportMock = Mockito.mockStatic(ExportController.class)) {
        when(orderFacade.get(1L)).thenReturn(mockOrder);
        exportMock.when(() -> ExportController.exportItemToFile(any(), anyString(), anyString()))
            .thenAnswer(inv -> null);

        mockMvc.perform(put("/orders/export/1")
                .with(user("admin").roles("ADMIN")))
            .andExpect(status().isOk())
            .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedDto)));
      }
    }
  }

  @Nested
  class ReportsTests {
    @Test
    void whenAdminGetsCompletedOrders_ShouldAllowAccess() throws Exception {
      List<Order> mockOrders = List.of(
          TestUtil.createTestOrder(1L),
          TestUtil.createTestOrder(2L)
      );
      List<OrderDTO> expectedList = OrderMapper.INSTANCE.toListDTO(mockOrders);

      when(orderFacade.getCompleted(any(), any(), any())).thenReturn(mockOrders);

      mockMvc.perform(get("/orders/completed")
              .param("sort", "ID")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().json(TestUtil.objectMapper.writeValueAsString(expectedList)));
    }

    @Test
    void whenAdminGetsCountCompleted_ShouldAllowAccess() throws Exception {
      when(orderFacade.getCountCompletedOrders(any(), any())).thenReturn(5L);

      mockMvc.perform(get("/orders/countCompletedOrders")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().string("5"));
    }

    @Test
    void whenAdminGetsEarnedSum_ShouldAllowAccess() throws Exception {
      when(orderFacade.getEarnedSum(any(), any())).thenReturn(1000.0);

      mockMvc.perform(get("/orders/earnedSum")
              .with(user("admin").roles("ADMIN")))
          .andExpect(status().isOk())
          .andExpect(content().string("1000.0"));
    }
  }
}