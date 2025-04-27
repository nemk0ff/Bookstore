#!/bin/bash

echo "Creating and filling in a database..."

# Выполнение SQL-скриптов
mysql -u root -p12345 -h db -P 3306 bookstore < /docker-entrypoint-initdb.d/creating.sql
mysql -u root -p12345 -h db -P 3306 bookstore < /docker-entrypoint-initdb.d/inserting.sql

echo "Database setup complete."
