# ============================================================
# PVMS — MySQL 8 with embedded schema + seed data (ARM64 friendly)
# ============================================================
# Builds a self-contained database image: starting the container
# creates the `pvms` database and loads the schema + varied seed data.
# ============================================================
FROM mysql:8.0

# Runtime environment — operators override MYSQL_ROOT_PASSWORD via compose
ENV MYSQL_DATABASE=pvms \
    MYSQL_ROOT_PASSWORD=123456 \
    MYSQL_CHARSET=utf8mb4 \
    MYSQL_COLLATION=utf8mb4_unicode_ci \
    LANG=C.UTF-8

# MySQL auto-runs every *.sql in this directory (sorted alphabetically)
# 01 -> schema, 02 -> varied seed data
COPY 01-schema.sql /docker-entrypoint-initdb.d/01-schema.sql
COPY 02-data.sql   /docker-entrypoint-initdb.d/02-data.sql
COPY 99-access.sql /docker-entrypoint-initdb.d/99-access.sql

# Persist auth plugin so Spring Boot can connect without
# caching_sha2_password surprises
COPY my.cnf /etc/mysql/conf.d/pvms.cnf

EXPOSE 3306

HEALTHCHECK --interval=10s --timeout=5s --retries=10 \
    CMD mysqladmin ping -h localhost -uroot -p$MYSQL_ROOT_PASSWORD || exit 1
