# Forecast 前端模块

## 1. 页面定位

前端 `forecast` 模块对应“预测分析 / M03”，页面入口是：

- `frontend/src/modules/forecast/pages/ForecastPage.vue`

这个模块当前有 3 个子视图：

- `overview`
  预测总览
- `adjustable`
  可调能力分析
- `accuracy`
  精度评估

## 2. 页面组成

### 核心文件

- `frontend/src/modules/forecast/pages/ForecastPage.vue`
- `frontend/src/modules/forecast/components/ForecastHero.vue`
- `frontend/src/modules/forecast/components/ForecastTabNav.vue`
- `frontend/src/modules/forecast/components/ForecastFilterBar.vue`
- `frontend/src/modules/forecast/components/views/ForecastOverviewView.vue`
- `frontend/src/modules/forecast/components/views/ForecastAdjustableView.vue`
- `frontend/src/modules/forecast/components/views/ForecastAccuracyView.vue`

### API 入口

- `frontend/src/api/pvms.js`

使用到的接口：

- `fetchForecastMeta`
- `fetchForecastOverview`
- `fetchForecastComparison`
- `fetchForecastDeviationHeatmap`
- `fetchForecastAdjustable`
- `fetchForecastAccuracy`

## 3. 数据流怎么走

### 3.1 页面初始化

`ForecastPage.vue` 在 `created()` 中做两件事：

- `loadMeta()`
- `loadCurrentView()`

### 3.2 元数据

`loadMeta()` 调用：

- `GET /api/pvms/forecast/meta`

返回值主要用于：

- 生成区域筛选
- 生成电站筛选
- 维持默认查询状态

### 3.3 总览页数据

`overview` 不是单接口，而是三个接口并发后再拼装：

- `GET /api/pvms/forecast/overview`
- `GET /api/pvms/forecast/comparison`
- `GET /api/pvms/forecast/deviation-heatmap`

在页面中最终被组合成：

```js
{
  ...overviewData,
  comparison: comparisonData,
  heatmap: heatmapData
}
```

这点很关键。维护时不要误以为 `ForecastOverviewView.vue` 的所有字段都来自单个接口。

### 3.4 可调能力页

`adjustable` 直接使用：

- `GET /api/pvms/forecast/adjustable`

### 3.5 精度评估页

`accuracy` 直接使用：

- `GET /api/pvms/forecast/accuracy`

## 4. 视图字段契约

### 4.1 `ForecastOverviewView.vue`

这个视图当前消费的数据结构是：

- `viewData.kpis`
- `viewData.stationTable`
- `viewData.comparison.timeLabels`
- `viewData.comparison.series.dayAhead`
- `viewData.comparison.series.ultraShort`
- `viewData.comparison.series.actual`
- `viewData.heatmap.hours`
- `viewData.heatmap.stations`
- `viewData.heatmap.data`

### 4.2 `ForecastAdjustableView.vue`

这个视图当前消费的数据结构是：

- `viewData.kpis.totalAdjustable`
- `viewData.kpis.upAdjustable`
- `viewData.kpis.downAdjustable`
- `viewData.kpis.max24h`
- `viewData.capacityCurve.timeLabels`
- `viewData.capacityCurve.predicted`
- `viewData.capacityCurve.upperBound`
- `viewData.capacityCurve.lowerBound`
- `viewData.timeline`
- `viewData.stationTable`

### 4.3 `ForecastAccuracyView.vue`

这个视图当前消费的数据结构是：

- `viewData.kpis.mae`
- `viewData.kpis.rmse`
- `viewData.kpis.qualifiedRate`
- `viewData.kpis.monthlyAvgAccuracy`
- `viewData.trend`
- `viewData.distribution`
- `viewData.stationRanking`
- `viewData.monthlyTable`

## 5. 当前联调状态

截至 `2026-03-30`：

- 开发环境前端已经默认走后端代理，不再优先走前端 mock
- `forecast` 后端接口已经切到 H2 + 后端计算链路
- 页面现在真正消费的是 Spring Boot 返回的结构，而不是前端本地假数据

这意味着之后只要改了后端字段，前端页面在本地就会立刻暴露问题，不会再像之前那样被 mock 掩盖。

## 6. 常见维护动作

### 改筛选项

优先看：

- `ForecastFilterBar.vue`
- `ForecastPage.vue` 中 `query`
- `fetchForecastMeta`

### 改总览图表

优先看：

- `ForecastOverviewView.vue`
- `ForecastPage.vue` 中 overview 的 `Promise.all` 拼装
- 后端 `/overview`、`/comparison`、`/deviation-heatmap`

### 改可调能力图表

优先看：

- `ForecastAdjustableView.vue`
- 后端 `/adjustable`

### 改精度评估图表

优先看：

- `ForecastAccuracyView.vue`
- 后端 `/accuracy`

## 7. 前端维护时最容易犯的错

### 把后端聚合逻辑搬回前端

不建议：

- 前端自己重新算 MAE / RMSE
- 前端自己拼可调上下界
- 前端自己根据原始点位重算热力图

这些逻辑应该继续留在后端，前端只负责展示。

### 忽略 `ForecastPage.vue` 的二次拼装

如果只盯着某个 view 组件，很容易误判数据来源。  
尤其是 `overview`，它并不是单接口。

### 只改页面，不同步文档

只要改了下面任何一项，都需要同步文档：

- 查询参数
- 视图字段
- 图表含义
- 筛选逻辑

## 8. 排障顺序

如果页面没数据，优先按这个顺序查：

1. 浏览器网络请求有没有命中 `/api/pvms/forecast/*`
2. `http://127.0.0.1:8091/api/pvms/forecast/meta` 是否正常
3. `ForecastPage.vue` 的 `viewData` 是否成功赋值
4. 具体视图组件引用的字段名是否和后端一致

## 9. 结论

前端 `forecast` 模块现在已经从“依赖 mock 的演示页”升级为“依赖后端真实契约的联调页”。  
新人接手时，应该把它当成真实前后端模块来维护，而不是把它当成静态 demo 页面。
