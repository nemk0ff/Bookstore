package ru.bookstore.model.impl;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.Objects;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import ru.bookstore.model.BookStatus;
import ru.bookstore.model.Item;

@AllArgsConstructor
@NoArgsConstructor
@Builder
@Setter
@Getter
@Entity
@Table(name = "library")
public class Book implements Item {
  @Id
  @GeneratedValue(strategy = GenerationType.IDENTITY)
  @Column(name = "book_id")
  private Long id;
  @Column(nullable = false)
  private String name;
  @Column(nullable = false)
  private String author;
  @Column(nullable = false)
  private Integer publicationDate;
  @Column(nullable = false)
  private Integer amount;
  @Column(nullable = false)
  private Double price;
  @Setter
  @Column
  private LocalDateTime lastDeliveredDate;
  @Setter
  @Column
  private LocalDateTime lastSaleDate;
  @Enumerated(EnumType.STRING)
  @Column(length = 20, nullable = false)
  private BookStatus status;

  public void setAmount(Integer amount) {
    this.amount = amount;
    if (this.amount > 0) {
      status = BookStatus.AVAILABLE;
    } else {
      this.amount = 0;
      status = BookStatus.NOT_AVAILABLE;
    }
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }
    Book book = (Book) o;
    return Objects.equals(id, book.id);
  }

  @Override
  public int hashCode() {
    return Objects.hash(id);
  }

  @Override
  public String toString() {
    return id + "," + name + "," + author + "," + publicationDate + "," + amount + "," + price
        + "," + lastDeliveredDate + "," + lastSaleDate + "," + status;
  }
}
