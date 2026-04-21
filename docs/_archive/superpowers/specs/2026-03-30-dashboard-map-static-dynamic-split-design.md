# Dashboard Map Static/Dynamic Split Design

## 1. Background

This project currently mixes two different classes of map-related data in the dashboard:

- Static map resources:
  - AMap SDK loading
  - base tile/style rendering
  - frontend fallback grid rendering
- Dynamic business data:
  - station point coordinates
  - station status and color
  - realtime power
  - filter options and summary counts
  - recent alarms used by the map insight panel
  - VPP node summary used by the map insight panel

The frontend map component already has a clean rendering boundary in:

- `frontend/src/modules/dashboard/components/DashboardMapCard.vue`
- `frontend/src/shared/plugins/amap.js`

The dynamic station data is currently delivered by hardcoded backend mock logic in:

- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMockService.java`

The user requirement is now explicit:

- Static map resources stay in the frontend
- Dynamic point data moves into the backend database
- The scope of the first batch is the dashboard map chain
- Documentation must be updated together with the design

## 2. Goal

Build a dashboard map data architecture where:

- the frontend remains responsible for map rendering only
- the backend becomes the source of truth for station points and map-side dynamic business data
- the development environment uses an embedded H2 database
- the backend calculates summary and insight data from stored dynamic records instead of hardcoded arrays
- the handover docs clearly explain what is static, what is dynamic, and how to extend the system to real external sources later

## 3. Approved Scope

This design covers the dashboard map chain only.

Included:

- `/api/pvms/dashboard/stations-geo`
- `/api/pvms/alarms/recent`
- `/api/pvms/dashboard/vpp-node-status`
- dashboard map station summary
- dashboard map point list
- dashboard map insight panel dependencies
- H2 seed data for dashboard map dynamic records
- handover and design documentation updates

Not included in this batch:

- production-monitor map-like views
- station tree archival data
- forecast heatmap data
- strategy map extensions
- replacing AMap itself

## 4. Architecture Decision

### 4.1 Static Layer

The frontend keeps:

- AMap SDK loader
- AMap key/security configuration
- map instance lifecycle
- CircleMarker rendering
- fallback grid and marker layout
- viewport focus/reset interaction

This means the frontend still owns visual map behavior and third-party map integration risk.

### 4.2 Dynamic Layer

The backend owns:

- station geo coordinates
- station runtime status
- capacity and realtime power
- region/status/capacity filter derivation
- status summary counts
- recent alarm aggregation for the map insight panel
- VPP node aggregate capacity and availability

### 4.3 Storage Strategy

The development environment uses embedded H2 as the persistence layer.

Seed data is stored in database tables, not in long hardcoded `List.of(...)` arrays.

Calculated outputs are derived in service code from seed records.

This preserves two future extension paths:

1. replace H2 with MySQL/PostgreSQL
2. replace seeded records with external system synchronization

## 5. Data Model

The first batch should introduce a minimal but extensible schema.

### 5.1 `dashboard_station`

Purpose:
- station identity and geo point source of truth

Core fields:
- `id`
- `name`
- `resource_unit_id`
- `resource_unit_name`
- `region`
- `longitude`
- `latitude`
- `address`
- `capacity_kwp`

### 5.2 `dashboard_station_status_snapshot`

Purpose:
- current dynamic status used by the map and insight panel

Core fields:
- `station_id`
- `status`
- `realtime_power_kw`
- `today_energy_kwh`
- `today_revenue_cny`
- `availability`
- `health_grade`
- `snapshot_time`

### 5.3 `dashboard_alarm_snapshot`

Purpose:
- recent alarms used by the map insight panel and alarm brief panel

Core fields:
- `id`
- `station_id`
- `time_text`
- `level`
- `level_label`
- `device_name`
- `alarm_type`
- `description`
- `status`
- `owner`
- `suggestion`

### 5.4 `dashboard_vpp_node_snapshot`

Purpose:
- VPP node summary shown beside the map

Core fields:
- `node_id`
- `total_capacity_mw`
- `available_capacity_mw`
- `online_stations`
- `total_stations`
- `adjustable_min_mw`
- `adjustable_max_mw`
- `last_heartbeat`

### 5.5 Optional future tables

Not required in the first implementation, but the docs should mention them as the next step:

- `weather_snapshot`
- `forecast_timeseries`
- `production_timeseries`
- `revenue_record`

## 6. API Contract Strategy

The frontend contract should remain stable in this batch.

### 6.1 `/api/pvms/dashboard/stations-geo`

This endpoint continues to return:

- `filters`
- `summary`
- `stations`

But the data source changes from hardcoded service arrays to H2-backed records and service calculations.

`stations` should include at least:

- `id`
- `name`
- `region`
- `longitude`
- `latitude`
- `status`
- `statusLabel`
- `statusColor`
- `capacityKwp`
- `realtimePowerKw`
- `todayEnergyKwh`
- `todayRevenueCny`
- `healthGrade`
- `availability`
- `address`
- `resourceUnitId`
- `resourceUnitName`

### 6.2 `/api/pvms/alarms/recent`

No frontend contract change.

The backend should derive the map-side recent alarm payload from `dashboard_alarm_snapshot`.

### 6.3 `/api/pvms/dashboard/vpp-node-status`

No frontend contract change.

The backend should source or calculate the payload from `dashboard_vpp_node_snapshot` and station status aggregates.

## 7. Backend Calculation Rules

The backend should distinguish stored facts from calculated values.

### 7.1 Stored facts

Store directly:

- coordinates
- region
- capacity
- current status
- current realtime power
- current availability
- alarm rows
- VPP node seed snapshot

### 7.2 Calculated values

Calculate in service code:

- `filters.regionOptions`
- `filters.capacityOptions`
- `summary[*].count`
- `statusLabel`
- `statusColor`
- `availableCapacityMw`
- `onlineStations`
- any future per-status counts or map-side aggregates

This is important because the user explicitly wants the logic chain to remain in the backend.

## 8. Implementation Decomposition

Option 3 is intentionally broader than a single endpoint patch. It should be split into the following subprojects.

### Subproject A: Persistence Foundation

Deliverables:

- add H2 dependency and dev configuration
- create schema and seed files
- add repository layer for dashboard map dynamic data
- keep the app bootable in local dev

### Subproject B: Station Geo Dynamicization

Deliverables:

- move station geo and runtime snapshot data out of hardcoded arrays
- implement H2-backed `stations-geo`
- preserve current frontend response shape

### Subproject C: Map Insight Dynamicization

Deliverables:

- move recent map alarms into H2
- move VPP node summary into H2 or derive it from station records
- verify `DashboardMapInsightPanel` still renders without frontend mock dependencies

### Subproject D: Documentation Synchronization

Deliverables:

- add handover doc describing static vs dynamic map boundaries
- update backend maintenance guidance for H2 dev mode
- explain future replacement with real GIS, SCADA, alarm, and VPP sources

## 9. Error Handling

The design should preserve graceful degradation:

- if AMap SDK fails, the frontend still shows fallback grid markers
- fallback markers must still use backend-delivered station coordinates
- if the backend returns no stations, the map renders empty state, not stale hardcoded markers
- if alarm or VPP data is temporarily empty, the panel renders empty-safe data structures

## 10. Testing Strategy

Implementation should use TDD.

Minimum verification targets:

- controller test for `/api/pvms/dashboard/stations-geo`
- controller test for `/api/pvms/alarms/recent`
- controller test for `/api/pvms/dashboard/vpp-node-status`
- repository/service test for summary count calculation
- frontend regression test that confirms dashboard map requests backend data in dev mode

## 11. Documentation Requirements

The handover documentation must explicitly state:

- AMap tiles/styles are frontend static dependencies
- station point coordinates and statuses are backend dynamic records
- H2 is a development-only persistence strategy
- future production sources are expected to come from external systems
- summary counts and map-side insight aggregates are backend-calculated, not directly stored as final display values

## 12. Success Criteria

This design is complete when:

- the dashboard map renders using backend H2 station point data
- map filters and summary counts are backend-calculated
- map insight data no longer depends on frontend mock data
- AMap remains only a frontend rendering dependency
- handover docs clearly explain the static/dynamic split and future extension path
