# Dashboard 模块

## 功能

综合监控中心首屏接口集合，负责返回 GIS 电站分布、KPI 看板、功率曲线、发电排名、实时告警和天气卡片所需的数据。

## 路由

### `GET /api/pvms/dashboard/stations-geo`

#### 参数

- `status?`: `normal | warning | fault | maintenance | offline`
- `region?`: 区域名称
- `capacityRange?`: `lt3000 | 3000to5000 | gte5000`

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "filters": {
      "statusOptions": [],
      "regionOptions": [],
      "capacityOptions": []
    },
    "summary": [],
    "stations": []
  }
}
```

### `GET /api/pvms/dashboard/kpi-summary`

#### 参数

- `stationId?`: 电站 ID

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "updatedAt": "2026-03-22 14:35",
    "focusLabel": "全系统",
    "items": []
  }
}
```

### `GET /api/pvms/dashboard/power-curve`

#### 参数

- `date?`: 日期，格式 `YYYY-MM-DD`
- `stationId?`: 电站 ID

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "stationId": "PV-001",
    "stationName": "深圳港科园区光伏电站",
    "currentDate": "2026-03-22",
    "dateTabs": [],
    "deviationRate": 4.6,
    "peakPowerKw": 2318,
    "actual": [],
    "plan": [],
    "forecast": [],
    "irradiance": []
  }
}
```

### `GET /api/pvms/dashboard/station-ranking`

#### 参数

- `metric?`: `energy | hours | pr`

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "currentMetric": "energy",
    "metricOptions": [],
    "unit": "kWh",
    "rankings": []
  }
}
```

### `GET /api/pvms/alarms/recent`

#### 参数

- `level?`: `critical | major | minor | hint`
- `stationId?`: 电站 ID

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "critical": 0,
      "major": 0,
      "minor": 0,
      "hint": 0
    },
    "items": []
  }
}
```

### `GET /api/pvms/weather/current`

#### 参数

- `stationId?`: 电站 ID

#### 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "stationId": "PV-001",
    "stationName": "深圳港科园区光伏电站",
    "current": {},
    "forecast": []
  }
}
```
