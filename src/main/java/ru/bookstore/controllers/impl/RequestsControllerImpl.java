package ru.bookstore.controllers.impl;

import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.constants.FileConstants;
import ru.bookstore.controllers.RequestsController;
import ru.bookstore.controllers.impl.importexport.ExportController;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.dto.mappers.BookMapper;
import ru.bookstore.dto.mappers.RequestMapper;
import ru.bookstore.facade.RequestFacade;
import ru.bookstore.model.impl.Request;
import ru.bookstore.sorting.RequestSort;

@Slf4j
@AllArgsConstructor
@RestController
@RequestMapping("/requests")
@Tag(name = "Контроллер запросов на книги", description = "API для управления запросами на книги")
public class RequestsControllerImpl implements RequestsController {
  private final RequestFacade requestFacade;
  private final ImportController importController;

  @PostMapping
  public ResponseEntity<?> createRequest(@RequestParam("bookId") Long bookId,
                                         @RequestParam("amount") Integer amount) {
    Long requestId = requestFacade.add(bookId, amount);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(requestFacade.get(requestId)));
  }

  @GetMapping
  @Override
  public ResponseEntity<?> getRequests(@RequestParam("sort") RequestSort requestSort) {
    return ResponseEntity.ok(requestFacade.getRequests(requestSort).entrySet().stream()
        .collect(Collectors.toMap(
            entry -> BookMapper.INSTANCE.toDTO(entry.getKey()),
            Map.Entry::getValue,
            (e1, e2) -> e1,
            LinkedHashMap::new
        )));
  }

  @GetMapping("getAll")
  @Override
  public ResponseEntity<?> getAllRequests() {
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(requestFacade.getAllRequests()));
  }

  @PutMapping("export/{id}")
  @Override
  public ResponseEntity<?> exportRequest(@PathVariable("id") Long id) {
    Request exportRequest = requestFacade.get(id);
    ExportController.exportItemToFile(exportRequest, FileConstants.EXPORT_REQUEST_PATH,
        FileConstants.REQUEST_HEADER);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(exportRequest));
  }

  @PutMapping("import/{id}")
  @Override
  public ResponseEntity<?> importRequest(@PathVariable("id") Long id) {
    Request findRequest = ImportController.findItemInFile(id, FileConstants.IMPORT_REQUEST_PATH,
        importController::requestParser);
    requestFacade.importRequest(findRequest);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(findRequest));
  }

  @PutMapping("import")
  @Override
  public ResponseEntity<?> importAll() {
    List<Request> importedRequests = ImportController.importAllItemsFromFile(
        FileConstants.IMPORT_REQUEST_PATH, importController::requestParser);
    importedRequests.forEach(requestFacade::importRequest);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(importedRequests));
  }

  @PutMapping("export")
  @Override
  public ResponseEntity<?> exportAll() {
    List<Request> exportRequests = requestFacade.getAllRequests();
    ExportController.exportAll(exportRequests,
        FileConstants.EXPORT_REQUEST_PATH, FileConstants.REQUEST_HEADER);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(exportRequests));
  }
}
