# Production Monitor Backend Module

## 模块定位

- 模块编号：`M02`
- 路由前缀：`/api/pvms/production-monitor`
- 当前状态：`已切到 H2 内嵌数据库 + 后台聚合计算`
- 适用场景：开发环境联调、页面演示、接口契约验证、后续真实数据源接入前的业务承载层

这部分后端已经不是单纯的 `MockService -> 固定 Map`。当前实现采用了两层结构：

1. H2 种子数据提供基础事实数据。
2. `ProductionMonitorDataService` 在运行时做聚合、推导和格式装配。

因此它更接近“开发态业务后端”，而不是“纯前端 mock 替身”。

## 代码入口

- Controller：
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- 主服务：
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`
- 时序聚合器：
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorSeriesAggregator.java`
- Repository：
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorResourceUnitRepository.java`
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorStationSnapshotRepository.java`
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorWeatherSnapshotRepository.java`
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorCurveRepository.java`
  - `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorDispatchRecordRepository.java`
- 数据初始化：
  - `backend/src/main/resources/schema.sql`
  - `backend/src/main/resources/data.sql`
- 测试：
  - `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

## 当前接口清单

### `GET /api/pvms/production-monitor/meta`

- 作用：
  - 返回资源单元筛选元数据。
- 主要字段：
  - `defaultResourceUnitId`
  - `regionOptions`
  - `resourceUnits`
- 数据来源：
  - `pm_resource_unit`
  - `pm_station`
- 备注：
  - `resourceUnits[].dispatchableCapacityMw` 不是表字段，而是根据站点状态和装机容量实时推导。

### `GET /api/pvms/production-monitor/overview`

- 参数：
  - `resourceUnitId`
- 主要字段：
  - `info`
  - `kpis`
  - `memberStations`
  - `weatherBrief`
  - `alarmBrief`
  - `summaryTable`
- 数据来源：
  - `pm_resource_unit`
  - `pm_station`
  - `pm_weather_snapshot`
  - `pm_station_curve_15m`
- 主要计算：
  - 实时总出力
  - 当日累计电量
  - 上调可调能力
  - 下调可调能力
  - 在线率
  - 预测准确率

### `GET /api/pvms/production-monitor/output`

- 参数：
  - `resourceUnitId`
  - `granularity`，当前支持 `15m`、`30m`、`60m`
- 主要字段：
  - `summary`
  - `curve`
  - `weatherTrend`
  - `contributionRanking`
  - `table`
- 数据来源：
  - `pm_station`
  - `pm_weather_snapshot`
  - `pm_station_curve_15m`
- 主要计算：
  - 粒度聚合
  - 预测偏差率
  - 峰值出力
  - 辐照度趋势
  - 站点贡献排名

### `GET /api/pvms/production-monitor/load`

- 参数：
  - `resourceUnitId`，当前仅保留兼容参数，现阶段不参与过滤
  - `date`，为空时默认 `2026-03-30`
  - `granularity`，当前支持 `15m`、`30m`、`60m`
- 主要字段：
  - `summary`
  - `stations`
- 数据来源：
  - `pm_station`
  - `pm_station_realtime_snapshot`
  - `pm_station_curve_15m`
- 主要计算：
  - 总负荷
  - 总光伏出力
  - 总可调空间
  - 平均爬坡速率
  - 各站电网交互曲线
- 关键说明：
  - 当前逻辑取所有 `load_visible = TRUE` 的站点，返回整张负荷与出力表，目的是先匹配现有前端页面。
  - 后续如果产品要求“按资源单元筛选负荷页”，需要先改前端交互，再让后端真正使用 `resourceUnitId` 过滤。

### `GET /api/pvms/production-monitor/dispatch`

- 参数：
  - `resourceUnitId`
- 主要字段：
  - `summary`
  - `executionTrend`
  - `riskHints`
  - `records`
- 数据来源：
  - `pm_resource_unit`
  - `pm_station`
  - `pm_weather_snapshot`
  - `pm_dispatch_record`
- 主要计算：
  - 指令成功率
  - 平均响应时长
  - 今日已调用量
  - 风险提示

### `GET /api/pvms/production-monitor/weather`

- 参数：
  - `resourceUnitId`
- 主要字段：
  - `summary`
  - `trend`
  - `impactTable`
- 数据来源：
  - `pm_resource_unit`
  - `pm_weather_snapshot`
- 说明：
  - 当前前端主路由未直接展示该接口，但后端契约已可用，可供后续“天气研判”页或独立卡片接入。

## H2 表结构说明

当前生产监控模块依赖以下 6 张表：

### `pm_resource_unit`

- 作用：
  - 资源单元主数据。
- 典型字段：
  - `id`
  - `name`
  - `region`
  - `city`
  - `status`
  - `cluster_radius_km`
  - `dispatch_mode`
  - `strategy_label`
  - `latest_alarm_title`
  - `latest_alarm_time`

### `pm_station`

- 作用：
  - 站点主数据和负荷页是否展示的配置。
- 典型字段：
  - `resource_unit_id`
  - `capacity_mw`
  - `weight`
  - `status`
  - `online_rate`
  - `alarm_count`
  - `sort_index`
  - `load_visible`
  - `load_base_kw`

### `pm_station_realtime_snapshot`

- 作用：
  - 站点实时快照。
- 典型字段：
  - `snapshot_time`
  - `realtime_power_kw`
  - `load_kw`
  - `availability`
  - `health_grade`
- 说明：
  - 当前数据由 `data.sql` 基于 `pm_station_curve_15m` 在 `14:00` 的时段推导得到。

### `pm_weather_snapshot`

- 作用：
  - 资源单元天气快照。
- 典型字段：
  - `weather`
  - `cloudiness`
  - `temperature`
  - `irradiance`
  - `humidity`
  - `wind_speed`
  - `conclusion`

### `pm_station_curve_15m`

- 作用：
  - 15 分钟级别的基础时序事实表。
- 典型字段：
  - `biz_date`
  - `time_slot`
  - `load_kw`
  - `pv_power_kw`
  - `forecast_power_kw`
  - `baseline_power_kw`
  - `irradiance`
  - `temperature`
- 说明：
  - `30m` 和 `60m` 不是单独存表，而是运行时聚合得到。

### `pm_dispatch_record`

- 作用：
  - 调度执行记录。
- 典型字段：
  - `issued_at`
  - `command_type`
  - `target_power_mw`
  - `actual_power_mw`
  - `response_seconds`
  - `status`
  - `deviation_reason`

## 数据来源边界

这部分最容易被误解，必须明确：

### 目前已经放进数据库的内容

- 资源单元元数据
- 站点元数据
- 天气快照
- 15 分钟时序曲线
- 实时快照
- 调度记录

这些数据当前都是开发态种子数据，目的是让后端能在真实接口路径上返回完整结构。

### 目前仍然是“造”的，但造在后端

- 光伏生产数据
- 预测数据
- 负荷曲线
- 调度记录
- 天气快照

它们不是来自真实外部系统，而是通过 `data.sql` 的固定数据和批量生成逻辑灌进 H2。

### 目前在后端实时计算的内容

- `dispatchableCapacityMw`
- `realtimePowerMw`
- `todayEnergyMwh`
- `upRegulationMw`
- `downRegulationMw`
- `onlineRate`
- `forecastAccuracy`
- `deviationRate`
- `avgRampRate`
- 风险提示和天气影响表

这部分才是后续最应该保留的“业务逻辑层”。

## 关键业务规则

### 状态系数

`ProductionMonitorDataService` 内部定义了资源状态对可调能力的影响系数：

- `normal -> 1.00`
- `warning -> 0.92`
- `maintenance -> 0.78`
- `fault -> 0.58`
- `offline -> 0.22`

这套系数直接影响 `dispatchableCapacityMw` 等指标。

### 上调能力

- 公式：
  - `(资源单元总装机 - 当前实时总出力) * 0.75`
- 说明：
  - 当前是演示态口径，保留了安全折减。
  - 未来接真实调度模型时，应把折减因子和边界条件抽为可配置规则。

### 下调能力

- 公式：
  - `当前实时总出力 * 0.35`

### 预测准确率

- 口径：
  - 先计算所有时点 `|实际 - 预测| / 预测`
  - 再以 `100 - 平均偏差率` 作为准确率

### 最大爬坡速率

- 口径：
  - 先算相邻时段的电网交互量 `负荷 - 光伏出力`
  - 再除以粒度分钟数得到 `kW/min`
  - 最后取最大值

## 当前实现和旧 mock 的关系

- `ProductionMonitorController` 已经切到 `ProductionMonitorDataService`
- 旧的 `ProductionMonitorMockService` 不再是主链路
- 如果你在调试时发现返回结果与文档不一致，优先看：
  - `ProductionMonitorController`
  - `ProductionMonitorDataService`
  - `schema.sql`
  - `data.sql`

不要再把 `ProductionMonitorMockService` 当成当前真实行为。

## 扩展到真实业务系统时怎么做

后续接真实系统时，建议保留现有“后端计算层 + 稳定契约”，只替换数据来源：

1. 资源单元和站点主数据：
   - 可改为从主数据中心、资产台账系统或业务库同步。
2. 天气数据：
   - 可改为接气象服务或第三方天气接口。
3. 生产与预测曲线：
   - 可改为接 SCADA、能管平台、预测服务。
4. 调度记录：
   - 可改为接调度平台或指令执行日志。

建议不要把计算逻辑再次挪回前端。前端应只消费：

- 元数据
- 聚合结果
- 图表时序
- 表格展示数据

## 开发和排障提示

### 查看数据库

- H2 Console：
  - `http://127.0.0.1:8091/h2-console`
- JDBC URL：
  - `jdbc:h2:mem:pvms`
- 用户名：
  - `sa`
- 密码：
  - 空

### 常用排障顺序

1. 看 `application.yml` 是否仍启用了 H2 和 SQL 初始化。
2. 在 H2 Console 查询 6 张 `pm_*` 表是否有数据。
3. 看 controller 是否仍指向 `ProductionMonitorDataService`。
4. 看前端是否真的打到了 `/api/pvms/production-monitor/*`。
5. 跑 `ProductionMonitorControllerTest` 确认契约未破坏。

## 测试现状

- 重点测试文件：
  - `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`
- 当前至少覆盖：
  - `meta`
  - `overview`
  - `output`
  - `load`
  - `dispatch`
  - `weather`
  - `30m` 聚合场景

## 当前已知限制

- `load` 页目前仍是“全局站表”，没有按资源单元过滤。
- 默认业务日期仍固定回落到 `2026-03-30`，没有接实际时间窗口和历史数据分区。
- 返回结构仍以 `Map<String, Object>` 为主，不是强类型 DTO。
- 前端的“电站监控模式”仍走 `stationarchive` 链路，不在本模块 H2 改造范围内。

## 维护建议

### 短期

- 继续把前端负荷页改成真正使用 `resourceUnitId`、`date` 和 `granularity`
- 为 `production-monitor` 补 DTO，减少契约漂移风险
- 给 `load` 和 `dispatch` 增加更多边界测试

### 中期

- 把 H2 种子数据迁移为可切换的 dev profile 数据库
- 引入真实数据同步或采集任务
- 把关键算法参数抽为配置

### 长期

- 对接真实预测、设备、收益、调度系统
- 建立历史数据归档和时间窗口查询能力
- 补统一异常处理、审计日志和权限控制
