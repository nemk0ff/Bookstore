package ru.bookstore.constants;

public abstract class FileConstants {
  public static final String BOOK_HEADER =
      "id;name;author;publicationYear;amount;price;lastDeliveredDate;lastSaleDate;status";
  public static final String ORDER_HEADER =
      "id;clientName;price;status;orderDate;completeDate;"
          + "book1;amount1;book2;amount2;...;bookN;amountN";
  public static final String REQUEST_HEADER = "id;bookId;amount;status";

  public static final String IMPORT_BOOK_PATH =
      "4/src/main/resources/io/import/importBooks.csv";
  public static final String EXPORT_BOOK_PATH =
      "4/src/main/resources/io/export/exportBooks.csv";

  public static final String IMPORT_ORDER_PATH =
      "4/src/main/resources/io/import/importOrders.csv";
  public static final String EXPORT_ORDER_PATH =
      "4/src/main/resources/io/export/exportOrders.csv";

  public static final String IMPORT_REQUEST_PATH =
      "4/src/main/resources/io/import/importRequests.csv";
  public static final String EXPORT_REQUEST_PATH =
      "4/src/main/resources/io/export/exportRequests.csv";
}