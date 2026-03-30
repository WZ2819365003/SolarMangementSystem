# Mock现状、联调策略与发布差距

## 1. 先区分两种 mock

这个项目同时存在两种不同层面的 mock，很多误判都来自把它们混在一起。

### 前端 mock

位置：

- `frontend/src/shared/mock/`

作用：

- 在前端开发态直接返回页面所需数据
- 让页面在没有宿主请求能力时也能演示

### 后端 mock

位置：

- `backend/src/main/java/.../module/*/service/*MockService.java`

作用：

- 提供可访问的 HTTP 接口
- 用硬编码数据、生成规则、内存态状态支撑前端

## 2. 当前最关键的事实

前端开发态默认优先走自己的 mock，而不是直接校验后端接口返回结构。

这件事由下面这个文件决定：

- `frontend/src/shared/host/bridge.js`

当前逻辑是：

- 只有在 `production` 且宿主提供 `apirequest` 时，才调用宿主真实请求
- 其余情况走 `shared/mock/data.js` 中的 `mockHandlers`

后果：

- 本地开发时页面大多数情况下不会直接暴露后端契约问题
- 真实联调前，很多字段问题会被前端 mock 掩盖

## 3. 各模块 mock 现状

| 模块 | 前端 mock | 后端 mock | 是否有真实持久化 | 当前判断 |
| --- | --- | --- | --- | --- |
| Dashboard | 有 | 有 | 无 | 演示态较完整 |
| Production Monitor | 有 | 有 | 无 | 演示态较完整 |
| Station Archive / Station Tree | 有 | 有 | 无 | 作为复用底座使用较多 |
| Forecast | 有 | 有 | 无 | 已出现前后端契约漂移 |
| Strategy | 有 | 有 | 无 | 已出现前后端契约漂移，且写接口最复杂 |
| Device Monitor | 有 | 有 | 无 | 以展示为主 |
| Alarm Center | 有 | 有 | 无 | 以展示为主 |

## 4. 为什么说后端不是“真实业务后端”

因为当前后端虽然有接口、有 service，但还缺少生产后端最重要的几个东西：

- 真实数据库或持久层
- 明确的 DTO / Entity / Repository 体系
- 权限认证
- 审计日志
- 外部系统接入层

更准确的说法是：

- 有接口层
- 有服务层
- 有一部分演示业务装配
- 但数据来源和状态真相仍主要是 mock / 内存态

## 5. 当前最严重的契约问题

### Forecast 模块

前端视图当前主要读取：

- `kpis`
- `stationTable`
- `comparison.timeLabels`
- `comparison.series`
- `heatmap.data`

后端当前主要返回：

- `kpi`
- `accuracyTrend`
- `forecastVsActual`
- `summary`
- `matrix`

结论：

- 前后端字段命名和结构层级已经不一致

### Strategy 模块

前端当前主要读取或提交：

- `tree`
- `confidenceRange`
- `results`
- `totalRevenue`
- 批量请求体 `strategies`

后端当前主要返回或接收：

- `items`
- `confidence`
- `revenueRange`
- `totalEstimatedRevenue`
- 批量请求体 `items`

结论：

- M04 的树、批量配置、收益模拟和批量创建都存在真实联调风险

## 6. 联调应该怎么展开

建议按下面顺序推进，而不是全模块同时改：

### 第一步：收敛契约文档

先做一件事：

- 明确每个接口的请求体、响应体、字段命名、空值策略

尤其优先：

- `forecast`
- `strategy`

### 第二步：让开发环境能校验真实接口

目标不是马上彻底去掉前端 mock，而是至少让开发者有一种稳定方式能够：

- 明确指定请求直连后端
- 在本地尽早发现字段不一致

### 第三步：按模块从 mock service 过渡到真实 service

建议顺序：

1. `strategy`
2. `forecast`
3. `production-monitor`
4. `dashboard`

## 7. 距离 release 还差什么

### 演示版

差距不大，当前代码已经足够支撑汇报和评审。

### 试运行 / 验收版

还差：

- 关键模块契约收敛
- 前后端真实联调闭环
- 基本质量门收口

### 生产版

还差：

- 数据源真实化
- 权限与安全
- 配置分环境
- 日志与监控
- 发布与回滚机制

## 8. 对维护者的直接建议

接手后最容易犯的错误是继续在前端 mock 上堆功能。正确顺序应该是：

1. 先把契约写清楚
2. 先把本地联调打通
3. 再决定哪些 mock 保留、哪些替换
4. 最后再继续扩展功能

