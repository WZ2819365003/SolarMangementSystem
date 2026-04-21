# Forecast 后端模块

## 1. 模块定位

`forecast` 对应系统菜单中的“预测分析 / M03”。  
截至 `2026-03-30`，这个模块已经从“纯 mock 返回”切到“`H2 种子数据 + 系统内真实链路取数 + 后端计算拼装`”模式。

当前它不是一个真正的预测算法引擎，而是一个可联调、可维护、可继续替换数据源的预测服务外壳。它承担三类职责：

- 对前端输出稳定的预测分析接口契约
- 从系统内已有模块抽取实际生产事实数据
- 基于预测元数据和误差样本，在后端计算 KPI、精度、偏差、可调能力等结果

## 2. 数据来源边界

这个模块现在刻意把“事实数据”和“预测元数据”分开。

### 2.1 系统内事实数据

来自当前系统已经维护的生产/电站链路：

- `sa_station`
  电站主数据，提供站点、公司、区域、状态、排序、容量等基础信息
- `sa_station_curve_15m`
  15 分钟级生产事实曲线，提供负荷、实际出力、历史预测参考值

这些数据代表“系统里真实存在的业务对象和生产侧事实”。后续如果切到 MySQL、时序库或外部采集系统，优先替换的就是这一层。

### 2.2 预测元数据

目前仍由 H2 种子数据维护：

- `fc_model`
  预测模型定义
- `fc_station_model_binding`
  电站与模型绑定关系
- `fc_prediction_series_15m`
  15 分钟级预测结果、上下界、场景标签
- `fc_error_sample`
  近 30 天小时级误差样本
- `fc_monthly_accuracy_snapshot`
  月度精度快照
- `fc_adjustable_window`
  可调能力时间窗

这一层的意义不是“假装真实生产”，而是为页面、接口和后端计算提供稳定输入。未来接真实预测平台时，优先替换这一层。

### 2.3 后端实时计算结果

以下内容不直接存表，而是在 service 中实时拼装：

- `overview` 的 KPI
- `comparison` 的曲线序列
- `deviation-heatmap` 的站点 x 小时误差矩阵
- `adjustable` 的当前可调、4h 预测、24h 上下界聚合
- `accuracy` 的 MAE、RMSE、趋势、分布、排名、月度对比

这部分是后端真正的业务价值所在。即使未来预测元数据换成外部系统，这些聚合规则仍然应该保留在后端。

## 3. 核心代码位置

### 控制器

- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastController.java`

### 服务

- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastDataService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastSeriesService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastMetricsCalculator.java`

### 数据访问

- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastStationRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastCurveRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastPredictionRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastModelRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastErrorSampleRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastMonthlyAccuracyRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastAdjustableWindowRepository.java`

### 初始化脚本

- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

### 测试

- `backend/src/test/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastControllerTest.java`

## 4. 接口清单

### `GET /api/pvms/forecast/meta`

用途：
- 初始化页面区域选项、电站选项、预测类型、模型信息

主要返回：
- `defaultDate`
- `defaultStationId`
- `regions`
- `stations`
- `forecastTypes`
- `models`

### `GET /api/pvms/forecast/overview`

查询参数：
- `region?`
- `stationId?`
- `forecastType?`

主要返回：
- `kpis.dayAheadAccuracy`
- `kpis.ultraShortAccuracy`
- `kpis.dailyDeviation`
- `kpis.qualifiedRate`
- `stationTable`

### `GET /api/pvms/forecast/comparison`

查询参数：
- `region?`
- `stationId?`

主要返回：
- `stationId`
- `stationName`
- `timeLabels`
- `series.dayAhead`
- `series.ultraShort`
- `series.actual`

### `GET /api/pvms/forecast/deviation-heatmap`

查询参数：
- `region?`
- `stationId?`
- `forecastType?`
- `startDate?`
- `endDate?`

主要返回：
- `hours`
- `stations`
- `data`

### `GET /api/pvms/forecast/adjustable`

查询参数：
- `region?`
- `stationId?`
- `forecastType?`

主要返回：
- `kpis.totalAdjustable`
- `kpis.upAdjustable`
- `kpis.downAdjustable`
- `kpis.max24h`
- `capacityCurve`
- `timeline`
- `stationTable`

### `GET /api/pvms/forecast/accuracy`

查询参数：
- `region?`
- `stationId?`
- `forecastType?`
- `startDate?`
- `endDate?`

主要返回：
- `kpis.mae`
- `kpis.rmse`
- `kpis.qualifiedRate`
- `kpis.monthlyAvgAccuracy`
- `trend`
- `distribution`
- `stationRanking`
- `monthlyTable`

## 5. 关键计算逻辑

### 5.1 总览 KPI

输入：
- `sa_station_curve_15m` 的实际出力
- `fc_prediction_series_15m` 的日前/超短期预测

计算：
- `dayAheadAccuracy`
  按 96 点曲线计算相对偏差平均值，再换算成精度百分比
- `ultraShortAccuracy`
  同上，但使用 `ultra-short`
- `dailyDeviation`
  取当前选中预测类型的日内平均绝对误差，单位转成 `MW`
- `qualifiedRate`
  以相对偏差阈值做合格率统计

### 5.2 偏差热力图

输入：
- `fc_error_sample`

计算：
- 以“站点 x 小时”维度聚合近 7 天或指定时间范围的误差样本
- 每个单元格是该小时绝对误差均值，单位 `MW`

### 5.3 可调能力分析

输入：
- `sa_station_curve_15m.load_kw`
- `fc_prediction_series_15m.predicted_power_kw`
- `fc_prediction_series_15m.upper_bound_kw`
- `fc_prediction_series_15m.lower_bound_kw`
- `fc_adjustable_window`

计算思路：
- `predictedAdjustable = max(load - predicted, 0)`
- `upperAdjustable = max(load - lowerBound, 0)`
- `lowerAdjustable = max(load - upperBound, 0)`
- 当前时刻用 `CURRENT_SLOT = 56` 代表 `14:00`
- `predicted4h` 取当前时刻后 16 个点的平均值
- `max24h` 取 96 点聚合曲线中的最大值

注意：
- 这里是“开发态预测分析算法”，不是生产环境调控策略
- 后续接入真实调度系统时，可以替换预测输入，但聚合思路保留在后端

### 5.4 精度评估

输入：
- `fc_error_sample`
- `fc_monthly_accuracy_snapshot`

计算：
- `MAE`
  平均绝对误差
- `RMSE`
  均方根误差
- `qualifiedRate`
  直接基于误差样本里的 `qualified`
- `trend`
  对 30 天内每日误差样本做天级精度反推
- `distribution`
  将误差值按 10 个桶做分布统计
- `stationRanking`
  以站点误差样本反推出精度后排序
- `monthlyTable`
  按月聚合 `mae/rmse/accuracy`，并计算环比提升

## 6. 维护时先看什么

如果你要改预测模块，优先看下面 4 个点：

1. 这次变更改的是“事实数据来源”还是“预测元数据来源”
2. 这次变更影响的是哪个接口契约
3. 指标应该存库，还是应该由 service 动态计算
4. 前端消费的是单接口字段，还是 `ForecastPage.vue` 二次拼装后的 `viewData`

## 7. 未来如何扩展到真实系统

推荐替换顺序：

1. 保留 `ForecastController` 和当前返回结构不动
2. 先把 `fc_prediction_series_15m` 替换为真实预测平台结果
3. 再把 `fc_error_sample` 替换为真实评估样本
4. 最后把 `fc_monthly_accuracy_snapshot` 和 `fc_adjustable_window` 改成批任务或实时任务生成

不建议的做法：

- 直接把前端需要的整包 JSON 写死在数据库
- 直接把聚合逻辑搬回前端
- 把 `overview/adjustable/accuracy` 做成互相不一致的独立口径

## 8. 当前限制

- 当前预测数据仍然是 H2 元数据，不是外部预测平台实时结果
- 当前误差样本是开发环境造数，不代表真实站点运行质量
- 当前没有引入 DTO 强类型返回模型，仍以 `Map<String, Object>` 为主
- 当前没有做权限、审计、任务调度、模型版本切换闭环

## 9. 结论

截至 `2026-03-30`，`forecast` 已经具备以下维护基础：

- 本地可联调
- 有稳定后端接口
- 有 H2 数据底座
- 有后端计算逻辑
- 有回归测试
- 有明确扩展边界

这意味着它已经从“前端演示模块”进入“可交接维护模块”阶段。
