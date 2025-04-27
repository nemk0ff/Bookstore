USE bookstore;

CREATE TABLE library (
    book_id BIGINT NOT NULL AUTO_INCREMENT,
    name VARCHAR(255) NOT NULL,
    author VARCHAR(255) NOT NULL,
    publicationDate INT,
    amount INT NOT NULL,
    price DECIMAL(10, 2) NOT NULL,
    lastDeliveredDate TIMESTAMP NOT NULL,
    lastSaleDate TIMESTAMP,
    status VARCHAR(20) NOT NULL,
    PRIMARY KEY (book_id)
);

CREATE TABLE orders (
    order_id BIGINT NOT NULL AUTO_INCREMENT,
    status VARCHAR(10) NOT NULL,
    price DECIMAL(10,2) NOT NULL,
    orderDate TIMESTAMP NOT NULL,
    completeDate TIMESTAMP,
    clientName VARCHAR(255) NOT NULL,
    PRIMARY KEY (order_id)
);

CREATE TABLE requests (
    request_id BIGINT NOT NULL AUTO_INCREMENT,
    book_id BIGINT NOT NULL,
    amount INT NOT NULL,
    status VARCHAR(10) NOT NULL,
    PRIMARY KEY(request_id),
    FOREIGN KEY (book_id) REFERENCES library(book_id)
);

CREATE TABLE ordered_books (
    order_id BIGINT NOT NULL,
    book_id BIGINT NOT NULL,
    amount INT NOT NULL,
    PRIMARY KEY (book_id, order_id),
    FOREIGN KEY (book_id) REFERENCES library(book_id),
    FOREIGN KEY (order_id) REFERENCES orders(order_id)
);

CREATE TABLE users (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    username VARCHAR(255) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    role ENUM('ADMIN', 'USER') NOT NULL
);

