# PVMS 后端 API 测试文档

> 版本: 0.0.1-SNAPSHOT | 端口: 8091 | 基础路径: `/api`
> 所有响应统一格式: `{ "code": 0, "message": "success", "data": {...} }`

---

## 目录

- [M00 系统健康](#m00-系统健康)
- [M01 综合监控层 (Dashboard)](#m01-综合监控层)
- [M02 生产监控层 (Production Monitor)](#m02-生产监控层)
- [M02-A 资源单元 (Resource Units)](#m02-a-资源单元)
- [M03 台账管理 (Station Archive)](#m03-台账管理)
- [M04 策略管理 (Strategy)](#m04-策略管理)
- [M05 预测分析 (Forecast)](#m05-预测分析)
- [M06 设备与告警 (Device & Alarm)](#m06-设备与告警)
- [M07 可调容量 (Adjustable Capacity)](#m07-可调容量)

---

## M00 系统健康

### `GET /api/system/health`

| 项目 | 说明 |
|------|------|
| 功能 | 服务健康检查 |
| 参数 | 无 |
| 测试类 | `SystemHealthControllerTest` |

**响应示例:**
```json
{
  "code": 0, "message": "success",
  "data": {
    "service": "pvms-backend",
    "status": "UP",
    "version": "0.0.1-SNAPSHOT"
  }
}
```

---

## M01 综合监控层

### `GET /api/pvms/dashboard/stations-geo`

| 项目 | 说明 |
|------|------|
| 功能 | GIS 电站分布 + 状态统计 |
| 参数 | `status?` (normal/warning/fault/maintenance/offline), `region?`, `capacityRange?` (lt3000/3000to5000/gte5000) |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "filters": { "statusOptions": [], "regionOptions": [], "capacityOptions": [] },
    "summary": [{ "key": "normal", "label": "正常", "color": "#67C23A", "count": 2 }],
    "stations": [{ "id": "PV-001", "name": "...", "longitude": 113.9, "latitude": 22.5, "status": "normal", "capacityKwp": 2500, "realtimePowerKw": 1823 }]
  }
}
```

### `GET /api/pvms/dashboard/kpi-summary`

| 项目 | 说明 |
|------|------|
| 功能 | KPI 看板数据 (8项指标) |
| 参数 | `stationId?` |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "updatedAt": "2026-03-22 14:35",
    "focusLabel": "全系统",
    "items": [{ "key": "capacity", "title": "总装机容量", "value": 23.9, "unit": "MWp", "icon": "el-icon-office-building", "accent": "teal" }]
  }
}
```

### `GET /api/pvms/dashboard/power-curve`

| 项目 | 说明 |
|------|------|
| 功能 | 功率曲线 (96个15分钟点位) |
| 参数 | `stationId?`, `date?` (YYYY-MM-DD) |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "PV-001", "stationName": "...", "currentDate": "2026-03-22",
    "deviationRate": 4.6, "peakPowerKw": 2318,
    "actual": [{ "time": "2026-03-22 00:00:00", "value": 0 }],
    "plan": [], "forecast": [], "irradiance": []
  }
}
```

### `GET /api/pvms/dashboard/station-ranking`

| 项目 | 说明 |
|------|------|
| 功能 | 电站发电排名 |
| 参数 | `metric?` (energy/hours/pr) |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "currentMetric": "energy", "unit": "kWh",
    "metricOptions": [{ "label": "日发电量", "value": "energy" }],
    "rankings": [{ "stationId": "PV-001", "stationName": "...", "value": 8432, "rankChange": 1 }]
  }
}
```

### `GET /api/pvms/dashboard/overview`

| 项目 | 说明 |
|------|------|
| 功能 | 首屏总览 (摘要卡片、趋势、焦点告警、电站行) |
| 参数 | 无 |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "summaryCards": [{ "key": "totalCapacity", "title": "总装机", "value": 23.9, "unit": "MWp" }],
    "trends": { "dates": [], "generation": [], "revenue": [] },
    "focusAlarms": [],
    "stationRows": [{ "id": "PV-001", "name": "...", "status": "normal", "power": 1823 }]
  }
}
```

### `GET /api/pvms/dashboard/vpp-node-status`

| 项目 | 说明 |
|------|------|
| 功能 | VPP 节点聚合状态 |
| 参数 | 无 |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "nodeId": "VPP-NODE-001",
    "totalCapacityMw": 23.9,
    "availableCapacityMw": 18.2,
    "onlineStations": 4,
    "totalStations": 6,
    "adjustableRangeMw": { "min": -5.2, "max": 8.6 },
    "lastHeartbeat": "2026-03-22 14:35:00"
  }
}
```

### `GET /api/pvms/alarms/recent`

| 项目 | 说明 |
|------|------|
| 功能 | 最近告警列表 |
| 参数 | `level?` (critical/major/minor/hint), `stationId?` |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "summary": { "critical": 1, "major": 2, "minor": 1, "hint": 1 },
    "items": [{ "id": "ALM-001", "level": "critical", "deviceName": "逆变器 INV-03", "stationName": "...", "description": "..." }]
  }
}
```

### `GET /api/pvms/weather/current`

| 项目 | 说明 |
|------|------|
| 功能 | 当前天气 + 3日预报 |
| 参数 | `stationId?` |
| 测试类 | `DashboardControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "PV-001", "stationName": "...",
    "current": { "weather": "多云", "temperature": 28, "irradiance": 680, "humidity": 65 },
    "forecast": [{ "date": "03-23", "weather": "多云", "tempHigh": 29, "tempLow": 23 }]
  }
}
```

---

## M02 生产监控层

### `GET /api/pvms/production-monitor/meta`

| 项目 | 说明 |
|------|------|
| 功能 | 资源单元元数据列表 |
| 参数 | 无 |
| 测试类 | `ProductionMonitorControllerTest` |

**响应:**
```json
{
  "data": {
    "defaultResourceUnitId": "RU-001",
    "regionOptions": [{ "label": "全部区域", "value": "" }],
    "resourceUnits": [{ "id": "RU-001", "name": "...", "region": "华南", "stationCount": 3, "capacityMwp": 14.1 }]
  }
}
```

### `GET /api/pvms/production-monitor/overview`

| 项目 | 说明 |
|------|------|
| 功能 | 资源单元总览 (KPI+成员+天气+告警) |
| 参数 | `resourceUnitId` |
| 测试类 | `ProductionMonitorControllerTest` |

### `GET /api/pvms/production-monitor/output`

| 项目 | 说明 |
|------|------|
| 功能 | 出力分析 (功率曲线+天气趋势+贡献排名) |
| 参数 | `resourceUnitId`, `granularity?` (15m/30m/60m) |
| 测试类 | `ProductionMonitorControllerTest` |

### `GET /api/pvms/production-monitor/dispatch`

| 项目 | 说明 |
|------|------|
| 功能 | 调度执行 (指令记录+趋势+风险) |
| 参数 | `resourceUnitId` |
| 测试类 | `ProductionMonitorControllerTest` |

### `GET /api/pvms/production-monitor/weather`

| 项目 | 说明 |
|------|------|
| 功能 | 气象研判 (摘要+趋势+影响) |
| 参数 | `resourceUnitId` |
| 测试类 | `ProductionMonitorControllerTest` |

---

## M02-A 资源单元

### `GET /api/pvms/resource-units/list`

| 项目 | 说明 |
|------|------|
| 功能 | 资源单元分页列表 |
| 参数 | `keyword?`, `status?`, `region?`, `capacityRange?`, `page` (default 1), `pageSize` (default 6) |
| 测试类 | `StationControllerTest` |

### `GET /api/pvms/resource-units/{resourceUnitId}/overview`

| 项目 | 说明 |
|------|------|
| 功能 | 资源单元详情总览 |
| 参数 | `resourceUnitId` (路径参数) |
| 测试类 | `StationControllerTest` |

### `GET /api/pvms/resource-units/{resourceUnitId}/power-curve`

| 项目 | 说明 |
|------|------|
| 功能 | 资源单元出力曲线 |
| 参数 | `resourceUnitId` (路径参数), `date?` |
| 测试类 | `StationControllerTest` |

---

## M03 台账管理

### `GET /api/pvms/stations/archive`

| 项目 | 说明 |
|------|------|
| 功能 | 电站台账列表 (含网架状态过滤) |
| 参数 | `keyword?`, `gridStatus?` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "ST-001", "name": "深圳港科园区光伏电站",
      "companyName": "深圳科技有限公司", "capacityKwp": 2500,
      "gridStatus": "grid-connected", "gridStatusLabel": "并网",
      "commissionDate": "2024-06-15", "address": "..."
    }]
  }
}
```

### `GET /api/pvms/station-tree`

| 项目 | 说明 |
|------|------|
| 功能 | 公司→电站树形结构 |
| 参数 | 无 |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "COM-SZ", "name": "深圳科技有限公司", "type": "company",
      "children": [{ "id": "ST-001", "name": "港科园区光伏电站", "type": "station", "capacityKwp": 2500 }]
    }]
  }
}
```

### `GET /api/pvms/station-archive/company-overview`

| 项目 | 说明 |
|------|------|
| 功能 | 公司级总览 (装机/发电/收益汇总) |
| 参数 | `companyId` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "companyId": "COM-SZ", "companyName": "...",
    "totalCapacityKwp": 14100, "stationCount": 6,
    "todayEnergyKwh": 32850, "monthEnergyKwh": 856200,
    "todayRevenueCny": 23400, "monthRevenueCny": 612300,
    "co2ReductionTon": 428.5, "avgAvailability": 96.2,
    "stations": [{ "id": "ST-001", "name": "...", "capacityKwp": 2500, "todayEnergyKwh": 8432 }]
  }
}
```

### `GET /api/pvms/station-archive/resource-overview`

| 项目 | 说明 |
|------|------|
| 功能 | 资源节点总览 (单站详情) |
| 参数 | `stationId` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "ST-001", "stationName": "...",
    "companyName": "...", "capacityKwp": 2500,
    "inverterCount": 8, "status": "normal",
    "kpi": { "todayEnergy": 8432, "monthEnergy": 215600, "todayRevenue": 6012 },
    "inverters": [{ "id": "INV-001", "name": "1#逆变器", "ratedPowerKw": 315, "status": "normal" }]
  }
}
```

### `GET /api/pvms/station-archive/station-realtime`

| 项目 | 说明 |
|------|------|
| 功能 | 电站实时数据 (功率+辐照+温度) |
| 参数 | `stationId` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "ST-001",
    "realtimePowerKw": 1823, "irradiance": 680,
    "panelTemperature": 42.5, "ambientTemperature": 28,
    "powerCurve": [{ "time": "00:00", "actual": 0, "forecast": 0 }],
    "inverterStatus": [{ "id": "INV-001", "name": "1#逆变器", "powerKw": 228, "status": "normal" }]
  }
}
```

### `GET /api/pvms/station-archive/station-adjustable`

| 项目 | 说明 |
|------|------|
| 功能 | 电站可调容量 |
| 参数 | `stationId` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "ST-001",
    "currentPowerKw": 1823, "maxAdjustableKw": 2200, "minAdjustableKw": 500,
    "adjustableRangeKw": 1700, "utilizationRate": 72.9,
    "periods": [{ "start": "08:00", "end": "11:00", "type": "peak", "adjustableKw": 1200 }]
  }
}
```

### `GET /api/pvms/station-archive/station-strategy`

| 项目 | 说明 |
|------|------|
| 功能 | 电站当前生效策略 |
| 参数 | `stationId` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "ST-001",
    "activeStrategies": [{
      "id": "STR-001", "name": "午间削峰策略",
      "type": "peak-shaving", "typeLabel": "削峰填谷",
      "status": "executing", "timePeriod": "11:00-14:00",
      "targetPowerKw": 1800, "estimatedRevenue": 2450
    }]
  }
}
```

### `GET /api/pvms/station-archive/inverter-realtime`

| 项目 | 说明 |
|------|------|
| 功能 | 逆变器实时监控 |
| 参数 | `stationId`, `inverterId?` |
| 测试类 | `StationArchiveControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "INV-001", "name": "1#逆变器",
      "ratedPowerKw": 315, "currentPowerKw": 228,
      "todayEnergyKwh": 1054, "efficiency": 97.2,
      "dcVoltage": 620, "dcCurrent": 12.5,
      "acVoltage": 380, "acCurrent": 18.2,
      "temperature": 45.3, "status": "normal",
      "strings": [{ "id": "STR-1", "voltage": 42.5, "current": 10.2 }]
    }]
  }
}
```

---

## M04 策略管理

### `GET /api/pvms/strategy/meta`

| 项目 | 说明 |
|------|------|
| 功能 | 策略模块元数据 (类型/状态/电站/公司/电价时段) |
| 参数 | 无 |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "types": [{ "label": "需求响应", "value": "demand-response" }],
    "statuses": [{ "label": "草稿", "value": "draft" }],
    "stations": [{ "id": "ST-001", "name": "..." }],
    "companies": [{ "id": "COM-SZ", "name": "..." }],
    "pricePeriods": [{ "start": "00:00", "end": "06:00", "type": "valley", "price": 0.25 }]
  }
}
```

### `GET /api/pvms/strategy/tree`

| 项目 | 说明 |
|------|------|
| 功能 | 策略树 (公司→电站, 含容量和负荷基础) |
| 参数 | 无 |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "COM-SZ", "name": "深圳科技有限公司", "type": "company",
      "children": [{
        "id": "ST-001", "name": "港科园区光伏电站",
        "type": "station", "capacityKwp": 2500, "loadBaseKw": 800
      }]
    }]
  }
}
```

### `GET /api/pvms/strategy/kpi`

| 项目 | 说明 |
|------|------|
| 功能 | 策略 KPI (活跃数/今日收益/成功率/待处理) |
| 参数 | 无 |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "activeCount": 10,
    "todayRevenue": 34520,
    "successRate": 92.5,
    "pendingCount": 5
  }
}
```

### `GET /api/pvms/strategy/list`

| 项目 | 说明 |
|------|------|
| 功能 | 策略列表 (含筛选) |
| 参数 | `status?`, `type?`, `stationId?` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "STR-001", "name": "午间削峰策略A",
      "stationId": "ST-001", "stationName": "...",
      "type": "peak-shaving", "typeLabel": "削峰填谷",
      "status": "executing", "statusLabel": "执行中",
      "executeDate": "2026-03-22",
      "timePeriod": "11:00-14:00",
      "targetPowerKw": 1800,
      "estimatedRevenue": 2450
    }]
  }
}
```

### `GET /api/pvms/strategy/detail`

| 项目 | 说明 |
|------|------|
| 功能 | 策略详情 (含分时电价) |
| 参数 | `id` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "id": "STR-001", "name": "...",
    "stationId": "ST-001", "stationName": "...",
    "type": "peak-shaving", "typeLabel": "削峰填谷",
    "status": "executing", "statusLabel": "执行中",
    "executeDate": "2026-03-22",
    "periods": [{ "start": "11:00", "end": "14:00" }],
    "targetPowerKw": 1800,
    "powerUpperLimitKw": 2200, "powerLowerLimitKw": 1400,
    "estimatedRevenue": 2450, "actualRevenue": 2380,
    "pricePeriods": [{ "period": "11:00-12:00", "typeLabel": "峰", "price": 1.11, "action": "削峰" }],
    "remark": "", "version": 1
  }
}
```

### `GET /api/pvms/strategy/electricity-price`

| 项目 | 说明 |
|------|------|
| 功能 | 分时电价表 (9个时段) |
| 参数 | `stationId?`, `date?` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "periods": [{
      "start": "00:00", "end": "06:00",
      "type": "valley", "typeLabel": "谷",
      "price": 0.25, "action": "填谷"
    }]
  }
}
```

### `POST /api/pvms/strategy/create`

| 项目 | 说明 |
|------|------|
| 功能 | 创建单个策略 |
| 请求体 | `{ name, stationId, type, executeDate, periods, targetPowerKw, powerUpperLimitKw, powerLowerLimitKw, remark? }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "id": "STR-NEW-001" } }
```

### `POST /api/pvms/strategy/batch-create`

| 项目 | 说明 |
|------|------|
| 功能 | 批量创建策略 |
| 请求体 | `{ items: [{ stationId, name, type, executeDate, periods, targetPowerKw, ... }] }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "ids": ["STR-NEW-001", "STR-NEW-002"] } }
```

### `POST /api/pvms/strategy/submit`

| 项目 | 说明 |
|------|------|
| 功能 | 提交策略 (draft→pending) |
| 参数 | `id` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "success": true } }
```

### `POST /api/pvms/strategy/terminate`

| 项目 | 说明 |
|------|------|
| 功能 | 终止策略 (→terminated) |
| 参数 | `id` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "success": true } }
```

### `POST /api/pvms/strategy/batch-submit`

| 项目 | 说明 |
|------|------|
| 功能 | 批量提交策略 |
| 请求体 | `{ ids: ["STR-001", "STR-002"] }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "success": true, "count": 2 } }
```

### `POST /api/pvms/strategy/batch-delete`

| 项目 | 说明 |
|------|------|
| 功能 | 批量删除草稿策略 |
| 请求体 | `{ ids: ["STR-001", "STR-002"] }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{ "data": { "success": true, "count": 2 } }
```

### `POST /api/pvms/strategy/simulate`

| 项目 | 说明 |
|------|------|
| 功能 | 单策略收益模拟 |
| 请求体 | `{ stationId, type, executeDate, periods, targetPowerKw }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "estimatedRevenue": 2450,
    "confidence": 0.85,
    "revenueRange": { "min": 2100, "max": 2800 },
    "breakdown": [{ "period": "11:00-12:00", "revenue": 820, "priceType": "peak" }]
  }
}
```

### `POST /api/pvms/strategy/batch-simulate`

| 项目 | 说明 |
|------|------|
| 功能 | 批量策略收益模拟 |
| 请求体 | `{ items: [{ stationId, type, executeDate, periods, targetPowerKw }] }` |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "totalEstimatedRevenue": 12500,
    "items": [{ "stationId": "ST-001", "stationName": "...", "estimatedRevenue": 2450 }]
  }
}
```

### `GET /api/pvms/strategy/revenue/summary`

| 项目 | 说明 |
|------|------|
| 功能 | 收益总览 (KPI+趋势+类型分布) |
| 参数 | 无 |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "kpi": { "monthlyTotal": 156800, "dailyAvg": 5226, "successRate": 92.5, "monthOverMonth": 12.3 },
    "trend": {
      "dates": ["2026-03-01", "2026-03-02"],
      "dailyRevenue": [4200, 5100],
      "cumulativeRevenue": [4200, 9300]
    },
    "typeBreakdown": [{ "type": "需求响应", "revenue": 68500 }]
  }
}
```

### `GET /api/pvms/strategy/revenue/detail`

| 项目 | 说明 |
|------|------|
| 功能 | 策略收益明细表 |
| 参数 | 无 |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "STR-001", "name": "...", "stationName": "...",
      "typeLabel": "削峰填谷", "timePeriod": "11:00-14:00",
      "estimatedRevenue": 2450, "actualRevenue": 2380,
      "deviationRate": -2.9
    }]
  }
}
```

### `GET /api/pvms/strategy/compare`

| 项目 | 说明 |
|------|------|
| 功能 | 策略对比 (2-3个) |
| 参数 | `ids` (逗号分隔) |
| 测试类 | `StrategyControllerTest` |

**响应:**
```json
{
  "data": {
    "items": [{
      "id": "STR-001", "name": "...", "stationName": "...",
      "typeLabel": "削峰填谷", "targetPowerKw": 1800,
      "estimatedRevenue": 2450, "actualRevenue": 2380,
      "deviationRate": -2.9
    }]
  }
}
```

---

## M05 预测分析

### `GET /api/pvms/forecast/meta`

| 项目 | 说明 |
|------|------|
| 功能 | 预测模块元数据 |
| 参数 | 无 |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "stations": [{ "id": "ST-001", "name": "..." }],
    "models": [{ "id": "lstm", "name": "LSTM 神经网络" }],
    "granularityOptions": [{ "label": "15分钟", "value": "15m" }]
  }
}
```

### `GET /api/pvms/forecast/overview`

| 项目 | 说明 |
|------|------|
| 功能 | 预测总览 (KPI+精度趋势) |
| 参数 | `stationId?` |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "kpi": { "todayForecastKwh": 45200, "accuracy": 96.2, "rmse": 128, "mae": 86 },
    "accuracyTrend": { "dates": [], "values": [] },
    "forecastVsActual": { "timeAxis": [], "forecast": [], "actual": [] }
  }
}
```

### `GET /api/pvms/forecast/adjustable`

| 项目 | 说明 |
|------|------|
| 功能 | 可调容量预测 |
| 参数 | `stationId?` |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "summary": { "maxAdjustableKw": 8600, "avgAdjustableKw": 5200 },
    "timeline": [{ "time": "00:00", "forecastPowerKw": 0, "loadKw": 200, "adjustableKw": 0 }]
  }
}
```

### `GET /api/pvms/forecast/accuracy`

| 项目 | 说明 |
|------|------|
| 功能 | 预测精度分析 |
| 参数 | `stationId?`, `dateRange?` |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "overall": { "accuracy": 96.2, "rmse": 128, "mae": 86, "mape": 3.8 },
    "byStation": [{ "stationId": "ST-001", "stationName": "...", "accuracy": 97.1 }],
    "byHour": [{ "hour": 6, "accuracy": 88.5 }]
  }
}
```

### `GET /api/pvms/forecast/comparison`

| 项目 | 说明 |
|------|------|
| 功能 | 预测 vs 实际对比 |
| 参数 | `stationId?`, `date?` |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "stationId": "ST-001", "date": "2026-03-22",
    "timeAxis": ["00:00", "00:15"],
    "forecast": [0, 0],
    "actual": [0, 0],
    "deviation": [0, 0]
  }
}
```

### `GET /api/pvms/forecast/deviation-heatmap`

| 项目 | 说明 |
|------|------|
| 功能 | 偏差热力图 (按站+时段) |
| 参数 | `dateRange?` |
| 测试类 | `ForecastControllerTest` |

**响应:**
```json
{
  "data": {
    "stations": ["深圳港科", "松山湖"],
    "hours": [0, 1, 2, 3],
    "matrix": [[1.2, 2.3, 3.1, 2.8], [1.5, 2.1, 2.9, 3.2]]
  }
}
```

---

## M06 设备与告警

### `GET /api/pvms/devices/monitor`

| 项目 | 说明 |
|------|------|
| 功能 | 设备监控列表 (含状态统计+分组) |
| 参数 | `keyword?`, `status?` |
| 测试类 | `DeviceAlarmControllerTest` |

**响应:**
```json
{
  "data": {
    "summary": { "total": 48, "normal": 40, "warning": 5, "fault": 2, "offline": 1 },
    "groups": [{ "stationName": "...", "devices": [{ "id": "INV-001", "name": "1#逆变器", "type": "inverter", "status": "normal", "powerKw": 228 }] }],
    "maintenanceTips": [{ "deviceId": "INV-003", "message": "距下次保养还剩 12 天" }]
  }
}
```

### `GET /api/pvms/alarms/center`

| 项目 | 说明 |
|------|------|
| 功能 | 告警中心 (工单看板+统计+列表) |
| 参数 | `level?`, `status?`, `keyword?` |
| 测试类 | `DeviceAlarmControllerTest` |

**响应:**
```json
{
  "data": {
    "processBoard": { "pending": 3, "processing": 2, "resolved": 15, "closed": 8 },
    "summary": { "critical": 1, "major": 2, "minor": 3, "hint": 2 },
    "items": [{
      "id": "ALM-001", "level": "critical", "levelLabel": "紧急",
      "deviceName": "逆变器 INV-03", "stationName": "...",
      "description": "...", "status": "pending", "createdAt": "2026-03-22 14:32"
    }]
  }
}
```

---

## M07 可调容量

### `GET /api/pvms/adjustable-capacity/realtime`

| 项目 | 说明 |
|------|------|
| 功能 | 实时可调容量汇总 (全局+分站+时序) |
| 参数 | 无 |
| 测试类 | `AdjustableCapacityControllerTest` |

**响应:**
```json
{
  "data": {
    "summary": {
      "totalCapacityKw": 23900, "currentPowerKw": 12267,
      "maxAdjustableUpKw": 8600, "maxAdjustableDownKw": 5200,
      "deferrableLoadKw": 1200, "utilizationRate": 51.3
    },
    "stations": [{
      "id": "ST-001", "name": "...", "capacityKw": 2500,
      "currentPowerKw": 1823, "adjustableUpKw": 600, "adjustableDownKw": 1300
    }],
    "timeline": [{
      "time": "00:00", "totalPowerKw": 0,
      "adjustableUpKw": 0, "adjustableDownKw": 0
    }]
  }
}
```

---

## 通用说明

### 响应码

| code | 含义 |
|------|------|
| 0 | 成功 |
| 400 | 参数校验失败 |
| 404 | 资源未找到 |
| 500 | 服务器内部错误 |

### 测试约定

- 测试框架: JUnit 5 + MockMvc + Spring Boot Test
- 所有测试类使用 `@SpringBootTest` + `@AutoConfigureMockMvc`
- 断言格式: `jsonPath("$.code").value(0)` + 业务字段校验
- 测试文件路径: `src/test/java/cn/techstar/pvms/backend/module/{module}/controller/{Module}ControllerTest.java`
