# M03 台账管理模块

## 功能

电站台账管理，提供公司→电站→逆变器三级数据查询，含实时监控、可调容量、策略关联。

## 路由

### `GET /api/pvms/stations/archive`
- 功能: 电站台账列表
- 参数: `keyword?`, `gridStatus?`
- 返回: `{ items: [{ id, name, companyName, capacityKwp, gridStatus, ... }] }`

### `GET /api/pvms/station-tree`
- 功能: 公司→电站树形结构
- 参数: 无
- 返回: `{ items: [{ id, name, type:"company", children: [{ id, name, type:"station" }] }] }`

### `GET /api/pvms/station-archive/company-overview`
- 功能: 公司级总览
- 参数: `companyId`
- 返回: `{ companyId, companyName, totalCapacityKwp, stationCount, todayEnergyKwh, stations: [...] }`

### `GET /api/pvms/station-archive/resource-overview`
- 功能: 资源节点总览
- 参数: `stationId`
- 返回: `{ stationId, stationName, capacityKwp, kpi: {...}, inverters: [...] }`

### `GET /api/pvms/station-archive/station-realtime`
- 功能: 电站实时数据 (功率曲线+逆变器状态)
- 参数: `stationId`
- 返回: `{ stationId, realtimePowerKw, powerCurve: [96点], inverterStatus: [...] }`

### `GET /api/pvms/station-archive/station-adjustable`
- 功能: 电站可调容量
- 参数: `stationId`
- 返回: `{ stationId, currentPowerKw, maxAdjustableKw, periods: [...] }`

### `GET /api/pvms/station-archive/station-strategy`
- 功能: 电站当前生效策略
- 参数: `stationId`
- 返回: `{ stationId, activeStrategies: [...] }`

### `GET /api/pvms/station-archive/inverter-realtime`
- 功能: 逆变器实时监控
- 参数: `stationId`, `inverterId?`
- 返回: `{ items: [{ id, name, ratedPowerKw, currentPowerKw, efficiency, strings: [...] }] }`

## 数据

- 5 家公司, 16 个电站, 每站 4~16 台逆变器
- 电站数据含 GIS、容量、并网状态、实时功率

## 测试

- 文件: `StationArchiveControllerTest.java`
- 覆盖: 列表、关键字过滤、树形结构、公司总览、资源总览、实时数据、可调容量、策略关联、逆变器监控
- 测试数: 9 个
