package ru.bookstore.controllers.impl.importexport;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import ru.bookstore.exceptions.ExportException;
import ru.bookstore.model.Item;

@Slf4j
public class ExportController {
  public static <T extends Item> void exportAll(List<T> items, String exportPath, String header) {
    log.info("Выполняется экспорт объектов в файл {}...", exportPath);
    items.forEach(item -> exportItemToFile(item, exportPath, header));
    log.info("Выполнен экспорт объектов в файл {}.", exportPath);
  }

  public static <T extends Item> void exportItemToFile(T item, String exportPath, String header) {
    log.info("Выполняется экспорт объекта: {}...", item);
    String exportString = item.toString();
    List<String> newFileStrings = new ArrayList<>();
    newFileStrings.add(header);

    String[] exportParts = exportString.split(",");
    long exportId = Long.parseLong(exportParts[0].trim());

    boolean isUpdated = false;

    try (BufferedReader reader = new BufferedReader(new FileReader(exportPath))) {
      reader.readLine();
      String line;
      while ((line = reader.readLine()) != null) {
        String[] parts = line.split(",");
        long id = Long.parseLong(parts[0].trim());
        if (id == exportId) {
          newFileStrings.add(exportString);
          isUpdated = true;
        } else {
          newFileStrings.add(line);
        }
      }
    } catch (IOException e) {
      throw new ExportException("При экспорте " + item + "возникла ошибка: " + e.getMessage(), e);
    }

    if (!isUpdated) {
      newFileStrings.add(exportString);
    }

    try (BufferedWriter writer = new BufferedWriter(new FileWriter(exportPath))) {
      for (String line : newFileStrings) {
        writer.write(line);
        writer.newLine();
      }
    } catch (IOException e) {
      throw new ExportException("При экспорте " + item + "возникла ошибка: " + e.getMessage(), e);
    }
    log.info("Экспорт выполнен успешно: {}", item);
  }
}
