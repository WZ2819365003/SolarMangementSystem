# Production Monitor H2 Migration Design

## 1. Background

The `production-monitor` module currently has a split state:

- the frontend page already includes the `load` view and calls `/api/pvms/production-monitor/load`
- the backend controller does not implement `/load`, so the development backend returns `404`
- the existing backend `ProductionMonitorMockService` is fully in-memory and does not use a database
- the frontend mock file `frontend/src/shared/mock/station-monitoring.js` already defines a richer `load` payload and several shared business rules

The user requirement for this batch is explicit:

- scope only `production-monitor`
- use embedded H2 in the development environment
- keep the logic chain real even in development
- raw facts such as production, device, forecast, and revenue metadata can be seeded
- summary indicators and derived values should be calculated in the backend service layer
- docs must explain current implementation and future extensibility to real upstream systems

## 2. Goal

Build a complete H2-backed backend data foundation for the `production-monitor` module so that:

- `meta`
- `overview`
- `output`
- `load`
- `dispatch`
- `weather`

all read from one consistent backend fact model instead of independent hardcoded Java lists.

The frontend contract should remain stable wherever practical, and the `load` endpoint should stop returning `404`.

## 3. Approved Scope

Included:

- `GET /api/pvms/production-monitor/meta`
- `GET /api/pvms/production-monitor/overview`
- `GET /api/pvms/production-monitor/output`
- `GET /api/pvms/production-monitor/load`
- `GET /api/pvms/production-monitor/dispatch`
- `GET /api/pvms/production-monitor/weather`
- embedded H2 schema and seed data for `M02`
- backend service-side calculation rules
- handover and module documentation updates

Not included in this batch:

- `forecast` module migration
- `strategy` module migration
- `station archive` data migration
- replacing the frontend production-monitor UI
- full production-grade external integrations

## 4. Architecture Decision

### 4.1 Frontend responsibilities

The frontend keeps:

- page shell and tab switching
- query state management
- charts and tables
- user interactions such as search, filters, and drawers

The frontend does not remain the source of truth for:

- resource unit facts
- member station facts
- current load/output status
- dispatch records
- shared weather context

### 4.2 Backend responsibilities

The backend owns:

- the `M02` fact model
- H2 seed data
- aggregation and indicator calculation
- granularity conversion from base timeseries
- response assembly for all `production-monitor` views

### 4.3 Data-source strategy

The development environment uses H2 as an embedded persistence layer.

The backend stores seed facts and calculates derived outputs from them.

This preserves two future extension paths:

1. replace H2 with MySQL/PostgreSQL
2. replace seed records with synchronization from real upstream systems

## 5. Data Model

This batch should introduce one unified `production-monitor` fact model.

### 5.1 `pm_resource_unit`

Purpose:

- source of truth for resource-unit level metadata

Core fields:

- `id`
- `name`
- `region`
- `city`
- `status`
- `cluster_radius_km`
- `dispatch_mode`
- `strategy_label`

### 5.2 `pm_station`

Purpose:

- member station identity and static modeling factors

Core fields:

- `id`
- `resource_unit_id`
- `name`
- `capacity_mw`
- `weight`
- `status`
- `online_rate`
- `alarm_count`

### 5.3 `pm_station_realtime_snapshot`

Purpose:

- current station-level facts used by overview and load views

Core fields:

- `station_id`
- `snapshot_time`
- `realtime_power_kw`
- `load_kw`
- `availability`
- `health_grade`

### 5.4 `pm_weather_snapshot`

Purpose:

- shared weather context for one resource unit

Core fields:

- `resource_unit_id`
- `snapshot_time`
- `weather`
- `cloudiness`
- `temperature`
- `irradiance`
- `humidity`
- `wind_speed`
- `conclusion`

### 5.5 `pm_station_curve_15m`

Purpose:

- single source of truth for output/load timeseries at base granularity

Core fields:

- `station_id`
- `biz_date`
- `time_slot`
- `load_kw`
- `pv_power_kw`
- `forecast_power_kw`
- `baseline_power_kw`
- `irradiance`
- `temperature`

This table is intentionally fixed at `15m` granularity. `30m` and `60m` responses are derived in backend code by aggregation.

### 5.6 `pm_dispatch_record`

Purpose:

- dispatch execution records for the dispatch view

Core fields:

- `id`
- `resource_unit_id`
- `issued_at`
- `command_type`
- `target_power_mw`
- `actual_power_mw`
- `response_seconds`
- `status`
- `deviation_reason`

## 6. API Contract Strategy

The frontend contract should remain stable in this batch.

### 6.1 `GET /api/pvms/production-monitor/meta`

Backend still returns:

- `defaultResourceUnitId`
- `regionOptions`
- `resourceUnits`

But all resource-unit items now come from `pm_resource_unit` plus derived status metadata.

### 6.2 `GET /api/pvms/production-monitor/overview`

Backend still returns:

- `info`
- `kpis`
- `memberStations`
- `weatherBrief`
- `alarmBrief`
- `summaryTable`

All values are derived from H2-backed facts instead of hardcoded Java lists.

### 6.3 `GET /api/pvms/production-monitor/output`

Backend still returns:

- `summary`
- `curve`
- `weatherTrend`
- `contributionRanking`
- `table`

But the curve data is now built from `pm_station_curve_15m`.

### 6.4 `GET /api/pvms/production-monitor/load`

This endpoint is added on the backend and returns the existing frontend contract:

- `summary`
- `stations`

Each station row includes:

- `id`
- `name`
- `capacityKwp`
- `realtimePowerKw`
- `loadKw`
- `adjustableKw`
- `maxRampRate`
- `status`
- `gridInteraction`

### 6.5 `GET /api/pvms/production-monitor/dispatch`

Backend still returns:

- `summary`
- `executionTrend`
- `riskHints`
- `records`

But it now reads dispatch records from H2 and computes summary counts and execution rates in service code.

### 6.6 `GET /api/pvms/production-monitor/weather`

Backend still returns:

- `summary`
- `trend`
- `impactTable`

Weather facts come from `pm_weather_snapshot`, and the impact rows are calculated in service code.

## 7. Backend Calculation Rules

The backend should clearly separate stored facts from derived outputs.

### 7.1 Stored facts

Store directly:

- resource-unit master data
- member-station master data
- current station realtime facts
- weather snapshot
- 15-minute station timeseries
- dispatch record rows

### 7.2 Calculated values

Calculate in service code:

- `regionOptions`
- `statusLabel`
- `statusColor`
- `overview` KPI cards
- `overview` summary-table rows
- `output` contribution ranking
- `load.summary.totalLoadMw`
- `load.summary.totalPvOutputMw`
- `load.summary.totalAdjustableMw`
- `load.summary.avgRampRate`
- `load.summary.stationCount`
- `load.summary.onlineCount`
- `load.station[*].adjustableKw`
- `load.station[*].maxRampRate`
- `load.station[*].gridInteraction`
- `dispatch` success rate and execution summary
- `weather` impact conclusions

## 8. Service Decomposition

The backend should be split into focused units:

- repository layer for resource unit, station, realtime snapshot, weather snapshot, timeseries, dispatch record
- shared calculator/helper layer for:
  - status metadata
  - granularity aggregation
  - load summary
  - ramp-rate calculation
  - grid-interaction curve building
- service layer for assembling each endpoint response

Recommended service structure:

- `ProductionMonitorMetaService`
- `ProductionMonitorOverviewService`
- `ProductionMonitorOutputService`
- `ProductionMonitorLoadService`
- `ProductionMonitorDispatchService`
- `ProductionMonitorWeatherService`

If the codebase prefers fewer files, a single orchestrator service may call focused internal calculators, but the logic boundaries should remain clear.

## 9. Testing Strategy

### 9.1 Backend tests

Extend `ProductionMonitorControllerTest` to cover:

- `meta` is H2-backed and returns expected unit count
- `overview` returns expected H2-derived KPI values
- `output` aggregates from `15m` facts
- `load` exists and returns the expected payload shape
- `dispatch` returns records and computed summary
- `weather` returns snapshot facts and derived impact rows

### 9.2 Critical behavioral assertions

At minimum, tests should assert:

- `/load` no longer returns `404`
- `load.summary.stationCount` matches seeded station rows
- `load.summary.onlineCount` excludes offline stations
- `adjustableKw` is derived, not stored
- `30m` and `60m` output views aggregate from `15m` rows
- dispatch success rate is computed from record statuses

## 10. Documentation Requirements

The docs must explain:

- which `production-monitor` data is seeded in H2
- which indicators are computed in backend code
- which data is a stand-in for upstream systems
- how to extend from H2 to real integrations later

The following docs should be updated after implementation:

- `docs/handover/05_后端维护手册.md`
- `docs/modules/backend/production-monitor.md`
- `docs/modules/frontend/production-monitor.md`
- `docs/handover/08_测试_排障_交接清单.md`

## 11. Future Extension Path

This design intentionally supports later integration with:

- GIS or asset master data for station/resource-unit identity
- SCADA / EMS for realtime power and load facts
- device platform for health and alarms
- forecast platform for prediction series
- revenue platform for settlement and yield metadata

The key rule is unchanged:

- raw facts can come from external systems
- aggregated indicators and business-facing summaries should still be calculated in backend services

## 12. Success Criteria

This batch is considered complete when:

- `GET /api/pvms/production-monitor/load` exists and returns `200`
- all `production-monitor` endpoints use one H2-backed fact model
- the frontend can load `M02` in development without relying on frontend mock data
- summary indicators for `M02` are calculated in backend code
- the handover docs explain the current implementation and future extensibility
