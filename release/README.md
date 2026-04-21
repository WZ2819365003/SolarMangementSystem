# PVMS 光伏管理系统 — 部署手册 (v2.2.0)

> **交付物**：`pvms-images-2.2.0-arm64.tar.gz` + `docker-compose.yml` + 本 README
>
> **架构**：linux/arm64（适配 Apple Silicon / ARM 服务器 / 鲲鹏）
>
> **技术栈**：Vue 2 + Element UI + Spring Boot 3 + MyBatis + MySQL 8

---

## 1. 运行环境要求

| 组件 | 版本 | 备注 |
|---|---|---|
| 操作系统 | Linux aarch64（CentOS/Ubuntu/麒麟均可） | 需 ARM64 架构 |
| Docker Engine | ≥ 24.0 | `docker info` 应显示 `Server Version ≥ 24` |
| Docker Compose | ≥ 2.20（内置于 Docker Desktop / 可单独安装） | `docker compose version` |
| 硬件 | 4 vCPU / 4 GB RAM / 10 GB 磁盘 | 建议 |
| 出站网络 | 无需公网（镜像已离线打包） | 仅部署阶段需要 |
| 端口 | **6618**（前端）、**8091**（后端）、**3307**（数据库） | 防火墙需放行 |

## 2. 部署步骤

### 2.1 上传交付物到目标主机

将以下两个文件上传到同一目录（例如 `/opt/pvms/`）：

```
/opt/pvms/
├── pvms-images-2.2.0-arm64.tar.gz
└── docker-compose.yml
```

### 2.2 加载镜像

```bash
cd /opt/pvms
docker load -i pvms-images-2.2.0-arm64.tar.gz
```

加载完成后应看到：

```
Loaded image: pvms-mysql:2.2.0
Loaded image: pvms-backend:2.2.0
Loaded image: pvms-frontend:2.2.0
```

验证：

```bash
docker images | grep pvms
```

### 2.3 启动服务

```bash
docker compose up -d
```

首次启动 MySQL 需要 30-60 秒初始化数据库（schema + 种子数据）。观察状态：

```bash
docker compose ps
docker compose logs -f backend   # Ctrl+C 退出
```

直到 `pvms-mysql` 显示 `healthy`、`pvms-backend` 日志出现 `Started PvmsBackendApplication`。

### 2.4 验证

| 检查项 | 命令 / 操作 | 期望结果 |
|---|---|---|
| 数据库连通 | `docker exec pvms-mysql mysql -uroot -p123456 -e "SELECT COUNT(*) FROM pvms.sa_station"` | 16 |
| 后端健康 | `curl -s http://localhost:8091/api/pvms/dashboard/stations-geo \| head -c 80` | 返回 JSON `code:0` |
| 前端页面 | 浏览器访问 `http://<服务器 IP>:6618/pvms/` | 加载综合监控中心 |
| 地图功能 | 打开首页检查"GIS 电站总览"卡 | 高德地图加载 + 电站标记点 |

## 3. 常用运维命令

```bash
# 查看运行状态
docker compose ps

# 查看实时日志
docker compose logs -f backend
docker compose logs -f frontend
docker compose logs -f mysql

# 重启单个服务
docker compose restart backend

# 停止
docker compose stop

# 完全下线（保留数据库数据）
docker compose down

# 完全下线（同时清除数据库数据卷，慎用）
docker compose down -v

# 升级到新版本
docker load -i pvms-images-NEW.tar.gz
# 修改 docker-compose.yml 中的镜像 tag
docker compose up -d
```

## 4. 端口与访问

| 服务 | 宿主机端口 | 容器端口 | 说明 |
|---|---|---|---|
| Frontend (Nginx) | 6618 | 80 | 浏览器访问入口 |
| Backend (Spring Boot) | 8091 | 8091 | 被前端 Nginx 反向代理 |
| MySQL 8 | 3307 | 3306 | 运维 DBA 诊断用（可关闭） |

**最终用户只需访问**：`http://<主机 IP>:6618/pvms/`

## 5. 配置调整

### 5.1 修改数据库密码

编辑 `docker-compose.yml`，三处同步改：
- `services.mysql.environment.MYSQL_ROOT_PASSWORD`
- `services.backend.environment.SPRING_DATASOURCE_PASSWORD`
- `services.mysql.healthcheck.test` 中的 `-p` 参数

之后 `docker compose down -v && docker compose up -d`（会清空现有数据）。

### 5.2 修改前端 API 代理目标

默认 Nginx 把 `/api/` 代理到 `http://backend:8091`。如需指向外部后端，编辑 `pvms-frontend` 镜像内的 `/etc/nginx/conf.d/default.conf` 或通过 bind-mount 覆盖。

### 5.3 修改高德地图 Key

当前 Key 已打包在前端镜像的 `dist/` 产物中。如需更换：联系开发团队重新构建镜像并重新交付。

## 6. 持久化与备份

数据库数据挂载在 Docker 命名卷 `pvms-mysql-data`。

```bash
# 备份
docker run --rm -v pvms-mysql-data:/var/lib/mysql -v $(pwd):/backup alpine \
  tar czf /backup/pvms-db-$(date +%F).tar.gz /var/lib/mysql

# 恢复
docker compose down
docker run --rm -v pvms-mysql-data:/var/lib/mysql -v $(pwd):/backup alpine \
  sh -c "rm -rf /var/lib/mysql/* && tar xzf /backup/pvms-db-YYYY-MM-DD.tar.gz -C /"
docker compose up -d
```

## 7. 故障排查

| 症状 | 可能原因 | 处理 |
|---|---|---|
| `docker compose up` 报 no matching manifest | 宿主机是 x86_64 而不是 arm64 | 确认 `uname -m` 输出为 `aarch64` |
| backend 启动失败，日志 `Access denied` | MySQL 未就绪就启动了 backend | `docker compose restart backend`；检查 mysql 健康 |
| 前端访问 502 | backend 未启动 / 端口冲突 | `docker compose ps`；确保 8091 端口未被占用 |
| 地图无法加载 | 高德 Key 域名未授权 | 在高德开放平台把部署域名/IP 加到白名单 |
| 端口 3307/8091/6618 被占 | 宿主机有其他服务 | 修改 `docker-compose.yml` 的端口映射左半 |

## 8. 版本信息

- **v2.2.0** (本版本)
  - 新增：`BaseFilterBar` 统一筛选组件，整合 4 个模块的筛选框
  - 新增：`stationContext` 全局电站聚焦状态，跨页面保持选中
  - 增强：种子数据波动化，使 KPI/曲线/告警可视化更有层次
  - 增强：地图 API Key 更新
- **v2.1.0** 文档重构
- **v2.0.0** 后端迁移到 MyBatis + MySQL

## 9. 联系与支持

部署异常、数据问题或二次开发需求，请联系 PVMS 研发团队。
