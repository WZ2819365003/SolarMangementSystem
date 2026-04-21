# Station Archive Backend Module

## 1. 模块定位

`station-archive` 是“电站监控 / 电站档案”这一条后端链路的统一承载模块。

它服务的前端场景有两类：

1. 电站档案列表页  
   入口是 `GET /api/pvms/stations/archive`

2. 生产监控中的“电站监控模式”  
   入口是 `GET /api/pvms/station-tree` 和 `GET /api/pvms/station-archive/*`

这条链路在 `2026-03-30` 已从旧的 `MockService` 迁移到 `H2 + repository + 后端计算` 模式。

## 2. 代码入口

- Controller  
  `backend/src/main/java/cn/techstar/pvms/backend/module/stationarchive/controller/StationArchiveController.java`

- H2 业务服务  
  `backend/src/main/java/cn/techstar/pvms/backend/module/stationarchive/service/StationArchiveDataService.java`

- 时序插值与粒度转换  
  `backend/src/main/java/cn/techstar/pvms/backend/module/stationarchive/service/StationArchiveSeriesService.java`

- 数据访问层  
  `backend/src/main/java/cn/techstar/pvms/backend/module/stationarchive/repository/`

- 数据初始化脚本  
  `backend/src/main/resources/schema.sql`  
  `backend/src/main/resources/data.sql`

## 3. 当前接口清单

### `GET /api/pvms/stations/archive`

用途：
- 电站档案列表

请求参数：
- `keyword?`
- `gridStatus?`

返回重点：
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

### `GET /api/pvms/station-tree`

用途：
- 生产监控站点模式左侧树

请求参数：
- `status?`  
  可用于筛选 `normal / warning / fault / maintenance / offline`

返回重点：
- `data.tree`
- `company -> station -> inverter` 三层结构
- `nodeType`
- `label`
- `extra.status`
- `extra.dataQuality`
- `extra.loadKw`
- `extra.pvOutputKw`
- `extra.adjustableKw`

### `GET /api/pvms/station-archive/company-overview`

用途：
- 公司级右侧总览面板

请求参数：
- `companyId`

返回重点：
- `data.name`
- `data.kpis`
- `data.stations`

### `GET /api/pvms/station-archive/resource-overview`

用途：
- 单电站独立总览

请求参数：
- `stationId`

返回重点：
- `data.name`
- `data.kpis`
- `data.powerCurve`
- `data.stations`
- `data.inverters`

### `GET /api/pvms/station-archive/station-realtime`

用途：
- 电站级图表主接口

请求参数：
- `stationId`
- `metric?`  
  支持 `adjustable / pv-output / load / forecast`
- `date?`
- `granularity?`  
  当前支持 `15min / 15m / 1min / 1m`

返回重点：
- `data.times`
- `data.series`
- `data.stationKpis`
- `data.monthlyStats`
- `data.fields`

说明：
- `adjustable` 会返回最完整的数据结构
- `forecast` 在 `1min` 粒度下返回 1440 点，由后端在 15 分钟原始曲线基础上插值生成

### `GET /api/pvms/station-archive/station-adjustable`

用途：
- 兼容旧页面的可调摘要接口

请求参数：
- `stationId`

返回重点：
- `data.currentPowerKw`
- `data.maxAdjustableKw`
- `data.minAdjustableKw`
- `data.adjustableRangeKw`
- `data.periods`

### `GET /api/pvms/station-archive/station-strategy`

用途：
- 电站策略只读面板

请求参数：
- `stationId`

返回重点：
- `data.currentStrategy`
- `data.executionLogs`

### `GET /api/pvms/station-archive/inverter-realtime`

用途：
- 逆变器详情侧板

请求参数：
- `inverterId`

兼容说明：
- Controller 仍兼容旧的 `stationId` 传法
- 如果前端只传 `inverterId`，后端会直接按逆变器 ID 解析，不再要求 `stationId`

返回重点：
- `data.ratedPowerKw`
- `data.realtimePowerKw`
- `data.dailyEnergyMwh`
- `data.efficiency`
- `data.powerCurve`
- `data.topology`
- `data.alarms`
- `data.deviceInfo`

## 4. H2 数据模型

### 主数据表

- `sa_company`
  公司主数据，保存公司名称、区域、排序

- `sa_station`
  电站主数据，保存公司归属、容量、状态、数据质量、并网信息、地址、基础负荷

- `sa_inverter`
  逆变器主数据，保存逆变器容量、设备型号、厂家、序列号、MPPT、拓扑参数

- `sa_station_strategy`
  电站当前策略快照，保存当前策略名、类型、状态、开始结束时间、目标功率、预计收益

- `sa_inverter_alarm`
  逆变器告警快照

### 时序表

- `sa_station_curve_15m`
  电站 15 分钟粒度原始曲线

字段包括：
- `load_kw`
- `pv_output_kw`
- `forecast_day_ahead_kw`
- `forecast_ultra_short_kw`

## 5. 后端计算边界

这是交接时最重要的部分。

### 直接落库的内容

- 公司
- 电站
- 逆变器
- 策略快照
- 告警快照
- 15 分钟原始曲线

### 运行时计算的内容

- 左侧树节点上的实时负荷、实时出力、可调空间
- 公司级 KPI
- 单电站的可调空间 KPI、月度统计、VPP 字段
- 负荷分项曲线
- 逆变器功率曲线
- 逆变器拓扑字符串结构
- 1 分钟粒度插值曲线
- 策略执行日志

### 为什么这样拆

因为后续接真实系统时，最容易替换的是“原始事实数据来源”：

- 站点主数据可来自资产系统
- 设备主数据可来自设备台账系统
- 曲线可来自 SCADA / EMS / 预测服务
- 告警可来自告警中心
- 策略可来自调度平台

而页面上真正消费的：

- KPI
- 树节点摘要
- 图表拼装
- 1 分钟粒度
- 拓扑展示

这些都更适合留在后端统一计算和装配。

## 6. 关键实现说明

### 树结构

树结构不是数据库原样返回，而是后端按下面顺序装配：

1. 读取公司列表
2. 读取电站主数据
3. 读取当日 15 分钟曲线
4. 取 `14:00` 时刻作为当前摘要值
5. 读取逆变器主数据
6. 拼成 `company -> station -> inverter`

### 1 分钟粒度

数据库只存 15 分钟粒度。

当前 `1min` 响应由 `StationArchiveSeriesService` 在内存中做线性插值，目的是：

- 满足前端 1 分钟图表交互
- 保持种子数据规模可控
- 未来接真实分钟级数据时，可直接替换实现，不必改前端契约

### 逆变器功率

当前逆变器功率曲线不是单独落库，而是：

1. 先取电站总出力曲线
2. 按逆变器额定功率和设备状态分摊
3. 得到每台逆变器的功率序列

这意味着它本质上是开发环境“可解释的推导数据”，不是实际采集曲线。

## 7. 测试

测试文件：
- `backend/src/test/java/cn/techstar/pvms/backend/module/stationarchive/controller/StationArchiveControllerTest.java`

当前覆盖：
- 档案列表
- 关键字过滤
- 状态筛选树
- 公司总览
- 电站总览
- `adjustable` 图表结构
- `1min` 粒度
- 旧可调摘要接口
- 策略只读接口
- 逆变器详情接口

建议维护动作：

1. 先改测试，再改实现
2. 涉及字段变更时，先确认前端组件真实消费哪些字段
3. 新增曲线粒度或新 metric 时，优先在 `StationArchiveSeriesService` 扩展

## 8. 后续扩展建议

### 第一优先级

- 为 `station-archive` 补 DTO，降低 `Map<String, Object>` 风险
- 把当前告警级别、策略状态这类枚举提到统一常量层

### 第二优先级

- 如果后端开始接真实分钟级采集数据，替换 `1min` 插值逻辑
- 如果设备监控继续深化，为逆变器增加独立时序表

### 第三优先级

- 将公司、电站、逆变器与生产监控的资源单元模型建立映射关系
- 统一 `production-monitor` 与 `station-archive` 的状态字典和指标公式
