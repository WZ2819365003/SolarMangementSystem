# 文档导航

> v2.0.0 — 后端已迁移至 MyBatis + MySQL

## 新人阅读顺序

| 序号 | 文件 | 内容 |
|---|---|---|
| 1 | `handover/01_系统与阶段概览.md` | 系统定义、当前阶段 |
| 2 | `handover/02_环境准备与本地启动.md` | 环境要求、MySQL 准备、启动命令 |
| 3 | `handover/03_仓库结构与核心代码入口.md` | 目录结构、关键入口文件 |
| 4 | `handover/04_前端维护手册.md` | Vue2 架构、路由、组件 |
| 5 | `handover/05_后端维护手册.md` | MyBatis 架构、表分域、Mapper 说明 |
| 6 | `handover/06_模块清单与接口链路.md` | 前后端接口映射表 |
| 7 | `handover/08_测试_排障_交接清单.md` | 测试策略、常见问题 |

## 专题文档

| 文件 | 内容 |
|---|---|
| `handover/09_地图静态底图与动态点位下沉方案.md` | 高德地图集成方案 |
| `handover/10_M02_生产监控_H2数据与计算说明.md` | 生产监控数据计算逻辑 |
| `handover/11_M02_电站监控_H2数据与计算说明.md` | 电站监控数据计算逻辑 |
| `handover/12_M03_预测分析_H2数据与计算说明.md` | 预测分析数据计算逻辑 |
| `handover/13_M04_策略管理_H2数据与计算说明.md` | 策略管理数据计算逻辑 |
| `handover/14_M05_M06_设备监测与告警中心_H2数据与计算说明.md` | 设备告警数据计算逻辑 |

## 模块文档

### 前端（`modules/frontend/`）

dashboard / production-monitor / station-monitoring / stations / forecast / strategy / devices / alarms / infrastructure

### 后端（`modules/backend/`）

dashboard / production-monitor / station-archive / forecast / strategy / device-alarm / adjustable-capacity / system

## 目录结构

```
docs/
├── handover/           # 交接文档（01-14），新人入门主线
├── modules/
│   ├── frontend/       # 前端各模块组件与路由说明
│   └── backend/        # 后端各模块接口与数据说明
├── audits/             # 代码审计和契约核验记录
├── testing/            # API 测试和 E2E 测试指南
└── README.md           # 本文件
```

## 与项目根目录的关系

- **`docs/`**（本目录）— 集中维护的最新文档
- **`doc/`** — 早期功能设计书（参考用，不再更新）
- **`README.md`**（根目录）— 快速启动和技术栈概览
