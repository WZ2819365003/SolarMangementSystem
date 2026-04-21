# Dashboard 后端模块

## 1. 模块职责

dashboard 后端模块为首页提供以下几类能力：

- 地图点位与状态摘要
- KPI 看板
- 功率曲线
- 电站排名
- 最近告警
- 天气卡片
- VPP 节点摘要

当前要特别注意两类数据来源已经分开：

- 地图相关动态数据已经进入 H2。
- 其余 dashboard 数据仍主要由 `DashboardMockService` 提供。

## 2. 代码入口

### Controller

- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/AlarmController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/WeatherController.java`

### Service

- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMapDataService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMockService.java`

### Repository

- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardStationGeoRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardAlarmSnapshotRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardVppNodeSnapshotRepository.java`

### 数据库资源

- `backend/src/main/resources/application.yml`
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

### 测试

- `backend/src/test/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardControllerTest.java`

## 3. 接口清单

### 现在由 H2 + `DashboardMapDataService` 提供

- `GET /api/pvms/dashboard/stations-geo`
- `GET /api/pvms/alarms/recent`
- `GET /api/pvms/dashboard/vpp-node-status`

### 现在仍由 `DashboardMockService` 提供

- `GET /api/pvms/dashboard/kpi-summary`
- `GET /api/pvms/dashboard/power-curve`
- `GET /api/pvms/dashboard/station-ranking`
- `GET /api/pvms/dashboard/overview`
- `GET /api/pvms/weather/current`

这说明 dashboard 模块目前是“部分真实链路、部分 mock 链路”的混合状态。维护时不要误判为整个模块都已经数据库化。

## 4. H2 数据模型

### `dashboard_station`

保存地图主数据：

- 站点 ID
- 站点名称
- 资源单元 ID
- 资源单元名称
- 区域
- 经度 / 纬度
- 地址
- 装机容量

### `dashboard_station_status_snapshot`

保存当前快照：

- 当前状态
- 实时功率
- 当日发电量
- 当日收益
- 可用率
- 健康等级
- 快照时间

### `dashboard_alarm_snapshot`

保存最近告警：

- 告警时间
- 告警级别
- 设备名称
- 告警类型
- 描述
- 当前处理状态
- 责任人
- 建议动作

### `dashboard_vpp_node_snapshot`

保存 VPP 节点基础快照：

- 节点 ID
- 总容量
- 可调范围
- 心跳时间

## 5. 后端计算逻辑

### 存储事实

- 站点主数据
- 站点实时快照
- 告警快照
- VPP 节点基础快照

### 在 service 中计算

- 区域筛选项
- 按状态统计的 summary
- `statusLabel`
- `statusColor`
- `availableCapacityMw`
- `onlineStations`
- `totalStations`

这部分要继续坚持放在后端计算。即使未来接入真实系统，也不要把这些规则回推到前端。

## 6. 当前返回契约要点

### `/api/pvms/dashboard/stations-geo`

站点项现在至少包含：

- `id`
- `name`
- `resourceUnitId`
- `resourceUnitName`
- `region`
- `longitude`
- `latitude`
- `status`
- `statusLabel`
- `statusColor`
- `capacityKwp`
- `realtimePowerKw`
- `todayEnergyKwh`
- `todayRevenueCny`
- `healthGrade`
- `availability`
- `address`

### `/api/pvms/alarms/recent`

保留原有结构：

- `summary`
- `items`

### `/api/pvms/dashboard/vpp-node-status`

保留原有结构：

- `nodeId`
- `totalCapacityMw`
- `availableCapacityMw`
- `onlineStations`
- `totalStations`
- `adjustableRangeMw`
- `lastHeartbeat`

## 7. 本地启动与验证

### 启动

在 `backend/` 目录执行：

```bash
mvn spring-boot:run
```

H2 会在启动时自动执行：

- `schema.sql`
- `data.sql`

### 验证

```bash
mvn -Dtest=DashboardControllerTest test
mvn test
```

接口探测：

```powershell
Invoke-RestMethod http://127.0.0.1:8091/api/pvms/dashboard/stations-geo
Invoke-RestMethod http://127.0.0.1:8091/api/pvms/alarms/recent
Invoke-RestMethod http://127.0.0.1:8091/api/pvms/dashboard/vpp-node-status
```

## 8. 扩展建议

你后面提到的生产数据、设备数据、预测数据、收益数据，建议按同样思路推进：

- 原始事实先落在后端数据库。
- 展示摘要和聚合指标尽量由后端统一计算。
- 前端只消费接口结果。

下一步合适的扩展表包括：

- `weather_snapshot`
- `production_timeseries`
- `forecast_timeseries`
- `revenue_record`

## 9. 当前限制

- H2 只覆盖 dashboard 地图链路，不覆盖整个项目。
- `DashboardMockService` 仍承担大量首页数据。
- production-monitor、forecast、strategy 等模块尚未完成同等级别的数据底座建设。
