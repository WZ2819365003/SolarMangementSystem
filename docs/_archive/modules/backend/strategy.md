# Strategy Backend Module

## 1. 当前状态

截至 `2026-03-30`，`strategy` 已经不再是“只有 controller 壳 + mock 返回”的状态。

当前实现是：

- `H2` 保存策略元数据、执行日志、电价时段、收益日表和最近一次模拟快照
- 后端 service 基于系统内已有事实数据实时计算模拟收益、区间和收益汇总
- 前端开发环境已经可以直接联调 `/api/pvms/strategy/*`

这意味着 `strategy` 模块现在和 `dashboard / production-monitor / station-archive / forecast` 一样，进入了“开发态真实联调”的阶段。

## 2. 核心代码位置

| 路径 | 作用 |
| --- | --- |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java` | `strategy` HTTP 入口 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyDataService.java` | 元数据、树、列表、详情、状态流转、创建 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategySimulationService.java` | 单策略/批量模拟，基于系统事实数据实时计算 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyRevenueService.java` | 收益 summary/detail/compare 汇总 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyMetaRepository.java` | 公司、电站元数据读取 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyTreeRepository.java` | 树结构统计读取 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRecordRepository.java` | 策略主表、period、log、snapshot 的读写 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyPriceRepository.java` | 电价时段读取 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRevenueRepository.java` | 收益日表读取与写入 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/model/` | 创建/批量/ID 请求体 |
| `backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java` | 模块契约测试 |

## 3. 数据边界

### 3.1 H2 中哪些是策略元数据

策略模块自己的元数据表全部以 `sg_` 开头：

- `sg_strategy`
- `sg_strategy_period`
- `sg_execution_log`
- `sg_price_period`
- `sg_revenue_daily`
- `sg_strategy_snapshot`

这些表的职责分别是：

- `sg_strategy`：策略主数据，包含站点、类型、状态、目标功率、执行时间窗
- `sg_strategy_period`：分时段动作计划
- `sg_execution_log`：创建、提交、终止、执行类事件
- `sg_price_period`：开发态分时电价模板
- `sg_revenue_daily`：开发态收益日汇总
- `sg_strategy_snapshot`：最近一次模拟快照

### 3.2 哪些数据来自系统内其他模块

`strategy` 不是凭空计算收益，而是复用系统里已经存在的事实数据：

- `sa_company`
- `sa_station`
- `sa_station_curve_15m`
- `fc_prediction_series_15m`

当前用法是：

- 站点、公司归属和装机来自 `sa_*`
- 站点 15 分钟负荷和实际出力来自 `sa_station_curve_15m`
- 日前预测值与上下界来自 `fc_prediction_series_15m`

### 3.3 哪些结果是后端实时计算

下面这些不是前端硬编码，也不是直接存表返回，而是 service 计算得到：

- `GET /tree` 的公司/站点统计
- `GET /kpi` 的执行数、待执行数、成功率、当日收益
- `GET /list` 的状态标签、收益快照、置信区间
- `POST /simulate` 的 `estimatedRevenue`
- `POST /simulate` 的 `confidenceRange`
- `POST /simulate` 的 `breakdown`
- `POST /batch-simulate` 的 `results` 与 `totalRevenue`
- `GET /revenue/summary` 的 KPI、趋势和类型拆分
- `GET /compare` 的累计收益差值

## 4. 接口清单

### 4.1 读取接口

| 路由 | 说明 |
| --- | --- |
| `GET /api/pvms/strategy/meta` | 返回公司、电站、类型、状态、电价模板 |
| `GET /api/pvms/strategy/tree` | 返回公司/电站树，字段名是 `tree` |
| `GET /api/pvms/strategy/kpi` | 返回策略 KPI |
| `GET /api/pvms/strategy/list` | 返回策略列表 |
| `GET /api/pvms/strategy/detail?id=SG-001` | 返回单策略详情 |
| `GET /api/pvms/strategy/electricity-price?stationId=SZ-001` | 返回指定站点电价时段 |
| `GET /api/pvms/strategy/revenue/summary` | 返回收益 KPI、趋势、类型拆分 |
| `GET /api/pvms/strategy/revenue/detail` | 返回收益明细列表 |
| `GET /api/pvms/strategy/compare?ids=SG-001,SG-002` | 返回策略对比结果 |

### 4.2 写入和状态流转接口

| 路由 | 说明 | 请求体 / 参数 |
| --- | --- | --- |
| `POST /api/pvms/strategy/create` | 创建单策略 | `StrategyRequest` |
| `POST /api/pvms/strategy/batch-create` | 批量创建 | `{ "strategies": [...] }` |
| `POST /api/pvms/strategy/submit?id=...` | 提交策略 | query param |
| `POST /api/pvms/strategy/terminate?id=...` | 终止策略 | query param |
| `POST /api/pvms/strategy/batch-submit` | 批量提交 | `{ "ids": [...] }` |
| `POST /api/pvms/strategy/batch-delete` | 批量删除 | `{ "ids": [...] }` |

### 4.3 模拟接口

| 路由 | 说明 | 关键响应字段 |
| --- | --- | --- |
| `POST /api/pvms/strategy/simulate` | 单策略模拟 | `estimatedRevenue`、`confidenceRange`、`breakdown` |
| `POST /api/pvms/strategy/batch-simulate` | 批量模拟 | `results`、`totalRevenue` |

## 5. 关键实现说明

### 5.1 StrategyDataService

这个类负责“策略元数据和状态流转”：

- `getMeta`
- `getTree`
- `getKpi`
- `getList`
- `getDetail`
- `getElectricityPrice`
- `createStrategy`
- `batchCreateStrategies`
- `submitStrategy`
- `terminateStrategy`
- `batchSubmit`
- `batchDelete`

这里还负责：

- 生成新策略 ID
- 将单策略请求拆成一段或两段 period
- 创建后写入 snapshot 和收益日表

### 5.2 StrategySimulationService

这个类是 `strategy` 的核心计算层。

它的输入不是纯 mock，而是：

- 电站负荷
- 电站实际光伏出力
- 日前预测出力
- 预测上下界
- 分时电价
- 用户录入的目标功率和时间窗

当前收益由 3 部分组成：

- `peakSaving`
- `responseReward`
- `penalty`

最终返回：

- `estimatedRevenue`
- `confidenceRange.low`
- `confidenceRange.high`
- `successProbability`
- `timeline`

### 5.3 StrategyRevenueService

这个类负责把 `sg_revenue_daily` 做成前端可直接消费的形态：

- `summary`：KPI、趋势、类型拆分
- `detail`：按策略/日期展开的收益明细
- `compare`：两条策略累计收益对比

## 6. 当前状态机

当前策略状态值：

- `draft`
- `pending`
- `executing`
- `completed`
- `terminated`

当前代码里真正实现的状态流转动作：

- `create` -> `draft`
- `submit` -> `pending`
- `terminate` -> `terminated`

`executing` 和 `completed` 当前主要由种子数据体现，用来支撑列表、KPI 和收益分析页面展示。

## 7. 验证方式

### 7.1 测试命令

```powershell
cd backend
mvn -Dtest=StrategyControllerTest test
mvn test
```

### 7.2 关键接口

```text
http://127.0.0.1:8091/api/pvms/strategy/meta
http://127.0.0.1:8091/api/pvms/strategy/tree
http://127.0.0.1:8091/api/pvms/strategy/list
http://127.0.0.1:8091/api/pvms/strategy/electricity-price?stationId=SZ-001
http://127.0.0.1:8091/api/pvms/strategy/revenue/summary
```

## 8. 以后怎么替换成真实系统

### 8.1 哪些可以直接替换数据源

- `sg_price_period` 可以替换成真实电价服务
- `sg_revenue_daily` 可以替换成真实结算/收益系统
- `sg_execution_log` 可以替换成真实执行审计日志

### 8.2 哪些后端计算逻辑建议保留

即使未来接真实系统，下面这些逻辑仍然建议保留在后端，而不是回到前端：

- 树结构统计
- KPI 汇总
- 收益 summary/detail/compare 装配
- 模拟区间和 breakdown 的统一口径

### 8.3 替换顺序建议

1. 保持现有 controller 契约不变
2. 先替换 repository 数据源
3. 再校准 simulation/revenue 的系数和公式
4. 最后再补权限、审计和异步任务链
