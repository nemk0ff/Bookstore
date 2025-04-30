package ru.bookstore.controllers.impl;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import ru.bookstore.constants.FileConstants;
import ru.bookstore.controllers.BooksController;
import ru.bookstore.controllers.impl.importexport.ExportController;
import ru.bookstore.controllers.impl.importexport.ImportController;
import ru.bookstore.dto.BookDTO;
import ru.bookstore.dto.mappers.BookMapper;
import ru.bookstore.facade.BookFacade;
import ru.bookstore.facade.OrderFacade;
import ru.bookstore.model.impl.Book;
import ru.bookstore.sorting.BookSort;

@Slf4j
@RestController
@Validated
@RequiredArgsConstructor
@RequestMapping("/books")
@Tag(name = "Контроллер книг", description = "API для управления книгами в магазине. " +
    "Позволяет просматривать, добавлять, списывать, импортировать и экспортировать книги.")
public class BooksControllerImpl implements BooksController {
  private final BookFacade bookFacade;
  private final OrderFacade orderFacade;

  @Operation(
      summary = "Получить информацию о книге",
      description = "Возвращает полную информацию о книге по её идентификатору",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Информация о книге",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BookDTO.class)
              )),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена",
              content = @Content(
                  schema = @Schema(implementation = ProblemDetail.class)
              )
          )
      }
  )
  @GetMapping("/{id}")
  @Override
  public ResponseEntity<BookDTO> showBookDetails(
      @Parameter(description = "Идентификатор книги", required = true, example = "1")
      @PathVariable("id") Long id) {
    return ResponseEntity.ok(BookMapper.INSTANCE.toDTO(bookFacade.get(id)));
  }

  @Operation(
      summary = "Добавить экземпляры книги",
      description = "Увеличивает количество экземпляров указанной книги на складе. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Обновленная информация о книге",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BookDTO.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные параметры запроса",
              ref = "#/components/responses/BadRequest"),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden"),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена",
              ref = "#/components/responses/NotFound")
      }
  )
  @PatchMapping("/add")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> addBook(
      @Parameter(description = "Идентификатор книги", required = true, example = "1")
      @RequestParam("id") Long id,
      @Parameter(description = "Количество добавляемых экземпляров", required = true, example = "5")
      @RequestParam("amount") Integer amount) {
    Book book = bookFacade.addBook(id, amount, LocalDateTime.now());
    orderFacade.updateOrders();
    return ResponseEntity.ok(BookMapper.INSTANCE.toDTO(book));
  }

  @Operation(
      summary = "Списать экземпляры книги",
      description = "Уменьшает количество экземпляров указанной книги на складе. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Обновленная информация о книге",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BookDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Некорректные параметры запроса",
              ref = "#/components/responses/BadRequest"),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden"),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена",
              ref = "#/components/responses/NotFound")
      }
  )
  @PatchMapping("/writeOff")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> writeOff(
      @Parameter(description = "Идентификатор книги", required = true, example = "1")
      @RequestParam("id") Long id,
      @Parameter(description = "Количество списываемых экземпляров", required = true, example = "3")
      @RequestParam("amount") Integer amount) {
    Book book = bookFacade.writeOff(id, amount, LocalDateTime.now());
    orderFacade.updateOrders();
    return ResponseEntity.ok(BookMapper.INSTANCE.toDTO(book));
  }

  @Operation(
      summary = "Получить список всех книг",
      description = "Возвращает список всех книг с возможностью сортировки",
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список книг",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
              ))
      }
  )
  @GetMapping
  @Override
  public ResponseEntity<?> getBooks(
      @Parameter(description = "Параметр сортировки", required = true,
          schema = @Schema(implementation = BookSort.class), example = "ID")
      @RequestParam("sort") BookSort bookSort) {
    return ResponseEntity.ok(BookMapper.INSTANCE.toListDTO(bookFacade.getAll(bookSort)));
  }

  @Operation(
      summary = "Получить список устаревших книг",
      description = "Возвращает список книг, которые давно не продавались. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список устаревших книг",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
              )),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden")
      }
  )
  @GetMapping("/stale")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> getStaleBooks(
      @Parameter(description = "Параметр сортировки", required = true,
          schema = @Schema(implementation = BookSort.class), example = "LAST_SALE_DATE")
      @RequestParam("sort") BookSort bookSort) {
    return ResponseEntity.ok(BookMapper.INSTANCE.toListDTO(bookFacade.getStale(bookSort)));
  }

  @Operation(
      summary = "Импортировать все книги из файла",
      description = "Импортирует все книги из системного файла. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список импортированных книг",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
              )
          ),
          @ApiResponse(
              responseCode = "400",
              description = "Ошибка при импорте",
              ref = "#/components/responses/ImportError"),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden")
      }
  )
  @PutMapping("/import")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importAll() {
    List<Book> importedBooks = ImportController.importAllItemsFromFile(
        FileConstants.IMPORT_BOOK_PATH, ImportController::bookParser);
    importedBooks.forEach(bookFacade::importBook);
    orderFacade.updateOrders();
    return ResponseEntity.ok(BookMapper.INSTANCE.toListDTO(importedBooks));
  }

  @Operation(
      summary = "Экспортировать все книги в файл",
      description = "Экспортирует все книги в системный файл. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Список экспортированных книг",
              content = @Content(
                  mediaType = "application/json",
                  array = @ArraySchema(schema = @Schema(implementation = BookDTO.class))
              )),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden"),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка при экспорте",
              ref = "#/components/responses/ExportError")
      }
  )
  @PutMapping("/export")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportAll() {
    List<Book> exportBooks = bookFacade.getAll(BookSort.ID);
    ExportController.exportAll(exportBooks,
        FileConstants.EXPORT_BOOK_PATH, FileConstants.BOOK_HEADER);
    return ResponseEntity.ok(BookMapper.INSTANCE.toListDTO(exportBooks));
  }

  @Operation(
      summary = "Импортировать конкретную книгу из файла",
      description = "Импортирует книгу с указанным ID из системного файла. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Импортированная книга",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BookDTO.class)
              )),
          @ApiResponse(
              responseCode = "400",
              description = "Ошибка при импорте",
              ref = "#/components/responses/ImportError"),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden"),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена в файле",
              ref = "#/components/responses/NotFound")
      }
  )
  @PutMapping("/import/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> importBook(
      @Parameter(description = "Идентификатор книги для импорта", required = true, example = "1")
      @PathVariable("id") Long id) {
    Book findBook = ImportController.findItemInFile(id, FileConstants.IMPORT_BOOK_PATH,
        ImportController::bookParser);
    bookFacade.importBook(findBook);
    orderFacade.updateOrders();
    return ResponseEntity.ok(BookMapper.INSTANCE.toDTO(findBook));
  }

  @Operation(
      summary = "Экспортировать конкретную книгу в файл",
      description = "Экспортирует книгу с указанным ID в системный файл. Требует роли ADMIN.",
      security = @SecurityRequirement(name = "bearerAuth"),
      responses = {
          @ApiResponse(
              responseCode = "200",
              description = "Экспортированная книга",
              content = @Content(
                  mediaType = "application/json",
                  schema = @Schema(implementation = BookDTO.class)
              )
          ),
          @ApiResponse(
              responseCode = "403",
              description = "Доступ запрещен",
              ref = "#/components/responses/Forbidden"),
          @ApiResponse(
              responseCode = "404",
              description = "Книга не найдена",
              ref = "#/components/responses/NotFound"),
          @ApiResponse(
              responseCode = "500",
              description = "Ошибка при экспорте",
              ref = "#/components/responses/ExportError")
      }
  )
  @PutMapping("/export/{id}")
  @PreAuthorize("hasRole('ADMIN')")
  @Override
  public ResponseEntity<?> exportBook(
      @Parameter(description = "Идентификатор книги для экспорта", required = true, example = "1")
      @PathVariable("id") Long id) {
    Book exportBook = bookFacade.get(id);
    ExportController.exportItemToFile(exportBook,
        FileConstants.EXPORT_BOOK_PATH, FileConstants.BOOK_HEADER);
    return ResponseEntity.ok(BookMapper.INSTANCE.toDTO(exportBook));
  }
}
