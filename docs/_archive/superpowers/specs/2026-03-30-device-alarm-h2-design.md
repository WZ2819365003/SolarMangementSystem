# Device Alarm H2 Design

**Date:** 2026-03-30  
**Scope:** `devices` + `alarms` modules  
**Branch:** `codex/device-alarm-h2`

## Goal

Finish the remaining two user-facing modules by replacing the frontend mock dependency with a Spring Boot + H2 backend chain that is consistent with the existing `dashboard`, `production-monitor`, `station-archive`, `forecast`, and `strategy` migrations.

## Recommendation

Use the existing station-archive H2 facts as the base instead of creating another isolated mock silo.

- Reuse `sa_station` and `sa_inverter` as the device inventory and realtime snapshot source.
- Reuse `sa_inverter_alarm` as the alarm fact table.
- Compute device summaries, device group ratios, maintenance tips, alarm summary cards, and alarm process board in the backend service layer.
- Keep the current frontend page layout and route structure.

## Rejected Alternatives

### Option A: Port the old mock service directly

Fastest, but it keeps device/alarm data disconnected from the rest of the system and repeats the same contract drift problem already fixed in other modules.

### Option B: Add a completely new `da_*` device/alarm schema

This is clean on paper, but duplicates data that already exists in `sa_inverter` and `sa_inverter_alarm`. It would increase maintenance cost for little product value.

## Data Model

The primary data source is the existing H2 schema:

- `sa_station`
- `sa_inverter`
- `sa_inverter_alarm`
- `sa_station_strategy`

If the current facts are not enough for a frontend field, add only small supplemental columns or derived mappings instead of introducing a parallel data model.

## Backend Design

Create a dedicated backend module:

- `module/devicealarm/controller/DeviceAlarmController.java`
- `module/devicealarm/repository/*`
- `module/devicealarm/service/*`

Endpoints:

- `GET /api/pvms/devices/monitor`
- `GET /api/pvms/alarms/center`

Response contracts must match the existing frontend pages:

- devices: `summaryCards`, `deviceGroups`, `maintenanceTips`, `rows`
- alarms: `summaryCards`, `processBoard`, `rows`, `total`

The backend must accept the current frontend query vocabulary, including Chinese status and level labels where the page already uses them.

## Frontend Design

Keep the current pages:

- `frontend/src/modules/devices/pages/DeviceMonitorPage.vue`
- `frontend/src/modules/alarms/pages/AlarmCenterPage.vue`

Only make minimal changes if the current pages assume old mock field names or need small filter normalization updates.

## Calculations

### Device monitor

- Total/online/offline/warning counts are computed from filtered device rows.
- Device group ratios are computed by device type.
- Maintenance tips are computed from device status, temperature, load rate, and recent alarm state.
- Device rows expose presentation-friendly fields such as `loadRate`, `temperature`, and `lastReportAt`.

### Alarm center

- Summary cards are computed from current-day alarm facts and close-loop rate.
- Process board text is derived from the current pending/processing concentration and alarm category mix.
- Alarm rows are filterable by keyword, level, and status.

## Documentation

Update:

- `docs/modules/backend/device-alarm.md`
- `docs/modules/frontend/devices.md`
- `docs/modules/frontend/alarms.md`
- `docs/handover/README.md`
- `docs/handover/05_后端维护手册.md`
- `docs/handover/08_测试_排障_交接清单.md`

Add:

- `docs/handover/14_M05_M06_设备监测与告警中心_H2数据与计算说明.md`

## Verification

- Backend controller tests for both endpoints
- `mvn test`
- frontend build
- live requests through backend direct port and frontend proxy
