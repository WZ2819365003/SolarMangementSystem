# Strategy 前端模块

## 1. 模块定位

该模块对应菜单“策略管理”，承担策略从查看、配置到收益分析的一整套前端交互。

页面入口：

- `frontend/src/modules/strategy/pages/StrategyPage.vue`

## 2. 路由

| 路由 | viewKey | 说明 |
| --- | --- | --- |
| `/strategy/list` | `list` | 策略列表 |
| `/strategy/config` | `config` | 策略配置 |
| `/strategy/revenue` | `revenue` | 收益分析 |

## 3. 核心文件

| 路径 | 作用 |
| --- | --- |
| `pages/StrategyPage.vue` | 模块总入口，负责 tabs 和元数据初始化 |
| `components/StrategyHero.vue` | 顶部说明区 |
| `components/StrategyTabNav.vue` | 子标签导航 |
| `components/views/StrategyListView.vue` | 列表、筛选、批量提交/删除 |
| `components/views/StrategyConfigView.vue` | 树选择、单个/批量配置、模拟、创建、提交 |
| `components/views/StrategyRevenueView.vue` | 收益趋势、明细、对比 |

## 4. 数据流

### 元数据

页面创建时先拉：

- `fetchStrategyMeta`

用于初始化：

- 策略类型
- 状态
- 电站
- 公司
- 分时电价

### 列表页

主要接口：

- `fetchStrategyKpi`
- `fetchStrategyList`
- `fetchStrategyDetail`
- `submitStrategy`
- `terminateStrategy`
- `batchSubmitStrategy`
- `batchDeleteStrategy`

### 配置页

主要接口：

- `fetchStrategyTree`
- `simulateStrategy`
- `batchSimulateStrategy`
- `createStrategy`
- `batchCreateStrategy`
- `submitStrategy`

### 收益页

主要接口：

- `fetchStrategyRevenueSummary`
- `fetchStrategyRevenueDetail`
- `fetchStrategyCompare`

## 5. 配置页是本模块最复杂的部分

`StrategyConfigView.vue` 同时承担以下职责：

- 左侧公司/电站树展示
- 单站与批量模式切换
- 表单录入
- 单策略收益模拟
- 批量收益模拟
- 创建策略
- 创建后提交

这也是它文件非常大的原因。

## 6. 当前前端期望的关键结构

### 树结构

配置页当前从 `fetchStrategyTree()` 返回结果里读取：

- `res.data.tree`

### 批量模拟结果

批量模拟后当前读取：

- `batchRes.data.results`
- `batchRes.data.totalRevenue`

### 单策略模拟结果

当前界面读取：

- `simulation.estimatedRevenue`
- `simulation.confidenceRange`
- `simulation.breakdown.peakSaving`

### 批量创建请求体

当前前端组包为：

- `{ strategies: [...] }`

## 7. 当前维护风险

这是与后端契约漂移最明显的模块之一。

已知风险包括：

- 树接口字段名不一致
- 批量创建请求体字段名不一致
- 批量模拟返回字段名不一致
- 单策略模拟返回结构不一致

因此维护本模块时，不能只在前端 mock 上验证成功就结束，必须同时对照：

- `frontend/src/shared/mock/strategy.js`
- `backend/src/main/java/.../strategy/controller/StrategyController.java`
- `backend/src/main/java/.../strategy/service/StrategyMockService.java`

## 8. 维护建议

### 改列表页

注意批量操作对状态流转的依赖：

- `draft`
- `pending`
- `executing`
- `completed`
- `terminated`

### 改配置页

建议先确认你改的是哪一类逻辑：

- 树选择
- 表单字段
- 模拟逻辑
- 创建逻辑
- 提交流程

否则很容易在一个超大文件里误伤其他流程。

### 改收益页

注意这里依赖 summary 和 detail 两条接口链路，还支持 compare 对比，不是单接口页面。

## 9. 推荐阅读文件

- `frontend/src/modules/strategy/pages/StrategyPage.vue`
- `frontend/src/modules/strategy/components/views/StrategyListView.vue`
- `frontend/src/modules/strategy/components/views/StrategyConfigView.vue`
- `frontend/src/modules/strategy/components/views/StrategyRevenueView.vue`

