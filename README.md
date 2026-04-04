# 光伏智能运营管理系统 (PVMS)

> **当前版本 v2.0.0** — MyBatis + MySQL 架构，49 项接口测试全部通过

## 技术栈

| 层 | 技术 | 版本 |
|---|---|---|
| 前端框架 | Vue 2 + Vue Router + Vuex | 2.7.x |
| UI 组件 | Element UI + ECharts 5 | 2.15.x / 5.x |
| 微前端 | qiankun 协议兼容 | — |
| 后端框架 | Spring Boot | 3.2.5 |
| 持久层 | MyBatis | 3.0.3 |
| 生产数据库 | MySQL 8 | 8.x |
| 测试数据库 | H2 (MODE=MySQL) | 2.x |
| JDK | Java | 21 |

## 目录结构

```
光伏管理系统/
├── frontend/          # Vue2 微前端子应用
├── backend/           # Spring Boot 后端 (MyBatis + MySQL)
├── docs/              # 集中式项目文档（交接、模块、审计）
├── doc/               # 早期功能设计书（参考用）
└── docker/            # Docker 部署配置
```

## 快速启动

### 1. 准备 MySQL

```sql
CREATE DATABASE IF NOT EXISTS pvms
  CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
```

初始化表结构和种子数据：

```bash
mysql -u root -p123456 --default-character-set=utf8mb4 pvms < backend/src/main/resources/schema.sql
mysql -u root -p123456 --default-character-set=utf8mb4 pvms < backend/src/main/resources/mysql-data.sql
```

### 2. 启动后端

```bash
cd backend
mvn spring-boot:run          # 默认端口 8091
```

验证：访问 `http://localhost:8091/api/system/health`

### 3. 启动前端

```bash
cd frontend
npm install --scripts-prepend-node-path=true
npm run serve                # 默认端口 8080
```

### 4. 运行测试

```bash
cd backend
mvn test                     # 49 项测试，H2 内存库
```

## 业务模块

| 编号 | 模块 | 前端路由 | 后端前缀 | 状态 |
|---|---|---|---|---|
| M01 | 综合驾驶舱 | `/dashboard` | `/api/pvms/dashboard` | 已完成 |
| M02 | 生产监控 | `/production-monitor` | `/api/pvms/production-monitor` | 已完成 |
| M02b | 电站档案 | `/station-archive` | `/api/pvms/station-archive` | 已完成 |
| M02c | 资源单元 | `/stations` | `/api/pvms/resource-units` | 已完成 |
| M03 | 预测分析 | `/forecast` | `/api/pvms/forecast` | 已完成 |
| M04 | 策略管理 | `/strategy` | `/api/pvms/strategy` | 已完成 |
| M05 | 设备监测 | `/devices` | `/api/pvms/devices` | 已完成 |
| M06 | 告警中心 | `/alarms` | `/api/pvms/alarms` | 已完成 |

## 数据库概览

27 张表，分 6 大域：

| 域 | 表前缀 | 表数量 | 说明 |
|---|---|---|---|
| 驾驶舱 | `dashboard_` | 4 | 电站地理、状态快照、告警、VPP 节点 |
| 生产监控 | `pm_` | 6 | 资源单元、站点、快照、气象、曲线、调度 |
| 电站档案 | `sa_` | 7 | 公司、站点、曲线、逆变器、告警、策略 |
| 预测分析 | `fc_` | 6 | 模型、绑定、预测序列、误差、月度精度、可调窗口 |
| 策略管理 | `sg_` | 6 | 策略、时段、执行日志、电价、收益、模拟快照 |
| 系统 | — | — | 健康检查（无持久化） |

## 后端架构 (v2.0.0)

```
Controller → Service → Mapper (MyBatis @Mapper 接口)
                         ↓
                    XML Mapper 文件 (src/main/resources/mapper/**/*.xml)
                         ↓
                      MySQL 8 / H2 (测试)
```

- **27 个 Mapper 接口**：使用 Java `record` 作为结果类型
- **27 个 XML Mapper**：SQL 与 Java 代码分离
- **@MapperScan**：自动扫描 `cn.techstar.pvms.backend` 包下所有 Mapper
- **驼峰映射**：`map-underscore-to-camel-case: true`，无需手写 resultMap

## 文档导航

详细文档集中在 `docs/` 目录：

- **交接文档** → `docs/handover/` — 新人入门必读（01-08）
- **模块文档** → `docs/modules/frontend/` 和 `docs/modules/backend/` — 接口与组件说明
- **审计记录** → `docs/audits/` — 代码审查和契约核验
- **测试指南** → `docs/testing/` — API 测试和 E2E 测试
- **功能设计** → `doc/` — 早期设计书（参考用）

## 版本历史

| 版本 | 日期 | 说明 |
|---|---|---|
| v1.0.0 | 2026-04-04 | H2 + JdbcClient 架构，全模块功能完成 |
| v2.0.0 | 2026-04-04 | 迁移至 MyBatis + MySQL，49 项测试通过 |
