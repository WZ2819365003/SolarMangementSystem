# Forecast 前端模块

## 1. 模块定位

该模块对应菜单“预测与分析”，承担以下三个子视图：

- 预测总览
- 可调能力分析
- 精度评估

页面入口：

- `frontend/src/modules/forecast/pages/ForecastPage.vue`

## 2. 路由

| 路由 | viewKey | 说明 |
| --- | --- | --- |
| `/forecast/overview` | `overview` | 预测总览 |
| `/forecast/adjustable` | `adjustable` | 可调能力分析 |
| `/forecast/accuracy` | `accuracy` | 精度评估 |

## 3. 核心文件

| 路径 | 作用 |
| --- | --- |
| `pages/ForecastPage.vue` | 模块总入口，负责 tabs、filter、数据请求和视图切换 |
| `components/ForecastHero.vue` | 页面顶部说明区 |
| `components/ForecastTabNav.vue` | 子标签导航 |
| `components/ForecastFilterBar.vue` | 查询条件条 |
| `components/views/ForecastOverviewView.vue` | 总览视图 |
| `components/views/ForecastAdjustableView.vue` | 可调能力视图 |
| `components/views/ForecastAccuracyView.vue` | 精度评估视图 |

## 4. 页面数据流

### 元数据

页面创建时先调用：

- `fetchForecastMeta()`

用途：

- 生成区域选项
- 生成电站选项

### 总览视图

总览视图会并发调用：

- `fetchForecastOverview`
- `fetchForecastComparison`
- `fetchForecastDeviationHeatmap`

然后在 `ForecastPage.vue` 中被拼成：

```js
{
  ...overviewData,
  comparison: comparisonData,
  heatmap: heatmapData
}
```

### 可调能力视图

单独调用：

- `fetchForecastAdjustable`

### 精度评估视图

单独调用：

- `fetchForecastAccuracy`

## 5. 当前前端视图期望的数据结构

### `ForecastOverviewView.vue`

当前主要读取：

- `viewData.kpis`
- `viewData.stationTable`
- `viewData.comparison.timeLabels`
- `viewData.comparison.series`
- `viewData.heatmap.hours`
- `viewData.heatmap.stations`
- `viewData.heatmap.data`

### `ForecastAdjustableView.vue`

当前主要读取：

- `viewData.kpis`
- `viewData.stationTable`
- `viewData.capacityCurve`
- `viewData.timeline`

### `ForecastAccuracyView.vue`

当前主要读取：

- `viewData.kpis`
- `viewData.monthlyTable`
- `viewData.trend`
- `viewData.distribution`
- `viewData.stationRanking`

## 6. 当前维护风险

这是当前前端最需要注意的模块之一。

原因：

- 前端视图读取的字段结构，与后端 `forecast` 模块当前返回结构并不一致
- 开发态默认又先走前端 mock，所以本地页面不容易第一时间暴露问题

维护本模块时，不要只看：

- `frontend/src/shared/mock/forecast.js`

还要同时看：

- `backend/src/main/java/.../forecast/controller/ForecastController.java`
- `backend/src/main/java/.../forecast/service/ForecastMockService.java`

## 7. 维护建议

### 改图表或页面布局时

优先检查：

- `viewData` 实际字段来源
- 图表初始化和 resize 逻辑
- `comparison` 和 `heatmap` 是否仍满足组件期望

### 改接口字段时

必须同时同步：

- `api/pvms.js`
- `shared/mock/forecast.js`
- 后端 forecast 模块
- 本文档和后端模块文档

## 8. 推荐阅读文件

- `frontend/src/modules/forecast/pages/ForecastPage.vue`
- `frontend/src/modules/forecast/components/views/ForecastOverviewView.vue`
- `frontend/src/modules/forecast/components/views/ForecastAdjustableView.vue`
- `frontend/src/modules/forecast/components/views/ForecastAccuracyView.vue`

