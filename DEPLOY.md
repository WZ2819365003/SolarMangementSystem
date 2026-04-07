# PVMS Docker 部署手册

光伏智能运营管理系统（PVMS）v2.0.0 容器化部署指南。

---

## 目录

1. [前置条件](#1-前置条件)
2. [目录结构](#2-目录结构)
3. [部署步骤](#3-部署步骤)
4. [环境变量说明](#4-环境变量说明)
5. [端口说明](#5-端口说明)
6. [常见问题](#6-常见问题)

---

## 1. 前置条件

| 工具 | 最低版本 | 说明 |
|------|----------|------|
| Docker | 20.10+ | 容器运行时 |
| Docker Compose | 2.0+ | 多容器编排（`docker compose` 命令） |

> **前端说明**：由于 `hs-dtk-ui` 依赖引用了宿主机本地路径，前端镜像直接使用项目内预构建好的 `frontend/dist/`，**无需在容器内执行 `npm install`**。
> 若 `frontend/dist/` 不存在或需要重新构建，请先在宿主机执行：
> ```bash
> cd frontend
> npm install
> npm run build
> ```

---

## 2. 目录结构

```
光伏管理系统/
├── backend/                        # Spring Boot 源码
│   └── src/main/resources/
│       ├── schema.sql              # 建表 DDL（自动挂载到 MySQL 初始化）
│       └── mysql-data.sql          # 初始数据（自动挂载到 MySQL 初始化）
├── frontend/
│   └── dist/                       # 预构建前端产物（必须存在）
├── docker/
│   ├── Dockerfile.frontend         # 前端镜像：nginx 托管 dist/
│   ├── Dockerfile.backend          # 后端镜像：Maven 多阶段构建
│   ├── docker-compose.yml          # 三服务编排
│   └── nginx.conf                  # nginx 配置（/pvms/ 静态 + /api/ 代理）
└── DEPLOY.md                       # 本文件
```

---

## 3. 部署步骤

### 3.1 克隆项目

```bash
git clone git@github.com:WZ2819365003/SolarMangementSystem.git
cd SolarMangementSystem
```

### 3.2 检查前端产物

```bash
ls frontend/dist/
# 应看到 index.html 和 static/ 目录
```

若 `dist/` 不存在，在宿主机构建（需要 Node 14.16.0 及 hs-dtk-ui 依赖可访问）：

```bash
cd frontend && npm install && npm run build && cd ..
```

### 3.3 启动全部服务

在项目根目录（`光伏管理系统/`）下执行：

```bash
docker compose -f docker/docker-compose.yml up -d --build
```

首次启动会：
1. 拉取 `mysql:8.0`、`nginx:1.27-alpine`、`eclipse-temurin:21` 基础镜像（需联网）
2. Maven 多阶段构建后端 jar（约 3-5 分钟，取决于网速）
3. MySQL 容器初始化完成后，后端才会启动（healthcheck 保证顺序）

### 3.4 验证服务

```bash
# 查看容器状态（三个容器均应为 Up）
docker compose -f docker/docker-compose.yml ps

# 查看后端日志
docker logs pvms-backend -f

# 访问前端
# 浏览器打开 http://localhost/pvms/
# 或 http://<服务器IP>/pvms/
```

### 3.5 停止服务

```bash
# 停止但保留数据卷
docker compose -f docker/docker-compose.yml down

# 停止并清除数据卷（会丢失数据库数据）
docker compose -f docker/docker-compose.yml down -v
```

### 3.6 重新构建镜像

代码有更新时：

```bash
docker compose -f docker/docker-compose.yml up -d --build --force-recreate
```

---

## 4. 环境变量说明

后端服务支持通过环境变量覆盖 `application.yml` 中的配置：

| 环境变量 | 默认值 | 说明 |
|----------|--------|------|
| `SPRING_DATASOURCE_URL` | `jdbc:mysql://pvms-mysql:3306/pvms?...` | 数据库连接地址（容器内已配置为服务名） |
| `SPRING_DATASOURCE_USERNAME` | `root` | 数据库用户名 |
| `SPRING_DATASOURCE_PASSWORD` | `123456` | 数据库密码 |

**生产环境请修改密码**，在 `docker/docker-compose.yml` 中调整：

```yaml
pvms-mysql:
  environment:
    MYSQL_ROOT_PASSWORD: "your-strong-password"

pvms-backend:
  environment:
    SPRING_DATASOURCE_PASSWORD: "your-strong-password"
```

---

## 5. 端口说明

| 容器 | 容器内端口 | 宿主机映射端口 | 用途 |
|------|-----------|--------------|------|
| pvms-frontend | 80 | 80 | 前端页面 + API 代理 |
| pvms-backend | 8091 | 8091 | Spring Boot REST API |
| pvms-mysql | 3306 | 3306 | MySQL 数据库 |

若宿主机 80 端口被占用，修改 `docker-compose.yml`：

```yaml
pvms-frontend:
  ports:
    - "8080:80"   # 改为 8080
```

---

## 6. 常见问题

### Q: 后端无法连接数据库，日志报 `Communications link failure`

MySQL 容器可能还未完成初始化。`docker-compose.yml` 已配置 `healthcheck`，正常情况下后端会等待 MySQL 就绪。
如仍报错，可手动重启后端：

```bash
docker restart pvms-backend
```

### Q: 访问 `/pvms/` 返回 404

确认前端镜像构建时 `frontend/dist/` 存在且非空：

```bash
docker exec pvms-frontend ls /usr/share/nginx/html/pvms/
```

### Q: 页面地图不显示（空白区域）

高德地图 Key 通过前端构建时的 `.env` 文件注入。需要在宿主机构建时配置：

```bash
# frontend/.env.production
VUE_APP_AMAP_KEY=你的高德Web端Key
```

然后重新 `npm run build` 并重建镜像。

### Q: MySQL 初始化失败，中文数据乱码

已在 `docker-compose.yml` 中配置 `--character-set-server=utf8mb4`。
若数据已存在且乱码，清除数据卷重新初始化：

```bash
docker compose -f docker/docker-compose.yml down -v
docker compose -f docker/docker-compose.yml up -d --build
```

### Q: 后端 Maven 构建太慢

首次构建需下载所有依赖（约 200MB+）。后续构建会利用 Docker 层缓存，仅在 `pom.xml` 变更时重新下载。

可在国内服务器配置 Maven 镜像加速，在 `Dockerfile.backend` 的构建阶段添加：

```dockerfile
COPY docker/settings.xml /root/.m2/settings.xml
```

`docker/settings.xml` 内配置阿里云镜像：

```xml
<mirrors>
  <mirror>
    <id>aliyun</id>
    <mirrorOf>central</mirrorOf>
    <url>https://maven.aliyun.com/repository/central</url>
  </mirror>
</mirrors>
```
