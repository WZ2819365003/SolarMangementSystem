# Frontend 模块文档索引

本目录记录前端各模块的页面职责、路由、核心文件和接口调用情况。

## 建议阅读顺序

1. `infrastructure.md`
2. `dashboard.md`
3. `production-monitor.md`
4. `stations.md`
5. `station-monitoring.md`
6. `forecast.md`
7. `strategy.md`
8. `devices.md`
9. `alarms.md`

## 模块说明

| 文档 | 对应模块 | 说明 |
| --- | --- | --- |
| `infrastructure.md` | 前端基础设施 | 运行环境、UI 库、地图依赖、宿主适配 |
| `dashboard.md` | 综合监控中心 | 首页聚合页 |
| `production-monitor.md` | 生产监控 | 资源总览 + 电站监控双模式 |
| `stations.md` | 电站档案 / 电站监控组件 | 树、详情面板、逆变器等复用组件 |
| `station-monitoring.md` | 站点与资源监控 | 资源单元和单站视角 |
| `forecast.md` | 预测与分析 | 三个子视图、图表和契约风险 |
| `strategy.md` | 策略管理 | 列表、配置、收益、批量操作 |
| `devices.md` | 设备监控 | 设备状态和分布 |
| `alarms.md` | 告警中心 | 告警列表和处置态势 |

## 维护提醒

- 前端接口总入口在 `frontend/src/api/pvms.js`
- 开发态请求行为取决于 `frontend/src/shared/host/bridge.js`
- `production-monitor` 会复用 `stations` 组件和 `stationarchive` 后端接口，维护时不要孤立看待

