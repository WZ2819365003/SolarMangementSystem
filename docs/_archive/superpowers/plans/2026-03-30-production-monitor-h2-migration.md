# Production Monitor H2 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Migrate the `production-monitor` module from in-memory mock data to one embedded H2-backed backend fact model, including the missing `/load` endpoint.

**Architecture:** The backend becomes the single source of truth for all `M02` views: `meta`, `overview`, `output`, `load`, `dispatch`, and `weather`. Raw facts are stored in H2 tables for resource units, stations, realtime snapshots, weather snapshots, base 15-minute timeseries, and dispatch records; service code calculates summaries, indicators, and aggregated view payloads from those facts while preserving the current frontend contract.

**Tech Stack:** Spring Boot 3.2, Spring JDBC, H2, MockMvc, Vue 2, ECharts, Markdown handover docs

---

## File Map

### Backend files to create

- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorResourceUnitRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorStationSnapshotRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorWeatherSnapshotRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorCurveRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorDispatchRecordRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorSeriesAggregator.java`

### Backend files to modify

- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorMockService.java`
- `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

### Documentation files to modify

- `docs/modules/backend/production-monitor.md`
- `docs/modules/frontend/production-monitor.md`
- `docs/handover/05_后端维护手册.md`
- `docs/handover/08_测试_排障_交接清单.md`

### Notes on scope

- Do not change the frontend `production-monitor` visual structure in this batch.
- Do not migrate `forecast`, `strategy`, or `station archive`.
- Keep the current request contract shape stable wherever practical.

---

### Task 1: Lock the missing `/load` contract with failing tests

**Files:**
- Modify: `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

- [ ] **Step 1: Write the failing test for `/load`**

Add a new test for:
- `GET /api/pvms/production-monitor/load`
- `resourceUnitId=RU-001`
- `date=2026-03-30`
- `granularity=15m`

Assertions:
- returns `200`
- `data.summary.totalLoadMw` exists
- `data.summary.stationCount` matches seeded station rows
- `data.stations[0].gridInteraction.times.length()` equals `96`

- [ ] **Step 2: Strengthen existing tests to prove H2-backed behavior**

Add assertions that:
- `meta.resourceUnits.length()` becomes the final seeded count
- `overview.info` comes from stored H2 facts
- `output` supports `15m` and still returns `96` points
- `dispatch.summary` and `records` are derived from stored dispatch rows
- `weather.summary` reflects stored weather facts

- [ ] **Step 3: Run the focused controller test to verify it fails**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- FAIL because `/load` is not implemented
- possibly FAIL because resource-unit count and other assertions no longer match the old hardcoded service

---

### Task 2: Extend the H2 schema for `M02`

**Files:**
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`

- [ ] **Step 1: Add the `production-monitor` tables to `schema.sql`**

Create:
- `pm_resource_unit`
- `pm_station`
- `pm_station_realtime_snapshot`
- `pm_weather_snapshot`
- `pm_station_curve_15m`
- `pm_dispatch_record`

- [ ] **Step 2: Seed the `M02` fact model in `data.sql`**

Insert:
- resource-unit rows
- station rows
- current station snapshots
- weather snapshots
- 15-minute timeseries rows for at least one business date
- dispatch records

Seed enough rows to support:
- all resource units used by the current frontend
- all load-table stations
- `15m` to `30m/60m` aggregation

- [ ] **Step 3: Re-run the focused test to confirm failure is now behavioral, not startup-related**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- context starts with H2 tables
- tests still fail because repositories and service wiring do not exist yet

---

### Task 3: Implement repositories and shared aggregation logic

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorResourceUnitRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorStationSnapshotRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorWeatherSnapshotRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorCurveRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/repository/ProductionMonitorDispatchRecordRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorSeriesAggregator.java`

- [ ] **Step 1: Implement the resource-unit repository**

Load:
- resource-unit master rows
- optional lookup by `resourceUnitId`

- [ ] **Step 2: Implement the station snapshot repository**

Load joined rows for:
- station identity
- current status
- realtime power
- current load
- availability / health

- [ ] **Step 3: Implement the weather repository**

Load one current weather snapshot per resource unit.

- [ ] **Step 4: Implement the curve repository**

Load `15m` rows by:
- `resourceUnitId`
- `bizDate`

Join station rows so the backend can build:
- total curves
- station load rows
- grid interaction curves

- [ ] **Step 5: Implement the dispatch repository**

Load dispatch rows by `resourceUnitId` with deterministic sort order.

- [ ] **Step 6: Implement the shared series aggregator**

Responsibilities:
- aggregate `15m` rows into `30m` and `60m`
- compute ramp rate
- build station grid interaction series
- sum resource-unit totals from station rows

- [ ] **Step 7: Run the focused controller test again**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- still FAIL because controller/service still points at the old mock service

---

### Task 4: Implement H2-backed `meta`, `overview`, and `weather`

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorMockService.java`

- [ ] **Step 1: Implement `getMeta` using H2**

Return:
- `defaultResourceUnitId`
- `regionOptions`
- `resourceUnits`

Derived values:
- `statusLabel`
- `statusColor`

- [ ] **Step 2: Implement `getOverview` using H2**

Build:
- `info`
- `kpis`
- `memberStations`
- `weatherBrief`
- `alarmBrief`
- `summaryTable`

Derived values must come from stored facts, not copied constants.

- [ ] **Step 3: Implement `getWeather` using H2**

Build:
- `summary`
- `trend`
- `impactTable`

- [ ] **Step 4: Rewire the controller for these endpoints**

Use the new H2-backed service methods for:
- `/meta`
- `/overview`
- `/weather`

- [ ] **Step 5: Run the focused controller test**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- some tests pass
- `output`, `load`, or `dispatch` related assertions still fail until later tasks

---

### Task 5: Implement H2-backed `output` and `/load`

**Files:**
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`
- Test: `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

- [ ] **Step 1: Implement `getOutput` from `pm_station_curve_15m`**

Build:
- `summary`
- `curve`
- `weatherTrend`
- `contributionRanking`
- `table`

Support:
- `15m`
- `30m`
- `60m`

- [ ] **Step 2: Implement `getLoad`**

Build:
- `summary`
- `stations`

Each station row must include:
- `loadKw`
- `realtimePowerKw`
- `adjustableKw`
- `maxRampRate`
- `status`
- `gridInteraction`

- [ ] **Step 3: Rewire the controller for `/output` and `/load`**

Add the new `/load` endpoint and point `/output` at the H2-backed service.

- [ ] **Step 4: Run the focused controller test again**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- `meta`
- `overview`
- `output`
- `load`
- `weather`
  all pass
- `dispatch` may still be the remaining failing area if not yet migrated

---

### Task 6: Implement H2-backed `dispatch` and remove endpoint drift

**Files:**
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorDataService.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- Modify: `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`

- [ ] **Step 1: Implement `getDispatch` from stored dispatch rows**

Build:
- `summary`
- `executionTrend`
- `riskHints`
- `records`

Derived values:
- issued count
- success count
- success rate
- average response time

- [ ] **Step 2: Re-run the focused controller test**

Run: `mvn -Dtest=ProductionMonitorControllerTest test`

Expected:
- PASS

- [ ] **Step 3: Remove or minimize obsolete production-monitor mock usage**

Keep only the code still needed for compatibility or delete dead branches if safe.

---

### Task 7: Verify full module behavior and sync docs

**Files:**
- Modify: `docs/modules/backend/production-monitor.md`
- Modify: `docs/modules/frontend/production-monitor.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`

- [ ] **Step 1: Run full backend verification**

Run: `mvn test`

Expected:
- all backend tests pass

- [ ] **Step 2: Probe the live `M02` endpoints**

Run:
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/meta`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/overview?resourceUnitId=RU-001`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/output?resourceUnitId=RU-001&granularity=15m`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/load?resourceUnitId=RU-001&date=2026-03-30&granularity=15m`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/dispatch?resourceUnitId=RU-001`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/weather?resourceUnitId=RU-001`

Expected:
- all return `200`
- `/load` no longer returns `404`

- [ ] **Step 3: Verify frontend integration manually**

Open the `production-monitor` page in dev mode and confirm:
- overview view loads
- load view loads data
- dispatch view loads data
- no frontend fallback mock path is required

- [ ] **Step 4: Update docs to match the implementation**

Document:
- the new H2 tables
- which values are stored facts
- which values are backend-calculated
- how `15m -> 30m/60m` aggregation works
- how to extend to real SCADA / weather / dispatch / forecast / revenue systems later

---

## Verification Checklist

- [ ] `mvn -Dtest=ProductionMonitorControllerTest test`
- [ ] `mvn test`
- [ ] `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/production-monitor/load?resourceUnitId=RU-001&date=2026-03-30&granularity=15m`
- [ ] frontend dev page loads `M02` from backend

## Expected Outcome

After this plan is implemented:

- `production-monitor` no longer depends on in-memory Java lists for its main data chain
- `/load` exists and returns the expected contract
- all `M02` endpoints read from one H2-backed fact model
- derived indicators are calculated in backend code
- handover docs clearly explain current implementation and future extension paths
