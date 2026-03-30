# Stations Frontend Module

## 1. 模块定位

前端 `stations` 不是单一页面，而是一组被多个页面复用的“电站监控组件”。

这组组件同时服务两个入口：

1. 电站档案列表页  
   读取 `/api/pvms/stations/archive`

2. 生产监控中的“电站监控模式”  
   读取 `/api/pvms/station-tree` 和 `/api/pvms/station-archive/*`

所以维护时不要把它理解成“独立页面组件”，它本质上是 `production-monitor` 的子系统。

## 2. 关键文件

### 页面与容器

- `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
  负责资源总览模式和电站监控模式切换

### 左侧树与头部

- `frontend/src/modules/stations/components/StationTreePanel.vue`
- `frontend/src/modules/stations/components/StationDataHeader.vue`

### 公司 / 电站 / 逆变器右侧内容

- `frontend/src/modules/stations/components/CompanyOverviewView.vue`
- `frontend/src/modules/stations/components/StationMonitorView.vue`
- `frontend/src/modules/stations/components/StationStrategyView.vue`
- `frontend/src/modules/stations/components/InverterDetailView.vue`

### 数据请求

- `frontend/src/api/pvms.js`

## 3. 前端真实依赖的后端契约

这是维护时最重要的部分。

### 档案列表页

依赖接口：
- `GET /api/pvms/stations/archive`

核心字段：
- `data.items[].id`
- `data.items[].name`
- `data.items[].companyName`
- `data.items[].capacityKwp`
- `data.items[].gridStatus`
- `data.items[].gridStatusLabel`
- `data.items[].commissionDate`
- `data.items[].address`
- `data.items[].inverterCount`
- `data.items[].status`

### 左侧树

依赖接口：
- `GET /api/pvms/station-tree`

核心字段：
- `data.tree`
- `nodeType`
- `label`
- `children`
- `extra.status`
- `extra.dataQuality`
- `extra.loadKw`
- `extra.pvOutputKw`
- `extra.adjustableKw`

说明：
- `StationDataHeader.vue` 会直接读取 `node.extra.status` 和 `node.extra.dataQuality`
- 如果后端漏掉这两个字段，头部标签会直接消失

### 公司总览

依赖接口：
- `GET /api/pvms/station-archive/company-overview`

核心字段：
- `data.name`
- `data.kpis`
- `data.stations`

注意：
- 公司视角的“策略查看”表格是前端基于 `stations` 本地拼的
- 这个区域当前不依赖后端直接返回公司级策略列表

### 电站图表

依赖接口：
- `GET /api/pvms/station-archive/station-realtime`

请求参数：
- `stationId`
- `metric`
- `date`
- `granularity`

不同 `metric` 的消费重点：

#### `adjustable`

必须返回：
- `times`
- `series`
- `stationKpis`
- `monthlyStats`
- `fields`

#### `pv-output`

必须返回：
- `times`
- `series`

#### `load`

必须返回：
- `times`
- `series`

#### `forecast`

必须返回：
- `times`
- `series`

### 电站策略

依赖接口：
- `GET /api/pvms/station-archive/station-strategy`

必须返回：
- `data.currentStrategy`
- `data.executionLogs`

### 逆变器详情

依赖接口：
- `GET /api/pvms/station-archive/inverter-realtime`

必须返回：
- `ratedPowerKw`
- `realtimePowerKw`
- `dailyEnergyMwh`
- `efficiency`
- `powerCurve`
- `topology`
- `alarms`
- `deviceInfo`

## 4. 维护时最容易踩坑的地方

### 坑 1：把 `stations` 当成独立模块维护

错误理解：
- 只看 `stations/components`，不看 `production-monitor/pages/ProductionMonitorPage.vue`

正确理解：
- 页面真实状态切换、选中节点、请求时机都在 `ProductionMonitorPage.vue`
- `stations/components` 主要负责渲染和局部交互

### 坑 2：只改后端字段，不看组件消费方式

例子：
- `StationTreePanel.vue` 需要 `label`
- `StationDataHeader.vue` 需要 `extra.status` 和 `extra.dataQuality`
- `StationMonitorView.vue` 在 `adjustable` 场景需要 `fields`
- `InverterDetailView.vue` 需要完整的 `topology` 和 `deviceInfo`

如果字段名改了但组件没改，页面通常不会报显式异常，只会静默空渲染。

### 坑 3：忽略粒度参数

当前前端会传 `granularity`。

维护原则：
- 15 分钟粒度是基础数据
- 1 分钟粒度是后端插值结果
- 前端不关心你后端是插值还是实采，只关心返回点数和结构是否稳定

## 5. 和 `production-monitor` 的关系

`production-monitor` 当前有两个模式：

1. 资源总览模式  
   走 `/api/pvms/production-monitor/*`

2. 电站监控模式  
   走 `/api/pvms/station-tree` 和 `/api/pvms/station-archive/*`

这意味着：
- 资源总览链路已经有自己独立的 H2 数据底座
- 电站监控链路现在也有独立的 H2 数据底座
- 两条链路在前端页面内并存，但后端接口前缀不同

## 6. 前端维护建议

### 改 UI 时

- 先确认当前节点类型是 `company / station / inverter` 哪一种
- 再确认对应数据源来自哪个接口

### 改字段时

建议顺序：

1. 改后端测试
2. 改后端返回
3. 改 `pvms.js`
4. 改组件读取
5. 跑页面回归

### 改图表时

- 不要假设所有 metric 都有相同字段
- `adjustable` 是最重的一种
- `pv-output / load / forecast` 只依赖 `times + series`

## 7. 回归建议

每次改动这组组件后，至少手动检查：

1. 电站树是否能展开到逆变器层
2. 公司节点切换后右侧 KPI 是否刷新
3. 电站节点切换后 `adjustable / pv-output / load / forecast` 是否都能切换
4. 逆变器节点点击后详情侧板是否能显示拓扑、告警和设备信息
