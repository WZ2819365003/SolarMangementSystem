# Device Alarm Backend Module

## Scope

This module covers the two remaining operational pages:

- device monitor
- alarm center

It does not introduce another isolated mock dataset. Instead, it reads from the existing H2 station-archive facts and builds the frontend view model in the backend service layer.

## Endpoints

### `GET /api/pvms/devices/monitor`

Purpose:

- return device summary cards
- return device group ratios
- return maintenance suggestions
- return the device status table used by the frontend page

Query params:

- `keyword?`
- `status?`

Current accepted status vocabulary matches the frontend page:

- `在线`
- `离线`
- `告警`

Response shape:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [],
    "deviceGroups": [],
    "maintenanceTips": [],
    "rows": []
  }
}
```

### `GET /api/pvms/alarms/center`

Purpose:

- return alarm summary cards
- return alarm process board suggestions
- return the alarm table used by the frontend page

Query params:

- `keyword?`
- `level?`
- `status?`

Current accepted level vocabulary matches the frontend page:

- `严重`
- `重要`
- `一般`
- `提示`

Current accepted status vocabulary matches the frontend page:

- `未处理`
- `处理中`
- `待确认`
- `已关闭`

Response shape:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [],
    "processBoard": [],
    "rows": [],
    "total": 0
  }
}
```

## Data Sources

This module is built on the existing H2 tables below:

- `sa_station`
- `sa_inverter`
- `sa_inverter_alarm`
- `sa_station_curve_15m`
- `sa_station_strategy`

That means device and alarm data is no longer maintained as a separate frontend-only mock island.

## Backend Calculations

### Device monitor

The backend computes:

- online/offline/alarm counts
- device group ratios
- maintenance suggestions
- display-friendly `loadRate`
- display-friendly `temperature`
- synthetic but deterministic `lastReportAt`

Important detail:

- `loadRate` is derived from the current station PV output, inverter count, inverter rated power, and inverter status factor
- `temperature` is adjusted from the inverter module temperature plus a small status-based offset
- `lastReportAt` is derived from a fixed development-time reference clock to keep the page stable and deterministic

### Alarm center

The backend computes:

- summary cards
- close-loop rate
- process board text
- frontend-facing Chinese level/status values
- alarm category labels
- alarm owner labels

Important detail:

- raw H2 alarm facts do not use the same vocabulary as the page
- the service layer normalizes raw facts into the labels expected by the current frontend contract

## Main Files

- `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/controller/DeviceAlarmController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/repository/DeviceAlarmRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/service/DeviceAlarmDataService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/service/DeviceAlarmSupport.java`

## Extension Path

When this module is later connected to real external systems:

- replace repository reads first
- keep the controller contract stable
- keep the service-layer calculations stable unless the business definition changes

In other words:

- external system integration belongs in the repository/data-source layer
- KPI and display aggregation logic should remain in the backend service layer

## Test Coverage

Primary test:

- `backend/src/test/java/cn/techstar/pvms/backend/module/devicealarm/controller/DeviceAlarmControllerTest.java`

Covered behaviors:

- device monitor contract
- device status filtering
- alarm center contract
- alarm level/status filtering
