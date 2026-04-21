# M02-A 聚合资源单元列表与总览 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 把 M02-A 从单电站运维视角收缩为虚拟电厂聚合商视角，交付资源单元列表页和资源单元总览页。

**Architecture:** 前端继续沿用现有 `station-monitoring` 模块目录和 `/station` 路由前缀，但页面语义切换为“聚合资源单元”。后端继续使用 Spring Boot mock 服务，接口从 `stations` 重构为 `resource-units`，并保留成员电站摘要作为解释性数据，而不是深设备入口。

**Tech Stack:** Vue 2, Vue Router, Element UI, hs-dtk-ui, ECharts 5, Playwright, Spring Boot, MockMvc, JUnit 5

---

### Task 1: 修订设计与维护文档基线

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/doc/M02_生产监控层_前端设计.md`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/docs/modules/station-monitoring.md`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/backend/docs/modules/stations.md`

- [ ] **Step 1: 把设计文档中的一级对象从电站改成聚合资源单元**
- [ ] **Step 2: 明确保留天气、出力预测/实际、调度执行、告警概况、成员电站摘要**
- [ ] **Step 3: 删除深设备页和占位页的产品承诺**

### Task 2: 先写后端失败测试

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/backend/src/test/java/cn/techstar/pvms/backend/module/stations/controller/StationControllerTest.java`

- [ ] **Step 1: 把列表接口测试改成 `GET /api/pvms/resource-units/list`**
- [ ] **Step 2: 运行单测，确认因为接口路径或字段结构不匹配而失败**
- [ ] **Step 3: 把详情接口测试改成 `GET /api/pvms/resource-units/{id}/overview`**
- [ ] **Step 4: 把曲线接口测试改成 `GET /api/pvms/resource-units/{id}/power-curve`**
- [ ] **Step 5: 再次运行单测，确认失败原因正确**

### Task 3: 实现后端聚合资源单元 mock

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/backend/src/main/java/cn/techstar/pvms/backend/module/stations/controller/StationController.java`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/backend/src/main/java/cn/techstar/pvms/backend/module/stations/service/StationMockService.java`

- [ ] **Step 1: 把控制器路径从 `stations` 切到 `resource-units`**
- [ ] **Step 2: 用资源单元主数据替换原单电站列表主数据**
- [ ] **Step 3: 让列表接口返回资源单元统计、筛选和分页**
- [ ] **Step 4: 让详情接口返回 `info / kpi / weather / dispatchSummary / alarmSummary / memberStations`**
- [ ] **Step 5: 让曲线接口返回 `actual / forecast / baseline / irradiance`**
- [ ] **Step 6: 运行后端单测，确认通过**

### Task 4: 先写前端失败断言

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/tests/playwright/specs/station-monitoring.spec.js`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/tests/playwright/specs/layout-smoke.spec.js`

- [ ] **Step 1: 把列表页断言改成资源单元语义**
- [ ] **Step 2: 把总览页断言改成天气、调度执行、告警概况、成员电站摘要**
- [ ] **Step 3: 运行目标 Playwright 用例，确认因为页面文案或结构不匹配而失败**

### Task 5: 调整前端 API 与 mock 链路

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/api/pvms.js`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/shared/mock/data.js`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/shared/mock/station-monitoring.js`

- [ ] **Step 1: 把前端 API 路径切到 `resource-units`**
- [ ] **Step 2: 调整本地 mock 数据结构与后端一致**
- [ ] **Step 3: 保证列表页和总览页在开发态先走前端 mock**

### Task 6: 改造资源单元列表页

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/pages/StationListPage.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationListFilterBar.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationCardGrid.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationCardItem.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationTableView.vue`

- [ ] **Step 1: 把文案和指标切成资源单元语义**
- [ ] **Step 2: 卡片改为展示电站数、可调容量、实时出力、偏差率、在线率、天气**
- [ ] **Step 3: 表格列同步改成聚合商视角**
- [ ] **Step 4: 确认点击仍然进入 `/station/:id`**

### Task 7: 改造资源单元总览页

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/pages/StationOverviewPage.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationOverviewHero.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationOverviewKpiBoard.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationOverviewPowerCurve.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationDeviceSummaryCard.vue`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/station-monitoring/components/StationQuickNav.vue`

- [ ] **Step 1: Hero 改成资源单元基础信息和天气摘要**
- [ ] **Step 2: KPI 改成实时出力、当日电量、上调可调、下调可调、在线率、预测准确率**
- [ ] **Step 3: 曲线图改成预测/实际/调度基线/辐照度**
- [ ] **Step 4: 原设备状态卡改成“调度执行 + 告警概况”组合卡**
- [ ] **Step 5: 原快捷导航改成成员电站摘要列表**
- [ ] **Step 6: 删除对深设备占位页的依赖**

### Task 8: 全量验证与收尾

**Files:**
- Verify only

- [ ] **Step 1: 运行前端 `lint`**
- [ ] **Step 2: 运行前端 `build`**
- [ ] **Step 3: 运行目标 Playwright**
- [ ] **Step 4: 运行 Playwright 全量回归**
- [ ] **Step 5: 运行后端 `mvn test`**
- [ ] **Step 6: 记录遗留问题和下一切片建议**
