package ru.bookstore.controllers.impl.importexport;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import ru.bookstore.exceptions.ImportException;
import ru.bookstore.model.BookStatus;
import ru.bookstore.model.Item;
import ru.bookstore.model.OrderStatus;
import ru.bookstore.model.RequestStatus;
import ru.bookstore.model.impl.Book;
import ru.bookstore.model.impl.Order;
import ru.bookstore.model.impl.Request;
import ru.bookstore.service.BookService;

@Controller
@RequiredArgsConstructor
@Slf4j
public class ImportController {
  private final BookService bookService;

  public static final DateTimeFormatter flexibleDateTimeFormatter = new DateTimeFormatterBuilder()
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss"))
      .appendOptional(DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm"))
      .toFormatter();

  public static Book bookParser(String[] parts) {
    if (parts.length != 9) {
      throw new ImportException("Неверное количество частей в строке: " + parts.length);
    }
    long id = Long.parseLong(parts[0].trim());
    String name = parts[1].trim();
    String author = parts[2].trim();
    int publicationYear = Integer.parseInt(parts[3].trim());
    int amount = Integer.parseInt(parts[4].trim());
    double price = Double.parseDouble(parts[5].trim());
    LocalDateTime lastDeliveredDate = parts[6].trim().equals("null")
        ? null : LocalDateTime.parse(parts[6].trim(), flexibleDateTimeFormatter);
    LocalDateTime lastSaleDate = parts[7].trim().equals("null")
        ? null : LocalDateTime.parse(parts[7].trim(), flexibleDateTimeFormatter);
    BookStatus status = bookStatusFromString(parts[8].trim(), amount);

    return new Book(id, name, author, publicationYear,
        amount, price, lastDeliveredDate, lastSaleDate, status);
  }

  @Transactional(readOnly = true)
  public Request requestParser(String[] parts) {
    if (parts.length != 4) {
      throw new ImportException("Неверное количество частей в строке: " + parts.length);
    }
    long id = Long.parseLong(parts[0].trim());
    long book_id = Long.parseLong(parts[1].trim());
    int amount = Integer.parseInt(parts[2].trim());
    RequestStatus status = RequestStatus.valueOf(parts[3].trim());

    return new Request(id, bookService.get(book_id), amount, status);
  }

  public static Order orderParser(String[] parts) {
    if (parts.length < 7) {
      throw new ImportException("Неверное количество частей в строке: " + parts.length);
    }
    long id = Long.parseLong(parts[0].trim());
    String name = parts[1].trim();
    double price = Double.parseDouble(parts[2].trim());
    OrderStatus status = OrderStatus.valueOf(parts[3].trim());
    LocalDateTime orderDate = parts[4].trim().equals("null")
        ? null : LocalDateTime.parse(parts[4].trim(), flexibleDateTimeFormatter);
    LocalDateTime completeDate = parts[5].trim().equals("null")
        ? null : LocalDateTime.parse(parts[5].trim(), flexibleDateTimeFormatter);

    Map<Long, Integer> books = new HashMap<>();
    for (int i = 6; i < parts.length; i += 2) {
      long bookId = Long.parseLong(parts[i].trim());
      int amount = Integer.parseInt(parts[i + 1].trim());
      books.put(bookId, amount);
    }
    return new Order(id, status, price, orderDate, completeDate, name, books);
  }

  public static <T extends Item> T
  findItemInFile(Long targetBookId, String importPath, Function<String[], T> parser) {
    try (BufferedReader reader = new BufferedReader(new FileReader(importPath))) {
      reader.readLine();

      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        long id = Long.parseLong(parts[0].trim());

        if (id == targetBookId) {
          return parser.apply(parts);
        }
      }
    } catch (IOException e) {
      throw new ImportException("При поиске объекта импорта возникла ошибка", e);
    }
    throw new ImportException("Не удалось найти объект для импорта");
  }

  public static <T extends Item> List<T>
  importAllItemsFromFile(String importPath, Function<String[], T> parser) {
    log.info("Импортируем данные из файла: {}...", importPath);
    List<T> items = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(importPath))) {
      reader.readLine();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        items.add(parser.apply(parts));
      }
    } catch (IOException e) {
      throw new ImportException("Не удалось выполнить импорт", e);
    }
    log.info("Выполнен импорт данных из файла: {}.", importPath);
    return items;
  }

  private static BookStatus bookStatusFromString(String input, int amount) {
    for (BookStatus status : BookStatus.values()) {
      if (status.name().equalsIgnoreCase(input)) {
        return status;
      }
    }
    return amount > 0 ? BookStatus.AVAILABLE : BookStatus.NOT_AVAILABLE;
  }
}
