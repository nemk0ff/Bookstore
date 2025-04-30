package ru.bookstore.controllers.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
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
import ru.bookstore.dto.RequestDTO;
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

  @Operation(
      summary = "Создать запрос на книгу",
      description = "Создает новый запрос на указанную книгу с заданным количеством",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Запрос успешно создан",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = RequestDTO.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные параметры запроса",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @PostMapping
  public ResponseEntity<?> createRequest(
      @Parameter(description = "ID книги", required = true, example = "1")
      @RequestParam("bookId") Long bookId,
      @Parameter(description = "Количество запрашиваемых экземпляров", required = true, example = "5")
      @RequestParam("amount") Integer amount) {
    Long requestId = requestFacade.add(bookId, amount);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(requestFacade.get(requestId)));
  }

  @Operation(
      summary = "Получить запросы с группировкой по книгам",
      description = "Возвращает запросы сгруппированные по книгам с возможностью сортировки",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список запросов с группировкой по книгам",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(
                      schema = @Schema(
                          implementation = Map.class,
                          description = "Map<BookDTO, List<RequestDTO>>"
                      )
                  )
              )
          )
      }
  )
  @GetMapping
  @Override
  public ResponseEntity<?> getRequests(
      @Parameter(description = "Параметр сортировки", required = true,
          schema = @Schema(implementation = RequestSort.class))
      @RequestParam("sort") RequestSort requestSort) {
    return ResponseEntity.ok(requestFacade.getRequests(requestSort).entrySet().stream()
        .collect(Collectors.toMap(
            entry -> BookMapper.INSTANCE.toDTO(entry.getKey()),
            Map.Entry::getValue,
            (e1, e2) -> e1,
            LinkedHashMap::new
        )));
  }


  @Operation(
      summary = "Получить все запросы",
      description = "Возвращает список всех запросов без группировки",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список всех запросов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = RequestDTO.class))
              ))
      }
  )
  @GetMapping("getAll")
  @Override
  public ResponseEntity<?> getAllRequests() {
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(requestFacade.getAllRequests()));
  }


  @Operation(
      summary = "Экспортировать запрос",
      description = "Экспортирует конкретный запрос в файл (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Запрос успешно экспортирован",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = RequestDTO.class)
              )),
          @ApiResponse(
              responseCode = "404",
              description = "Запрос не найден",
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
  @PutMapping("export/{id}")
  @Override
  public ResponseEntity<?> exportRequest(
      @Parameter(description = "ID запроса для экспорта", required = true, example = "1")
      @PathVariable("id") Long id) {
    Request exportRequest = requestFacade.get(id);
    ExportController.exportItemToFile(exportRequest, FileConstants.EXPORT_REQUEST_PATH,
        FileConstants.REQUEST_HEADER);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(exportRequest));
  }

  @Operation(
      summary = "Импортировать запрос",
      description = "Импортирует запрос из файла по ID (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Запрос успешно импортирован",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = RequestDTO.class))
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
              description = "Запрос не найден в файле",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @PutMapping("import/{id}")
  @Override
  public ResponseEntity<?> importRequest(
      @Parameter(description = "ID запроса для импорта", required = true, example = "1")
      @PathVariable("id") Long id) {
    Request findRequest = ImportController.findItemInFile(id, FileConstants.IMPORT_REQUEST_PATH,
        importController::requestParser);
    requestFacade.importRequest(findRequest);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toDTO(findRequest));
  }

  @Operation(
      summary = "Импортировать все запросы",
      description = "Импортирует все запросы из файла (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список импортированных запросов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = RequestDTO.class)))
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ошибка импорта",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = ProblemDetail.class))
          )
      }
  )
  @PutMapping("import")
  @Override
  public ResponseEntity<?> importAll() {
    List<Request> importedRequests = ImportController.importAllItemsFromFile(
        FileConstants.IMPORT_REQUEST_PATH, importController::requestParser);
    importedRequests.forEach(requestFacade::importRequest);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(importedRequests));
  }

  @Operation(
      summary = "Экспортировать все запросы",
      description = "Экспортирует все запросы в файл (только для ADMIN)",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список всех экспортированных запросов",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = RequestDTO.class)))
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
  @PutMapping("export")
  @Override
  public ResponseEntity<?> exportAll() {
    List<Request> exportRequests = requestFacade.getAllRequests();
    ExportController.exportAll(exportRequests,
        FileConstants.EXPORT_REQUEST_PATH, FileConstants.REQUEST_HEADER);
    return ResponseEntity.ok(RequestMapper.INSTANCE.toListDTO(exportRequests));
  }
}
