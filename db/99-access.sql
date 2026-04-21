-- Ensure root can connect via TCP from any host with mysql_native_password
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY '123456';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY '123456';
FLUSH PRIVILEGES;
