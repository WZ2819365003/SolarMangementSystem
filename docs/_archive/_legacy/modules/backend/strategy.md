# Strategy 后端模块

## 1. 模块定位

该模块对应后端“策略管理”能力，覆盖策略的全生命周期：

- 元数据
- 树结构
- KPI
- 列表
- 详情
- 创建
- 提交 / 终止 / 删除
- 单个模拟 / 批量模拟
- 收益分析
- 策略对比

从接口数量和行为复杂度来看，它是当前后端最重的模块之一。

## 2. 核心代码位置

| 路径 | 作用 |
| --- | --- |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java` | HTTP 接口入口 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyMockService.java` | 策略 mock 逻辑、状态流转、收益模拟 |
| `backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java` | 接口测试 |

## 3. 查询接口

| 路由 | 说明 | 参数 |
| --- | --- | --- |
| `GET /api/pvms/strategy/meta` | 元数据 | 无 |
| `GET /api/pvms/strategy/tree` | 公司/电站树 | 无 |
| `GET /api/pvms/strategy/kpi` | KPI | 无 |
| `GET /api/pvms/strategy/list` | 策略列表 | `status?`、`type?`、`stationId?` |
| `GET /api/pvms/strategy/detail` | 策略详情 | `id` |
| `GET /api/pvms/strategy/electricity-price` | 分时电价 | `stationId?`、`date?` |
| `GET /api/pvms/strategy/revenue/summary` | 收益总览 | 无 |
| `GET /api/pvms/strategy/revenue/detail` | 收益明细 | 无 |
| `GET /api/pvms/strategy/compare` | 策略对比 | `ids` |

## 4. 写接口

| 路由 | 说明 | 请求体 / 参数 |
| --- | --- | --- |
| `POST /api/pvms/strategy/create` | 创建单个策略 | `Map<String, Object>` |
| `POST /api/pvms/strategy/batch-create` | 批量创建策略 | 当前后端读取 `{ items: [...] }` |
| `POST /api/pvms/strategy/submit` | 提交策略 | `id` query param |
| `POST /api/pvms/strategy/terminate` | 终止策略 | `id` query param |
| `POST /api/pvms/strategy/batch-submit` | 批量提交 | `{ ids: [...] }` |
| `POST /api/pvms/strategy/batch-delete` | 批量删除 | `{ ids: [...] }` |
| `POST /api/pvms/strategy/simulate` | 单策略模拟 | `Map<String, Object>` |
| `POST /api/pvms/strategy/batch-simulate` | 批量模拟 | 当前后端读取 `{ items: [...] }` |

## 5. 当前真实返回结构

### `GET /meta`

当前返回：

- `types`
- `statuses`
- `stations`
- `companies`
- `pricePeriods`

### `GET /tree`

当前返回：

- `items`

树节点当前主要字段：

- `id`
- `name`
- `type`
- `children`

### `GET /kpi`

当前返回：

- `activeCount`
- `todayRevenue`
- `successRate`
- `pendingCount`

### `GET /list`

当前返回：

- `items`

### `GET /detail`

当前返回会在基础策略字段之上补充：

- `periods`
- `powerUpperLimitKw`
- `powerLowerLimitKw`
- `actualRevenue`
- `pricePeriods`
- `remark`
- `version`

### `POST /simulate`

当前返回：

- `estimatedRevenue`
- `confidence`
- `revenueRange`
- `breakdown`

### `POST /batch-simulate`

当前返回：

- `totalEstimatedRevenue`
- `items`

### 收益接口

`/revenue/summary` 当前返回：

- `kpi`
- `trend`
- `typeBreakdown`

`/revenue/detail` 当前返回：

- `items`

## 6. 当前数据和状态的真实情况

这个模块不是纯静态接口，它已经具备一定“内存态业务逻辑”：

- 初始化 25 条策略
- 支持创建、提交、终止、批量提交、批量删除
- 支持单个和批量收益模拟
- 按状态计算 KPI
- 按策略类型汇总收益

但要注意：

- 这些状态都保存在进程内
- 服务重启后会恢复初始数据
- 不具备持久化能力

## 7. 当前已知前后端契约问题

这是另一个高风险模块。

### 前端当前期望

前端页面当前更偏向使用：

- 树结构字段 `tree`
- 批量模拟返回 `results`、`totalRevenue`
- 单模拟返回 `confidenceRange`
- 批量创建请求体 `strategies`

### 后端当前实际

后端当前使用的是：

- 树结构字段 `items`
- 批量模拟返回 `items`、`totalEstimatedRevenue`
- 单模拟返回 `confidence`、`revenueRange`
- 批量创建请求体 `items`

结论：

- list 页部分链路问题较小
- config 页和 batch 相关链路是真实联调的高风险点

## 8. 业务语义补充

### 策略状态

- `draft`
- `pending`
- `executing`
- `completed`
- `terminated`

### 策略类型

- `demand-response`
- `frequency-regulation`
- `grid-constraint`
- `peak-shaving`

### 分时电价

当前内置 9 个时段，用于：

- 展示电价曲线
- 生成策略说明
- 估算收益

## 9. 测试覆盖

测试文件：

- `StrategyControllerTest.java`

当前覆盖较完整，包含：

- meta
- tree
- kpi
- list
- detail
- electricity-price
- create
- batch-create
- submit
- terminate
- batch-submit
- batch-delete
- simulate
- batch-simulate
- revenue summary
- revenue detail
- compare

## 10. 维护建议

### 如果只是补演示逻辑

可以继续在 `StrategyMockService` 中维护：

- 初始策略池
- 状态流转
- 收益估算规则
- 电价时段

### 如果目标是打通前后端联调

建议优先做：

1. 统一树接口字段
2. 统一 batch create / batch simulate 请求体和响应体
3. 统一 simulate 的收益结构表达

### 如果目标是走向真实业务后端

建议路线：

1. 先拆 DTO
2. 再拆 Service 和 Repository
3. 最后接真实电价、策略执行和收益数据源

