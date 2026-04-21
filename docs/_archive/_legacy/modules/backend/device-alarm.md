# M06 设备与告警模块

## 功能

设备监控列表、告警中心工单看板、告警统计与筛选。

## 路由

### `GET /api/pvms/devices/monitor`
- 功能: 设备监控列表 (含状态统计、分组、保养提示)
- 参数: `keyword?`, `status?`
- 返回: `{ summary: { total, normal, warning, fault, offline }, groups: [...], maintenanceTips: [...] }`

### `GET /api/pvms/alarms/center`
- 功能: 告警中心 (工单看板+统计+列表)
- 参数: `level?` (critical/major/minor/hint), `status?` (pending/processing/resolved/closed), `keyword?`
- 返回: `{ processBoard: { pending, processing, resolved, closed }, summary: { critical, major, minor, hint }, items: [...] }`

## 数据

- 设备: 5 个电站共 60 台逆变器
- 告警: 10 条预置告警 (2紧急 + 3重要 + 3一般 + 2提示)

## 测试

- 文件: `DeviceAlarmControllerTest.java`
- 覆盖: 设备列表、设备状态过滤、告警中心、告警级别过滤、告警状态过滤
- 测试数: 5 个
