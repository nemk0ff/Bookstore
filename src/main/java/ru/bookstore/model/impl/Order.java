package ru.bookstore.model.impl;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bookstore.model.Item;
import ru.bookstore.model.OrderStatus;

@AllArgsConstructor
@NoArgsConstructor
@Setter
@Data
@Getter
@Entity
@Table(name = "orders")
public class Order implements Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "order_id")
  private Long id;
  @Setter
  @Enumerated(EnumType.STRING)
  @Column(nullable = false, length = 10)
  private OrderStatus status;
  @Column(nullable = false)
  private Double price;
  @Column(nullable = false)
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime orderDate;
  @Column
  @Setter
  @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "HH:mm:ss dd-MM-yyyy")
  private LocalDateTime completeDate;
  @Column(nullable = false)
  private String clientName;

  @ElementCollection
  @CollectionTable(name = "ordered_books",
      joinColumns = @JoinColumn(name = "order_id"))
  @MapKeyColumn(name = "book_id")
  @Column(name = "amount")
  private Map<Long, Integer> books;

  public Order(Map<Long, Integer> books, double price, OrderStatus status,
               LocalDateTime orderDate, String clientName) {
    this.books = books;
    this.status = status;
    this.orderDate = orderDate;
    this.clientName = clientName;
    this.price = price;
  }


  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Order order = (Order) o;
    return Objects.equals(id, order.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    StringBuilder resultString = new StringBuilder();
    resultString.append(id).append(",")
        .append(clientName).append(",")
        .append(price).append(",")
        .append(status).append(",")
        .append(orderDate).append(",")
        .append(completeDate);

    for (Map.Entry<Long, Integer> book : books.entrySet()) {
      resultString.append(",").append(book.getKey()).append(",").append(book.getValue());
    }
    return resultString.toString();
  }
}
