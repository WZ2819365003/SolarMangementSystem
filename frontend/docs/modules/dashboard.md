# Dashboard Module

## Route

- Path: `/dashboard/overview`
- Purpose: 光伏管理系统首页驾驶舱，整合 GIS 电站态势、经营 KPI、功率曲线、发电排名、实时告警和天气信息

## Page Composition

- Top row:
  - `GIS 电站总览`
  - `核心 KPI 看板`
  - `天气信息`
- `GIS 电站总览` 内部采用两层结构：
  - 地图主视区
  - 底部运行洞察区
- 运行洞察区由 3 张轻量卡片组成：
  - `重点关注电站`
    - 默认展示 3 条
    - 超出部分通过轻量轮播分页查看
    - 每条包含状态、功率摘要和 3 个轻量 badge
  - `调度摘要`
    - 包含一条值班摘要条
    - 4 个指标 tile 均带进度条
    - 底部补 2 条动作建议
  - `区域态势`
- 洞察区不新增独立接口，前端通过 `stations-geo + alarms/recent` 本地聚合

## Params

- Route params: none
- Query params:
  - `stationId?`: 可选，电站 ID，用于聚焦 KPI、曲线、天气和告警
  - `focus?`: 可选，跳转自其他页面时用于高亮默认电站

## Data Contract

### `GET /pvms/dashboard/stations-geo`

- Purpose: 获取地图电站点位、状态汇总和筛选项
- Query:
  - `status?`: `normal | warning | fault | maintenance | offline`
  - `region?`: 区域名称
  - `capacityRange?`: `lt3000 | 3000to5000 | gte5000`
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "filters": {
      "statusOptions": [{ "label": "全部状态", "value": "" }],
      "regionOptions": [{ "label": "全部区域", "value": "" }],
      "capacityOptions": [{ "label": "全部容量", "value": "" }]
    },
    "summary": [
      { "key": "normal", "label": "正常", "color": "#67C23A", "count": 2 }
    ],
    "stations": [
      {
        "id": "PV-001",
        "name": "深圳港科园区光伏电站",
        "region": "华南园区",
        "longitude": 113.9489,
        "latitude": 22.5408,
        "status": "normal",
        "statusLabel": "正常",
        "statusColor": "#67C23A",
        "capacityKwp": 2500,
        "realtimePowerKw": 1823,
        "todayEnergyKwh": 8432,
        "todayRevenueCny": 3372.8,
        "healthGrade": "A",
        "availability": 99.2,
        "address": "深圳港科生态园 8 栋屋面"
      }
    ]
  }
}
```

### `GET /pvms/dashboard/kpi-summary`

- Purpose: 获取首页 KPI 看板
- Query:
  - `stationId?`: 可选，电站维度
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "updatedAt": "2026-03-22 14:35",
    "focusLabel": "全系统",
    "items": [
      {
        "key": "capacity",
        "title": "总装机容量",
        "value": 23.9,
        "unit": "MWp",
        "icon": "el-icon-office-building",
        "accent": "teal",
        "helper": "园区集中式 + 分布式汇总",
        "changeRate": null,
        "changeLabel": "稳定"
      }
    ]
  }
}
```

### `GET /pvms/dashboard/power-curve`

- Purpose: 获取计划/实际/预测功率曲线
- Query:
  - `date?`: 业务日期，默认 `2026-03-22`
  - `stationId?`: 可选，电站维度
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "stationId": "PV-001",
    "stationName": "深圳港科园区光伏电站",
    "currentDate": "2026-03-22",
    "dateTabs": [
      { "label": "今日", "value": "2026-03-22" },
      { "label": "昨日", "value": "2026-03-21" }
    ],
    "deviationRate": 4.6,
    "peakPowerKw": 2318,
    "actual": [{ "time": "2026-03-22 00:00:00", "value": 0 }],
    "plan": [{ "time": "2026-03-22 00:00:00", "value": 0 }],
    "forecast": [{ "time": "2026-03-22 00:00:00", "value": 0 }],
    "irradiance": [{ "time": "2026-03-22 00:00:00", "value": 0 }]
  }
}
```

### `GET /pvms/dashboard/station-ranking`

- Purpose: 获取发电排名
- Query:
  - `metric?`: `energy | hours | pr`
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "currentMetric": "energy",
    "metricOptions": [
      { "label": "日发电量", "value": "energy" }
    ],
    "unit": "kWh",
    "rankings": [
      {
        "stationId": "PV-001",
        "stationName": "深圳港科园区光伏电站",
        "value": 8432,
        "rankChange": 1
      }
    ]
  }
}
```

### `GET /pvms/alarms/recent`

- Purpose: 获取首页滚动告警
- Query:
  - `level?`: `critical | major | minor | hint`
  - `stationId?`: 可选，电站维度
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summary": {
      "critical": 1,
      "major": 2,
      "minor": 1,
      "hint": 1
    },
    "items": [
      {
        "id": "ALM-20260322-001",
        "time": "14:32",
        "level": "critical",
        "levelLabel": "紧急",
        "deviceName": "逆变器 INV-03",
        "stationId": "PV-003",
        "stationName": "武汉物流基地光伏电站",
        "alarmType": "dc-over-voltage",
        "description": "直流侧过压告警",
        "status": "未处理",
        "owner": "华中运维一组",
        "suggestion": "15 分钟内派单复核"
      }
    ]
  }
}
```

## Interaction Notes

- 地图筛选条件变更后，会同步重载：
  - `stations-geo`
  - `kpi-summary`
  - `power-curve`
  - `weather/current`
  - `alarms/recent`
- 地图点位或洞察区“重点关注电站”点击后，会联动刷新：
  - KPI 看板
  - 功率曲线
  - 天气信息
  - 实时告警
- 用户主动选站后，地图会从全景态切到该电站的道路级局部视图，并放大周边道路与园区环境
- 聚焦态会显示 `返回全景` 入口，点击后回到当前筛选条件下的总览视图
- 地图筛选变化时，会自动退出聚焦态，重新进入总览视图
- “重点关注电站”在可视区域只保留 3 条，超出项通过自动轮播和底部分页点切换

### `GET /pvms/weather/current`

- Purpose: 获取首页天气卡片
- Query:
  - `stationId?`: 可选，电站维度
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "stationId": "PV-001",
    "stationName": "深圳港科园区光伏电站",
    "current": {
      "weather": "多云",
      "temperature": 28,
      "irradiance": 680,
      "humidity": 65,
      "windSpeed": 3.2,
      "windDirection": "东南风"
    },
    "forecast": [
      {
        "date": "03-23",
        "weather": "多云",
        "tempHigh": 29,
        "tempLow": 23,
        "irradianceEstimate": 620
      }
    ]
  }
}
```
