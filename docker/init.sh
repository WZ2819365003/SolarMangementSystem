#!/bin/bash
# 使用 utf8mb4 字符集执行初始化 SQL，避免中文乱码
set -e

echo "[init] Importing schema..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --default-character-set=utf8mb4 \
  "${MYSQL_DATABASE}" < /sql/01_schema.sql

echo "[init] Importing data..."
mysql -uroot -p"${MYSQL_ROOT_PASSWORD}" \
  --default-character-set=utf8mb4 \
  "${MYSQL_DATABASE}" < /sql/02_data.sql

echo "[init] Done."
