package ru.bookstore.controllers.impl;

import jakarta.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
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
public class OrdersControllerImpl implements OrdersController {
  private final OrderFacade orderFacade;

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

  @PostMapping(value = "cancelOrder/{id}")
  @Override
  public ResponseEntity<?> cancelOrder(@PathVariable("id") Long id) {
    SecurityAccessUtils.checkAccessDenied(SecurityContextHolder.getContext().getAuthentication(),
        "Вы не можете отменить чужой заказ", orderFacade.get(id).getClientName());

    orderFacade.cancelOrder(id);
    return ResponseEntity.ok(orderFacade.get(id));
  }

  @GetMapping("{id}")
  @Override
  public ResponseEntity<?> showOrderDetails(@PathVariable("id") Long id) {
    SecurityAccessUtils.checkAccessDenied(SecurityContextHolder.getContext().getAuthentication(),
        "Вы не можете увидеть детали чужого заказа", orderFacade.get(id).getClientName());

    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(orderFacade.get(id)));
  }

  @PostMapping("/setOrderStatus")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> setOrderStatus(@RequestParam("id") Long id,
                                          @RequestParam("status") OrderStatus newStatus) {
    orderFacade.setOrderStatus(id, newStatus);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(orderFacade.get(id)));
  }

  @GetMapping
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getOrders(@RequestParam("sort") OrderSort orderSort) {
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(orderFacade.getAll(orderSort)));
  }

  @GetMapping("completed")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getCompleted(
      @RequestParam("sort") OrderSort orderSort,
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(OrderMapper.INSTANCE
        .toListDTO(orderFacade.getCompleted(orderSort, begin, end)));
  }

  @GetMapping("/countCompletedOrders")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getCountCompletedOrders(
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(orderFacade.getCountCompletedOrders(begin, end));
  }

  @GetMapping("/earnedSum")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getEarnedSum(
      @RequestParam(value = "begin", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime begin,
      @RequestParam(value = "end", required = false)
      @DateTimeFormat(pattern = "dd.MM.yyyy HH:mm:ss") LocalDateTime end) {
    return ResponseEntity.ok(orderFacade.getEarnedSum(begin, end));
  }

  @PutMapping("/import")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importAll() {
    List<Order> importedOrders = ImportController.importAllItemsFromFile(
        FileConstants.IMPORT_ORDER_PATH, ImportController::orderParser);
    importedOrders.forEach(orderFacade::importOrder);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(importedOrders));
  }

  @PutMapping("/export")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportAll() {
    List<Order> exportOrders = orderFacade.getAll(OrderSort.ID);
    ExportController.exportAll(exportOrders,
        FileConstants.EXPORT_ORDER_PATH, FileConstants.ORDER_HEADER);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toListDTO(exportOrders));
  }

  @PutMapping("/import/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importOrder(@PathVariable("id") Long id) {
    Order findOrder = ImportController.findItemInFile(id, FileConstants.IMPORT_ORDER_PATH,
        ImportController::orderParser);
    orderFacade.importOrder(findOrder);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(findOrder));
  }

  @PutMapping("/export/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportOrder(@PathVariable("id") Long id) {
    Order exportOrder = orderFacade.get(id);
    ExportController.exportItemToFile(exportOrder,
        FileConstants.EXPORT_ORDER_PATH, FileConstants.ORDER_HEADER);
    return ResponseEntity.ok(OrderMapper.INSTANCE.toDTO(exportOrder));
  }
}