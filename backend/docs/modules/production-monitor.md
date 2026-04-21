# Production Monitor Backend Module

## Scope

- 模块编号: `M02`
- 模块名称: `生产监控层`
- 后端职责:
  - 提供生产监控层的 mock 契约接口
  - 固化资源单元共享天气与共享出力趋势的业务规则
  - 为前端视图提供稳定的数据结构

## Current API List

### `GET /api/pvms/production-monitor/meta`

- 作用:
  - 初始化资源单元筛选元数据
- 当前参数:
  - 无必填参数
- 当前返回核心字段:
  - `defaultResourceUnitId`
  - `regionOptions`
  - `resourceUnits`

### `GET /api/pvms/production-monitor/overview`

- 作用:
  - 返回资源总览视图数据
- 当前参数:
  - `resourceUnitId`
- 当前返回核心字段:
  - `info`
  - `kpis`
  - `memberStations`
  - `weatherBrief`
  - `alarmBrief`
  - `summaryTable`

### `GET /api/pvms/production-monitor/output`

- 作用:
  - 返回出力分析视图数据
- 当前参数:
  - `resourceUnitId`
  - `granularity`
- 当前返回核心字段:
  - `summary`
  - `curve`
  - `weatherTrend`
  - `contributionRanking`
  - `table`

### `GET /api/pvms/production-monitor/dispatch`

- 作用:
  - 返回调度执行视图数据
- 当前参数:
  - `resourceUnitId`
- 当前返回核心字段:
  - `summary`
  - `executionTrend`
  - `riskHints`
  - `records`

### `GET /api/pvms/production-monitor/weather`

- 作用:
  - 返回气象研判视图数据
- 当前参数:
  - `resourceUnitId`
- 当前返回核心字段:
  - `summary`
  - `trend`
  - `impactTable`

## Business Rule

### 资源单元建模规则

- 同一个资源单元中的电站必须地理位置接近
- 当前 mock 用 `clusterRadiusKm` 表达聚合范围
- 如果站点距离过远，应拆成新的资源单元

### 天气共享规则

- 同一资源单元共用一套天气剖面
- 当前天气字段包括:
  - `weather`
  - `cloudiness`
  - `temperature`
  - `irradiance`
  - `humidity`
  - `windSpeed`
  - `conclusion`

### 出力共享规则

- 同一资源单元共用一条总出力趋势
- 成员电站的实时出力由以下因素推导:
  - 资源单元实时总出力
  - 电站 `weight`
  - 电站状态系数

当前状态系数:

- `normal -> 1.0`
- `warning -> 0.88`
- `fault -> 0.52`
- `offline -> 0.18`

## Code Structure

- Controller:
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- Service:
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorMockService.java`
- Test:
  - `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

## Response Format

统一响应仍然使用:

```json
{
  "code": 0,
  "message": "success",
  "data": {}
}
```

## Deprecated Backend Semantics

旧接口集合:

- `/api/pvms/stations/list`
- `/api/pvms/stations/{stationId}/overview`
- `/api/pvms/stations/{stationId}/power-curve`

说明:

- 这些旧接口当前仍可能保留在工程中
- 但它们不再是模块 2 的正式主链路

## Tests

- 当前测试文件:
  - `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`
- 当前覆盖:
  - `meta`
  - `overview`
  - `output`
  - `dispatch`
  - `weather`

## Maintenance Notes

- 当前后端是 mock 契约层，不是正式业务后端
- `date` 参数目前还没有进入正式计算逻辑
- 后续接真实天气采集时，应优先保持当前响应字段不变
- 后续接真实业务数据时，建议保留：
  - 资源单元聚合规则在 service 中
  - controller 仅处理参数和返回结构
