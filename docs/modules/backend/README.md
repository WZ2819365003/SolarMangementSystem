# Backend 模块文档索引

本目录记录后端各业务模块的接口职责、路由前缀和测试覆盖情况。

## 模块总览

| 文档 | 路由前缀 | 说明 |
| --- | --- | --- |
| `system.md` | `/api/system` | 健康检查 |
| `dashboard.md` | `/api/pvms/dashboard` | 首页地图、KPI、曲线、排名等 |
| `device-alarm.md` | `/api/pvms/devices/monitor`、`/api/pvms/alarms/center` | 设备监控与告警中心 |
| `adjustable-capacity.md` | `/api/pvms/adjustable-capacity` | 可调容量相关接口 |
| `stations.md` | `/api/pvms/resource-units` | 资源单元列表和详情 |
| `station-archive.md` | `/api/pvms/station-tree`、`/api/pvms/station-archive/*` | 电站树、公司总览、单站实时等 |
| `production-monitor.md` | `/api/pvms/production-monitor` | 生产监控总览接口 |
| `forecast.md` | `/api/pvms/forecast` | 预测与分析 |
| `strategy.md` | `/api/pvms/strategy` | 策略管理 |

## 当前维护重点

当前最值得先看的后端模块不是功能最多的，而是契约风险最大的：

1. `forecast.md`
2. `strategy.md`
3. `production-monitor.md`
4. `station-archive.md`

补充说明：

- `production-monitor.md` 当前已更新为 H2 + 后端计算链路说明，不再只是旧 mock 描述。
- `dashboard.md` 也已经包含地图动态点位下沉后的维护边界。

## 维护提醒

- 当前后端大量使用 `*MockService`
- 接口测试主要集中在 `backend/src/test/java/.../module/`
- 如果改了返回结构，必须同步确认前端页面读取字段和交接文档
