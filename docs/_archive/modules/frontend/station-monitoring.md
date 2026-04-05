# Production Monitor Frontend Module

## 1. 模块定位

`production-monitor` 是 M02 的前端壳层。

它现在不是一个单页面，而是两个模式共存：

1. 资源总览模式  
   面向聚合资源单元，后端接口前缀是 `/api/pvms/production-monitor/*`

2. 电站监控模式  
   面向公司 / 电站 / 逆变器，后端接口前缀是 `/api/pvms/station-tree` 和 `/api/pvms/station-archive/*`

理解这两个模式的边界，是继续维护 M02 的前提。

## 2. 页面入口

- 页面文件  
  `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`

- API 封装  
  `frontend/src/api/pvms.js`

- 电站监控复用组件  
  `frontend/src/modules/stations/components/`

## 3. 当前路由与模式

当前主入口仍然是：

- `/production-monitor/overview`
- `/production-monitor/output`
- `/production-monitor/dispatch`
- `/production-monitor/weather`

但在同一套页面壳里，用户可以切到“电站监控模式”。

切到电站监控模式后：
- 左侧显示公司 / 电站 / 逆变器树
- 右侧根据选中节点显示公司总览、电站图表或逆变器详情

## 4. 当前前后端边界

### 资源总览模式

后端已经完成：
- H2 种子数据
- 资源单元、天气、调度、负荷与出力计算

核心接口：
- `/api/pvms/production-monitor/meta`
- `/api/pvms/production-monitor/overview`
- `/api/pvms/production-monitor/output`
- `/api/pvms/production-monitor/load`
- `/api/pvms/production-monitor/dispatch`
- `/api/pvms/production-monitor/weather`

### 电站监控模式

后端也已经完成：
- `station-tree`
- `company-overview`
- `resource-overview`
- `station-realtime`
- `station-adjustable`
- `station-strategy`
- `inverter-realtime`

也就是说，M02 当前两条主链都已经不再依赖前端本地 mock 才能联调。

## 5. 查询状态

M02 页面顶部共享以下查询状态：

- `resourceUnitId`
- `region`
- `city`
- `date`
- `granularity`

但要注意：

- 资源总览模式主要消费 `resourceUnitId`
- 电站监控模式主要消费 `stationId / inverterId / companyId`
- `date` 和 `granularity` 在电站模式下会传给 `station-realtime`

## 6. 开发时怎么判断问题在哪一层

### 如果是资源总览模式有问题

优先检查：
- `production-monitor` 接口是否返回正确
- 资源单元筛选状态是否变化
- `overview / output / load / dispatch / weather` 五个视图是否只刷新当前视图

### 如果是电站监控模式有问题

优先检查：
- `station-tree` 是否返回三层节点
- 当前选中节点类型是否正确
- `station-realtime` 的 `metric` 是否正确传出
- `inverter-realtime` 是否只传了 `inverterId`

## 7. 维护建议

### 新人先读这三份文档

1. `docs/modules/frontend/stations.md`
2. `docs/modules/backend/station-archive.md`
3. `docs/modules/backend/production-monitor.md`

### 改 M02 页面前先确认

1. 你改的是资源总览模式还是电站监控模式
2. 对应接口前缀是不是同一组
3. 当前问题是页面状态问题、契约问题，还是后端计算问题

### 不要再默认认为“电站监控是前端 mock”

截至 `2026-03-30`，这个判断已经过时。

正确判断是：
- 开发环境底图仍然是前端展示
- 资源总览数据已经在后端 H2
- 电站监控数据也已经在后端 H2
