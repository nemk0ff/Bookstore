package ru.bookstore.controllers.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.constants.FileConstants;
import ru.bookstore.controllers.OrdersController;
import ru.bookstore.controllers.impl.importexport.ExportController;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.dto.OrderDTO;
import ru.bookstore.dto.mappers.OrderMapper;
import ru.bookstore.facade.OrderFacade;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.impl.Order;
import ru.bookstore.security.SecurityAccessUtils;
import ru.bookstore.sorting.OrderSort;

@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/orders")
@Tag(name = "Контроллер заказов", description = "API для управления заказами магазина")
public class OrdersControllerImpl implements OrdersController {
  private final OrderFacade orderFacade;

  @Operation(
      summary = "Создать новый заказ",
      description = "Создает новый заказ для указанного клиента",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Заказ успешно создан",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные данные заказа",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PostMapping
  @Override
  public ResponseEntity<?> createOrder(@RequestBody @Valid OrderDTO orderDTO) {
    SecurityAccessUtils.checkAccessDenied(SecurityContextHolder.getContext().getAuthentication(),
        "Вы можете создать заказ только на своё имя", orderDTO.getClientName());
    return ResponseEntity.ok(OrderMapper.INSTANCE
        .toDTO(orderFacade.createOrder(orderDTO.getBooks(),
            orderDTO.getClientName(),
            LocalDateTime.now())));
  }

  @Operation(
      summary = "Отменить заказ",
      description = "Отменяет заказ по его идентификатору",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Заказ успешно отменен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class)
              )),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )),
          @ApiResponse(
              responseCode = "404",
              description = "Заказ не найден",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              ))}
  )
  @PostMapping(value = "cancelOrder/{id}")
  @Override
  public ResponseEntity<?> cancelOrder(
      @Parameter(description = "ID заказа", required = true, example = "1")
      @PathVariable("id") Long id) {
    SecurityAccessUtils.checkAccessDenied(SecurityContextHolder.getContext().getAuthentication(),
        "Вы не можете отменить чужой заказ", orderFacade.get(id).getClientName());

    orderFacade.cancelOrder(id);
    return ResponseEntity.ok(orderFacade.get(id));
  }

  @Operation(
      summary = "Получить детали заказа",
      description = "Возвращает детальную информацию о заказе",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Детали заказа",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Заказ не найден",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @GetMapping("{id}")
  @Override
  public ResponseEntity<?> showOrderDetails(
      @Parameter(description = "ID заказа", required = true, example = "1")
      @PathVariable("id") Long id) {
    SecurityAccessUtils.checkAccessDenied(SecurityContextHolder.getContext().getAuthentication(),
        "Вы не можете увидеть детали чужого заказа", orderFacade.get(id).getClientName());

    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(orderFacade.get(id)));
  }

  @Operation(
      summary = "Изменить статус заказа",
      description = "Обновляет статус заказа (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Статус заказа обновлен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректный статус",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )),
          @ApiResponse(
              responseCode = "404",
              description = "Заказ не найден",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PostMapping("/setOrderStatus")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> setOrderStatus(
      @Parameter(description = "ID заказа", required = true, example = "1")
      @RequestParam("id") Long id,
      @Parameter(description = "Новый статус заказа", required = true,
          schema = @Schema(implementation = OrderStatus.class))
      @RequestParam("status") OrderStatus newStatus) {
    orderFacade.setOrderStatus(id, newStatus);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(orderFacade.get(id)));
  }

  @Operation(
      summary = "Получить все заказы",
      description = "Возвращает список всех заказов с сортировкой (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список заказов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class))
              )),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getOrders(
      @Parameter(description = "Параметр сортировки", required = true,
          schema = @Schema(implementation = OrderSort.class))
      @RequestParam("sort") OrderSort orderSort) {
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(orderFacade.getAll(orderSort)));
  }

  @Operation(
      summary = "Получить выполненные заказы",
      description = "Возвращает список выполненных заказов за указанный период (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список выполненных заказов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @GetMapping("completed")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getCompleted(
      @Parameter(description = "Параметр сортировки", required = true,
          schema = @Schema(implementation = OrderSort.class))
      @RequestParam("sort") OrderSort orderSort,
      @Parameter(description = "Начальная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "01.01.2023 00:00:00")
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @Parameter(description = "Конечная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "31.12.2023 23:59:59")
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(OrderMapper.INSTANCE
        .toListDTO(orderFacade.getCompleted(orderSort, begin, end)));
  }

  @Operation(
      summary = "Количество выполненных заказов",
      description = "Возвращает количество выполненных заказов за период (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Количество заказов",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(type = "integer", example = "42"))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @GetMapping("/countCompletedOrders")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getCountCompletedOrders(
      @Parameter(description = "Начальная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "01.01.2023 00:00:00")
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @Parameter(description = "Конечная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "31.12.2023 23:59:59")
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(orderFacade.getCountCompletedOrders(begin, end));
  }

  @Operation(
      summary = "Сумма выручки",
      description = "Возвращает сумму выручки за период (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Сумма выручки",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(type = "number", format = "double", example = "12500.50"))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @GetMapping("/earnedSum")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getEarnedSum(
      @Parameter(description = "Начальная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "01.01.2023 00:00:00")
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @Parameter(description = "Конечная дата периода (формат: dd.MM.yyyy HH:mm:ss)", example = "31.12.2023 23:59:59")
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(orderFacade.getEarnedSum(begin, end));
  }

  @Operation(
      summary = "Импорт всех заказов",
      description = "Импортирует все заказы из файла (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список импортированных заказов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ошибка импорта",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @PutMapping("/import")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importAll() {
    List<Order> importedOrders = ImportController.importAllItemsFromFile(
        FileConstants.IMPORT_ORDER_PATH, ImportController::orderParser);
    importedOrders.forEach(orderFacade::importOrder);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(importedOrders));
  }

  @Operation(
      summary = "Экспорт всех заказов",
      description = "Экспортирует все заказы в файл (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список экспортированных заказов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = OrderDTO.class)))
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          ),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка экспорта",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @PutMapping("/export")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportAll() {
    List<Order> exportOrders = orderFacade.getAll(OrderSort.ID);
    ExportController.exportAll(exportOrders,
        FileConstants.EXPORT_ORDER_PATH, FileConstants.ORDER_HEADER);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(exportOrders));
  }

  @Operation(
      summary = "Импорт конкретного заказа",
      description = "Импортирует заказ по ID из файла (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Импортированный заказ",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ошибка импорта",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Заказ не найден в файле",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @PutMapping("/import/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importOrder(@PathVariable("id") Long id) {
    Order findOrder = ImportController.findItemInFile(id, FileConstants.IMPORT_ORDER_PATH,
        ImportController::orderParser);
    orderFacade.importOrder(findOrder);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(findOrder));
  }

  @Operation(
      summary = "Экспортировать заказ",
      description = "Экспортирует заказ в файл (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Заказ успешно экспортирован",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = OrderDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "404",
              description = "Заказ не найден",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка экспорта",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PutMapping("/export/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportOrder(
      @Parameter(description = "ID заказа для экспорта", required = true, example = "1")
      @PathVariable("id") Long id) {
    Order exportOrder = orderFacade.get(id);
    ExportController.exportItemToFile(exportOrder,
        FileConstants.EXPORT_ORDER_PATH, FileConstants.ORDER_HEADER);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(exportOrder));
  }
}