# Backend 模块文档索引

这组文档记录后端各业务模块的接口职责、数据来源、计算边界和测试覆盖。

## 模块总览

| 文档 | 路由前缀 | 说明 |
| --- | --- | --- |
| `system.md` | `/api/system` | 健康检查 |
| `dashboard.md` | `/api/pvms/dashboard` | 首页地图、KPI、告警和 VPP 摘要 |
| `device-alarm.md` | `/api/pvms/devices/monitor`、`/api/pvms/alarms/center` | 设备监控与告警中心 |
| `adjustable-capacity.md` | `/api/pvms/adjustable-capacity` | 可调容量 |
| `stations.md` | `/api/pvms/resource-units` | 旧资源单元链路 |
| `station-archive.md` | `/api/pvms/station-tree`、`/api/pvms/station-archive/*` | 电站监控链路 |
| `production-monitor.md` | `/api/pvms/production-monitor` | 生产监控链路 |
| `forecast.md` | `/api/pvms/forecast` | 预测分析 |
| `strategy.md` | `/api/pvms/strategy` | 策略管理 |

## 当前优先阅读

按当前维护价值，建议先看：

1. `production-monitor.md`
2. `station-archive.md`
3. `forecast.md`
4. `dashboard.md`
5. `strategy.md`

## 当前状态提示

截至 `2026-03-30`：

- `dashboard` 地图动态点位链路已经切到 H2
- `production-monitor` 已切到 H2
- `station-archive` 已切到 H2
- `forecast` 已切到 “系统内事实数据 + H2 预测元数据 + 后端计算”
- `strategy` 仍然是后续契约收敛重点

维护时不要再默认认为：

- `station-archive` 还是旧 mock
- `production-monitor` 只有 overview 链路在后端
- `forecast` 还只是前端演示数据
