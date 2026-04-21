# Strategy Frontend Module

## 1. 当前状态

截至 `2026-03-30`，前端已经有独立的 `strategy` 模块，不再只是文档或残留设计稿。

当前真实入口：

- `frontend/src/modules/strategy/pages/StrategyPage.vue`

真实路由：

- `/strategy/list`
- `/strategy/config`
- `/strategy/revenue`

真实菜单：

- `frontend/src/settings.js` 中的 `strategy`

## 2. 模块结构

| 路径 | 作用 |
| --- | --- |
| `pages/StrategyPage.vue` | 模块页壳，负责 tabs、筛选条件、数据装配 |
| `components/StrategyHero.vue` | 顶部说明区 |
| `components/StrategyTabNav.vue` | 三个子视图的切换 |
| `components/StrategyFilterBar.vue` | 公共筛选条件 |
| `components/views/StrategyListView.vue` | 列表、详情抽屉、批量提交/删除 |
| `components/views/StrategyConfigView.vue` | 树、表单、电价、单/批量模拟、创建 |
| `components/views/StrategyRevenueView.vue` | 收益 KPI、趋势图、明细表、对比表 |

## 3. 路由与 viewKey

| 路由 | `viewKey` | 说明 |
| --- | --- | --- |
| `/strategy/list` | `list` | 策略台账和状态流转 |
| `/strategy/config` | `config` | 策略配置与模拟 |
| `/strategy/revenue` | `revenue` | 策略收益分析 |

`StrategyPage.vue` 根据 `viewKey` 决定当前应该加载哪一个子视图。

## 4. 数据流

### 4.1 首次进入模块

页面创建时先拉：

- `fetchStrategyMeta`

这一步会初始化：

- 默认电站
- 公司/区域筛选项
- 电站筛选项
- 策略类型筛选项
- 策略状态筛选项

### 4.2 列表页

列表页当前会调用：

- `fetchStrategyKpi`
- `fetchStrategyList`

点击详情时额外调用：

- `fetchStrategyDetail`

点击动作时会调用：

- `submitStrategy`
- `terminateStrategy`
- `batchSubmitStrategy`
- `batchDeleteStrategy`

### 4.3 配置页

配置页当前会调用：

- `fetchStrategyTree`
- `fetchStrategyElectricityPrice`
- `fetchStrategyList`
- `simulateStrategy`
- `batchSimulateStrategy`
- `createStrategy`
- `batchCreateStrategy`

注意：

- 电价表是后端接口，不再由前端静态写死
- 模拟结果来自后端计算，不再走本地 mock

### 4.4 收益页

收益页当前会调用：

- `fetchStrategyRevenueSummary`
- `fetchStrategyRevenueDetail`
- `fetchStrategyCompare`

## 5. 前端与后端契约

这次 M04 的一个重点，是把之前飘掉的字段名收回来。

当前前端按下面这些字段消费：

- 树结构字段：`tree`
- 单策略模拟区间：`confidenceRange`
- 批量模拟结果数组：`results`
- 批量模拟总收益：`totalRevenue`
- 批量创建请求体：`{ strategies: [...] }`

如果后端有人后续改字段名，优先改后端适配层，不要直接把字段漂移留给页面层承受。

## 6. 各视图维护要点

### 6.1 StrategyListView

职责：

- 展示 KPI 卡片
- 展示策略列表
- 打开详情抽屉
- 执行提交、终止、批量提交、批量删除

维护注意点：

- 表格字段来自 `list.items`
- 抽屉字段来自 `detail`
- 详情抽屉里 period 和 log 都是后端 detail 接口给出的

### 6.2 StrategyConfigView

这是当前模块最复杂的视图。

职责：

- 显示公司/站点树
- 维护策略表单
- 拉取电价时段
- 执行单策略模拟
- 执行批量模拟
- 创建单条策略
- 批量创建策略

维护注意点：

- `form` 只是前端输入态，真实结果以后台响应为准
- `batchSimulate` 在没有勾选表格时，会按当前筛选范围自动挑两座站做候选
- 电价变化必须重新走 `fetchStrategyElectricityPrice`

### 6.3 StrategyRevenueView

职责：

- 展示收益 KPI
- 画趋势图
- 展示收益明细
- 展示策略对比

维护注意点：

- 趋势图依赖 `summary.trend`
- 对比图依赖 `compare.items`
- 明细表依赖 `detail.items`

## 7. 手动联调时怎么检查

### 7.1 页面入口

```text
http://127.0.0.1:6618/strategy/list
http://127.0.0.1:6618/strategy/config
http://127.0.0.1:6618/strategy/revenue
```

### 7.2 开发态关键请求

打开浏览器开发者工具，确认请求命中：

- `/api/pvms/strategy/meta`
- `/api/pvms/strategy/list`
- `/api/pvms/strategy/tree`
- `/api/pvms/strategy/simulate`
- `/api/pvms/strategy/revenue/summary`

## 8. 后续扩展建议

### 8.1 如果继续做真实业务化

优先扩展：

- 更细的策略表单字段
- 真正的批量选站和批量编排
- 更丰富的 compare 维度
- 提交后自动刷新详情和收益页

### 8.2 如果后端换成真实系统

前端建议保持：

- API 名称不变
- 组件职责不变
- 视图拆分不变

这样后端只需要替换数据源和公式，不需要再重写页面。
