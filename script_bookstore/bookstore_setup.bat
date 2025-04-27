@echo off

chcp 65001

echo Creating and filling in a database ...

mysql -u root -p -e "source creating.sql; source inserting.sql;"

echo Database setup complete.

pause
