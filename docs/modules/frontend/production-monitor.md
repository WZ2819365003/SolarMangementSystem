# Production Monitor 前端模块

## 1. 模块定位

该模块对应菜单“生产监控”，是当前前端结构里最复杂的模块之一。

复杂点不在于单个图表，而在于它同时承载两种运行模式：

- 资源总览模式
- 电站监控模式

页面入口：

- `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`

## 2. 路由

| 路由 | viewKey | 说明 |
| --- | --- | --- |
| `/production-monitor/overview` | `overview` | 资源总览 |
| `/production-monitor/output` | `output` | 出力分析 |
| `/production-monitor/load` | `load` | 负荷与出力 |
| `/production-monitor/dispatch` | `dispatch` | 调度执行 |
| `/production-monitor/station` | `station` | 电站监控 |

## 3. 模块结构

| 路径 | 作用 |
| --- | --- |
| `pages/ProductionMonitorPage.vue` | 总入口，负责双模式切换、查询条件、数据加载 |
| `components/ProductionMonitorHero.vue` | 顶部概览区 |
| `components/ProductionMonitorTabNav.vue` | overview/load/dispatch 等标签 |
| `components/ProductionMonitorFilterBar.vue` | 资源总览模式筛选条 |
| `components/views/ProductionOverviewView.vue` | 资源总览视图 |
| `components/views/ProductionOutputView.vue` | 出力分析视图 |
| `components/views/ProductionLoadView.vue` | 负荷与出力视图 |
| `components/views/ProductionDispatchView.vue` | 调度执行视图 |

需要特别注意的复用组件：

- `frontend/src/modules/stations/components/StationTreePanel.vue`
- `frontend/src/modules/stations/components/StationDataPanel.vue`

## 4. 两种模式的差异

### 资源总览模式

`secondaryMode === 'overview'`

特征：

- 显示 Hero
- 显示 tab 导航
- 显示筛选栏
- 视图组件根据 `currentViewKey` 切换

主要接口：

- `fetchProductionMonitorMeta`
- `fetchProductionMonitorOverview`
- `fetchProductionMonitorOutput`
- `fetchProductionMonitorLoad`
- `fetchProductionMonitorDispatch`

### 电站监控模式

`secondaryMode === 'station'`

特征：

- 不走 overview 的 tab 区
- 左侧是站点树
- 右侧是公司/电站/逆变器详情面板
- 实际依赖的是 station archive 链路

主要接口：

- `fetchStationTree`
- `fetchStationArchiveCompanyOverview`
- `fetchStationArchiveRealtime`
- `fetchStationArchiveStrategy`
- `fetchStationArchiveInverterRealtime`

## 5. 维护时最容易看错的地方

### 看起来在改 production-monitor，实际上会影响 stations

因为本模块直接复用了：

- `StationTreePanel`
- `StationDataPanel`

这些组件同时服务于站点/电站相关功能，改动前要确认复用范围。

### 路由存在 `output`，但页面 tab 并不完全对齐

当前页面内部 tab 数据与路由定义不是完全一一对应的，维护时要同时检查：

- `router/index.js`
- `ProductionMonitorPage.vue`

## 6. 维护建议

### 改查询条件

先看：

- `createEmptyMeta`
- `bootstrapQuery`
- `resourceUnitOptions`
- `cityOptions`

### 改电站监控右侧内容

先看：

- `activeStationTab`
- `loadNodeData`
- `loadStationData`
- `loadInverterData`

因为右侧展示不是单接口直出，而是根据节点类型和 tab 决定不同请求。

## 7. 推荐阅读文件

- `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- `frontend/src/modules/stations/components/StationTreePanel.vue`
- `frontend/src/modules/stations/components/StationDataPanel.vue`
- `backend/src/main/java/.../productionmonitor/controller/ProductionMonitorController.java`
- `backend/src/main/java/.../stationarchive/controller/StationArchiveController.java`

