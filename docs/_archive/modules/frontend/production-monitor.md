# Production Monitor 前端模块

## 模块定位

生产监控模块是前端里最容易“看着像一个模块，实际跨两个系统边界”的页面。

它同时承载两套模式：

- 资源总览模式
- 电站监控模式

其中只有“资源总览模式”走 `/api/pvms/production-monitor/*` 这组接口；“电站监控模式”仍然复用 `stationarchive` 链路。

页面主入口：

- `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`

## 路由和模式

| 路由 | `viewKey` | 实际模式 | 说明 |
| --- | --- | --- | --- |
| `/production-monitor/overview` | `overview` | 资源总览 | KPI、成员站、天气、摘要表 |
| `/production-monitor/output` | `output` | 资源总览 | 出力曲线、天气趋势、贡献排名 |
| `/production-monitor/load` | `load` | 资源总览 | 负荷与出力、站表、电网交互 |
| `/production-monitor/dispatch` | `dispatch` | 资源总览 | 调度执行趋势、记录、风险提示 |
| `/production-monitor/station` | `station` | 电站监控 | 站树 + 右侧详情面板 |

路由定义在：

- `frontend/src/router/index.js`

## 当前真实数据边界

### 已接后端 H2 的部分

以下接口现在已经走真实后端路径，不再依赖前端 mock：

- `fetchProductionMonitorMeta`
- `fetchProductionMonitorOverview`
- `fetchProductionMonitorOutput`
- `fetchProductionMonitorLoad`
- `fetchProductionMonitorDispatch`
- `fetchProductionMonitorWeather`

接口统一定义在：

- `frontend/src/api/pvms.js`

开发环境请求桥接在：

- `frontend/src/shared/host/bridge.js`

### 仍然不在本次 H2 改造范围的部分

`secondaryMode === 'station'` 时，页面仍然使用：

- `fetchStationTree`
- `fetchStationArchiveCompanyOverview`
- `fetchStationArchiveRealtime`
- `fetchStationArchiveStrategy`
- `fetchStationArchiveInverterRealtime`

也就是说：

- `/production-monitor/overview`
- `/production-monitor/output`
- `/production-monitor/load`
- `/production-monitor/dispatch`

已经切到 `productionmonitor` 后端链路。

但：

- `/production-monitor/station`

仍然依赖 `stationarchive`。

## 页面结构

### 总入口：`ProductionMonitorPage.vue`

它负责四件事：

1. 初始化元数据。
2. 根据路由判断当前是“资源总览”还是“电站监控”。
3. 管理查询条件。
4. 把接口返回数据分发到对应 view 组件。

关键函数：

- `initializeModule`
- `bootstrapQuery`
- `loadCurrentView`
- `switchSecondary`
- `loadTree`

### 资源总览模式下的组件

- `components/ProductionMonitorHero.vue`
- `components/ProductionMonitorTabNav.vue`
- `components/ProductionMonitorFilterBar.vue`
- `components/views/ProductionOverviewView.vue`
- `components/views/ProductionOutputView.vue`
- `components/views/ProductionLoadView.vue`
- `components/views/ProductionDispatchView.vue`

### 电站监控模式下的组件

- `frontend/src/modules/stations/components/StationTreePanel.vue`
- `frontend/src/modules/stations/components/StationDataPanel.vue`

这两个组件是复用组件，不只服务于 `production-monitor`。改它们时要同时考虑 `stations` 模块。

## 查询参数与数据流

页面内部维护的查询对象是：

```js
{
  region: '',
  city: '',
  resourceUnitId: '',
  date: 'YYYY-MM-DD',
  granularity: '15m'
}
```

注意这里有两个容易踩坑的点：

### `date` 和 `granularity` 已经存在，但不一定都体现在 URL 中

`syncRouteQuery()` 当前只把 `resourceUnitId` 放进路由 query。也就是说：

- 刷新页面后，`date` 和 `granularity` 不会从 URL 恢复。
- 如果后续要支持分享或收藏具体查询条件，需要一起改前端路由同步。

### `load` 页会把整份查询对象传给后端

`loadCurrentView()` 会把 `query` 整体传给 `fetchProductionMonitorLoad`。因此后端已经能接到：

- `resourceUnitId`
- `date`
- `granularity`

但当前后端 `load` 接口只真正使用了：

- `date`
- `granularity`

`resourceUnitId` 现阶段仅作为兼容参数保留。

## 当前前端消费的主要字段

### Overview 视图

依赖：

- `info`
- `kpis`
- `memberStations`
- `weatherBrief`
- `alarmBrief`
- `summaryTable`

### Output 视图

依赖：

- `summary`
- `curve`
- `weatherTrend`
- `contributionRanking`
- `table`

### Load 视图

依赖：

- `summary.totalLoadMw`
- `summary.totalPvOutputMw`
- `summary.totalAdjustableMw`
- `summary.avgRampRate`
- `summary.stationCount`
- `summary.onlineCount`
- `stations[]`

每个站点还要求：

- `name`
- `capacityKwp`
- `realtimePowerKw`
- `loadKw`
- `adjustableKw`
- `maxRampRate`
- `status`
- `gridInteraction.times`
- `gridInteraction.series`

### Dispatch 视图

依赖：

- `summary`
- `executionTrend`
- `riskHints`
- `records`

## `load` 页的关键维护点

负荷与出力页在：

- `frontend/src/modules/production-monitor/components/views/ProductionLoadView.vue`

这个页面不是单纯表格，有三层结构：

1. 顶部 KPI 板。
2. 中间筛选条。
3. 底部站表 + 右侧电网交互抽屉。

点击“查看”后会打开抽屉，读取每个站点的：

- `gridInteraction.times`
- `gridInteraction.series`

因此后端如果改了站点行结构，最容易先坏掉的是这个抽屉图表，而不是 KPI。

## 当前已知前端行为差异

### 路由有 `output`，但页签没有直接展示

`router/index.js` 中存在 `/production-monitor/output`，但 `tabs` 只展示：

- `overview`
- `load`
- `dispatch`

所以：

- 代码支持 output 视图。
- UI 主 tab 没有直接暴露 output 页签入口。

维护时如果你只看页面，很容易误判“output 已删除”。实际上它还在路由和 view 组件里。

### 导出按钮还是占位行为

`handleExport()` 当前只弹消息：

- “当前为 mock 设计版，导出能力将在接真实接口后打开”

这意味着生产监控的导出链路还没有接后端。

## 和后端的协作边界

当前推荐的前后端分工是：

### 前端负责

- 页面布局
- 图表渲染
- 查询条件管理
- 视图切换
- 站点抽屉交互

### 后端负责

- 元数据
- 站点事实数据
- 时序聚合
- 负荷与出力指标计算
- 预测偏差和调度统计
- 风险提示文案

不要把以下逻辑重新搬回前端：

- 粒度聚合
- 最大爬坡速率计算
- 预测准确率计算
- 调度成功率和调用量统计

这些应该继续留在后端。

## 推荐阅读顺序

如果是第一次维护这个模块，建议按下面顺序看代码：

1. `frontend/src/router/index.js`
2. `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
3. `frontend/src/api/pvms.js`
4. `frontend/src/modules/production-monitor/components/views/ProductionLoadView.vue`
5. `frontend/src/modules/production-monitor/components/views/ProductionOverviewView.vue`
6. `frontend/src/modules/production-monitor/components/views/ProductionDispatchView.vue`
7. `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
8. `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`

## 常见问题

### 为什么页面切到了 `/load`，后端却没收到 `resourceUnitId` 过滤效果？

因为当前后端 `load` 接口还没有按资源单元过滤，先返回了全局站表，以兼容现有前端展示方式。

### 为什么切换查询条件后，刷新页面又回到默认粒度？

因为当前路由 query 只持久化了 `resourceUnitId`，没有持久化 `date` 和 `granularity`。

### 为什么改了站点监控右侧内容，却影响了别的页面？

因为 `StationTreePanel` 和 `StationDataPanel` 是跨模块复用组件。

## 后续建议

### 短期

- 让 tab 和实际路由完全对齐，避免 output 页面成为隐式入口
- 把 `date` 和 `granularity` 同步到 URL query
- 为 `load` 页补一条前端联调用例，验证抽屉曲线结构

### 中期

- 把 `station` 模式也逐步切到真实后端数据链路
- 给 `production-monitor` 前端补类型约束或字段校验层

### 长期

- 支持真正按资源单元、日期、粒度的历史数据分析
- 支持导出、回放、趋势对比等运维功能
