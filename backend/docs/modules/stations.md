# Resource Units Module

## Scope

- 切片名称: `M02-A`
- 当前接口:
  - `GET /api/pvms/resource-units/list`
  - `GET /api/pvms/resource-units/{resourceUnitId}/overview`
  - `GET /api/pvms/resource-units/{resourceUnitId}/power-curve`
- 当前产品身份:
  - 面向虚拟电厂聚合商的生产监控 mock 接口

## Controller

- File: `src/main/java/cn/techstar/pvms/backend/module/stations/controller/StationController.java`
- Service: `StationMockService`

## Endpoints

### `GET /api/pvms/resource-units/list`

- Purpose: 返回聚合资源单元列表页数据
- Query params:
  - `keyword?`
  - `status?`
  - `region?`
  - `capacityRange?`
  - `page` default `1`
  - `pageSize` default `6`
- Response fields:
  - `filters.statusOptions`
  - `filters.regionOptions`
  - `filters.capacityOptions`
  - `statistics`
  - `total`
  - `page`
  - `pageSize`
  - `list`

### `GET /api/pvms/resource-units/{resourceUnitId}/overview`

- Purpose: 返回资源单元总览页的基础信息、KPI、天气、调度执行、告警概况和成员电站摘要
- Path params:
  - `resourceUnitId`
- Response fields:
  - `info`
  - `kpi`
  - `weather`
  - `dispatchSummary`
  - `alarmSummary`
  - `memberStations`

### `GET /api/pvms/resource-units/{resourceUnitId}/power-curve`

- Purpose: 返回资源单元总览页的出力预测/实际主图数据
- Path params:
  - `resourceUnitId`
- Query params:
  - `date?`
- Response fields:
  - `resourceUnitId`
  - `resourceUnitName`
  - `currentDate`
  - `peakPowerMw`
  - `deviationRate`
  - `actual`
  - `forecast`
  - `baseline`
  - `irradiance`

## Mock Data Source

- File: `src/main/java/cn/techstar/pvms/backend/module/stations/service/StationMockService.java`
- Responsibilities:
  - 保存资源单元主数据
  - 做列表筛选、统计和分页
  - 返回资源单元总览结构
  - 生成出力曲线 mock 数据

## Tests

- File: `src/test/java/cn/techstar/pvms/backend/module/stations/controller/StationControllerTest.java`
- Covered:
  - 资源单元列表结构
  - 关键字 + 状态过滤
  - 资源单元总览结构
  - 出力曲线结构

## Maintenance Notes

- 当前仍是 mock 服务，但字段结构已按前端消费模型固定
- 如果后续替换成真实数据源，优先保持响应结构不变
- 如需新增字段，先补 `MockMvc` 测试，再扩展前端 mock，最后接真实实现
