# Frontend 模块文档索引

这组文档记录前端模块的页面职责、核心文件、接口依赖和维护边界。

## 建议阅读顺序

1. `infrastructure.md`
2. `dashboard.md`
3. `production-monitor.md`
4. `station-monitoring.md`
5. `stations.md`
6. `forecast.md`
7. `strategy.md`
8. `devices.md`
9. `alarms.md`

## 模块说明

| 文档 | 模块 | 说明 |
| --- | --- | --- |
| `infrastructure.md` | 基础设施 | 宿主桥接、UI 依赖、地图依赖 |
| `dashboard.md` | 综合监控中心 | 首页 |
| `production-monitor.md` | M02 页面壳 | 生产监控 + 电站监控双模式 |
| `station-monitoring.md` | M02 生产监控 | M02 模式边界和数据来源 |
| `stations.md` | 电站监控组件 | 树、面板、策略、逆变器详情 |
| `forecast.md` | 预测分析 | M03 |
| `strategy.md` | 策略管理 | M04 |
| `devices.md` | 设备监控 | 设备状态和分布 |
| `alarms.md` | 告警中心 | 告警列表和处理态势 |

## 当前状态提示

截至 `2026-03-30`：

- `production-monitor` 页面已经不只是资源总览
- 电站监控模式和资源总览模式共存在同一个前端壳层
- 电站监控模式已经可以直接联调后端 H2 数据
- `forecast` 页面也已经切到真实后端代理链路

维护时建议把下面四份文档一起看：

1. `production-monitor.md`
2. `station-monitoring.md`
3. `stations.md`
4. `forecast.md`
