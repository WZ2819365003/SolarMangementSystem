# Forecast 后端模块

## 1. 模块定位

该模块对应后端“预测与分析”能力，提供以下接口：

- 元数据
- 预测总览
- 可调能力分析
- 精度评估
- 预测对比
- 偏差热力图

当前实现定位不是生产预测引擎，而是“用于页面联调和演示的 mock 服务层”。

## 2. 核心代码位置

| 路径 | 作用 |
| --- | --- |
| `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastController.java` | HTTP 接口入口 |
| `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastMockService.java` | mock 数据生成与装配 |
| `backend/src/test/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastControllerTest.java` | 接口测试 |

## 3. 路由清单

| 路由 | 说明 | 参数 |
| --- | --- | --- |
| `GET /api/pvms/forecast/meta` | 元数据 | 无 |
| `GET /api/pvms/forecast/overview` | 预测总览 | `stationId?` |
| `GET /api/pvms/forecast/adjustable` | 可调能力 | `stationId?` |
| `GET /api/pvms/forecast/accuracy` | 精度评估 | `stationId?`、`dateRange?` |
| `GET /api/pvms/forecast/comparison` | 预测与实际对比 | `stationId?`、`date?` |
| `GET /api/pvms/forecast/deviation-heatmap` | 偏差热力图 | `dateRange?` |

## 4. 当前真实返回结构

### `GET /meta`

当前返回核心字段：

- `stations`
- `models`
- `granularityOptions`

### `GET /overview`

当前返回核心字段：

- `kpi`
- `accuracyTrend`
- `forecastVsActual`

### `GET /adjustable`

当前返回核心字段：

- `summary`
- `timeline`

### `GET /accuracy`

当前返回核心字段：

- `overall`
- `byStation`
- `byHour`

### `GET /comparison`

当前返回核心字段：

- `stationId`
- `date`
- `timeAxis`
- `forecast`
- `actual`
- `deviation`

### `GET /deviation-heatmap`

当前返回核心字段：

- `stations`
- `hours`
- `matrix`

## 5. 数据是怎么来的

当前 `ForecastMockService` 主要使用以下方式生成数据：

- 固定电站列表
- 固定容量数据
- 按时间循环生成 96 点序列
- 使用 `sin/cos` 近似日照和出力变化
- 使用 `Math.random()` 制造精度和偏差波动

这意味着：

- 数据结构稳定性还可以
- 但数据业务真实性有限
- 每次请求结果可能有轻微波动

## 6. 当前已知问题

这是当前前后端契约漂移最明显的后端模块之一。

### 前端期望

前端 forecast 页面当前更偏向读取：

- `kpis`
- `stationTable`
- `comparison.timeLabels`
- `comparison.series`
- `heatmap.data`

### 后端实际

后端当前返回的是：

- `kpi`
- `accuracyTrend`
- `forecastVsActual`
- `summary`
- `matrix`

结论：

- 现在后端接口即使服务正常，也不能直接保证前端页面按真实接口无缝渲染

## 7. 测试覆盖

测试文件：

- `ForecastControllerTest.java`

当前覆盖范围：

- 元数据
- 总览
- 可调能力
- 精度评估
- 对比
- 热力图

这说明后端接口层本身是可回归的，但测试验证的是“后端当前结构正确”，不是“前后端结构一致”。

## 8. 维护建议

### 如果只是补演示数据

可以继续在 `ForecastMockService` 中调整：

- 电站列表
- 曲线生成
- KPI 数值
- 热力图矩阵

### 如果目标是前后端联调

必须先做：

1. 统一字段命名
2. 明确 overview / adjustable / accuracy 三个视图到底要什么结构
3. 再决定前端适配后端，还是后端适配前端

### 如果目标是走向真实后端

建议路线：

1. 先设计 DTO
2. 再把 `Map<String, Object>` 改成明确返回模型
3. 最后再接真实预测数据源

