# Dashboard Map Static/Dynamic Split Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Keep the dashboard map base map in the frontend while moving dashboard map points, alarm feed, and VPP node summary into an embedded H2-backed backend data source.

**Architecture:** The frontend continues to own AMap SDK loading and marker rendering, but the backend becomes the source of truth for dynamic dashboard map data. A minimal H2 schema stores seeded station, station status, alarm, and VPP node records; service code calculates filters, status summaries, and capacity aggregates from those stored facts while preserving the existing frontend API contracts.

**Tech Stack:** Spring Boot 3.2, Spring JDBC, H2, MockMvc, Vue 2, Axios, Markdown handover docs

---

## File Map

### Backend files to create

- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardStationGeoRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardAlarmSnapshotRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardVppNodeSnapshotRepository.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMapDataService.java`
- `backend/src/main/resources/schema.sql`
- `backend/src/main/resources/data.sql`

### Backend files to modify

- `backend/pom.xml`
- `backend/src/main/resources/application.yml`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/AlarmController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMockService.java`
- `backend/src/test/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardControllerTest.java`

### Documentation files to modify

- `docs/README.md`
- `docs/handover/README.md`
- `docs/handover/09_地图静态底图与动态点位下沉方案.md`
- `docs/modules/backend/dashboard.md` if present, otherwise create `docs/modules/backend/dashboard.md`
- `docs/modules/frontend/dashboard.md` if present, otherwise create `docs/modules/frontend/dashboard.md`

### Notes on scope

- Keep the frontend map rendering code unchanged unless backend contract drift forces a small compatibility fix.
- Do not migrate unrelated dashboard endpoints in this batch.
- Do not replace AMap or proxy AMap tile requests through the backend.

---

### Task 1: Add the embedded H2 persistence foundation

**Files:**
- Modify: `backend/pom.xml`
- Modify: `backend/src/main/resources/application.yml`
- Create: `backend/src/main/resources/schema.sql`
- Create: `backend/src/main/resources/data.sql`

- [ ] **Step 1: Add a failing backend context test expectation**

Add or extend a backend controller test so the application context will require database startup and seeded dashboard records.

Expected future assertions:
- `/api/pvms/dashboard/stations-geo` returns 6 seeded stations
- `/api/pvms/alarms/recent` returns seeded alarm data
- `/api/pvms/dashboard/vpp-node-status` returns a seeded node id

- [ ] **Step 2: Run the focused dashboard controller test to verify the current code is not database-backed**

Run: `mvn -Dtest=DashboardControllerTest test`

Expected:
- test currently passes against hardcoded service data
- no H2 datasource exists yet

- [ ] **Step 3: Add minimal persistence dependencies and configuration**

Implement:
- `spring-boot-starter-jdbc`
- `com.h2database:h2`
- H2 datasource settings in `application.yml`
- SQL init enabled for dev/test startup

- [ ] **Step 4: Create the schema**

Create tables:
- `dashboard_station`
- `dashboard_station_status_snapshot`
- `dashboard_alarm_snapshot`
- `dashboard_vpp_node_snapshot`

Store seeded facts only:
- coordinates
- region
- capacity
- status
- current realtime power
- current daily totals
- alarms
- VPP seed snapshot

- [ ] **Step 5: Seed the dashboard map data**

Insert the current six station rows, their latest status snapshots, alarm rows, and one VPP node snapshot into `data.sql`.

- [ ] **Step 6: Restart the focused dashboard test**

Run: `mvn -Dtest=DashboardControllerTest test`

Expected:
- tests still pass after H2 startup is introduced
- database bootstraps cleanly with seeded rows

---

### Task 2: Lock the API behavior with failing endpoint tests

**Files:**
- Modify: `backend/src/test/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardControllerTest.java`

- [ ] **Step 1: Add failing assertions for database-backed station geo behavior**

Add assertions that cover:
- `resourceUnitId` and `resourceUnitName` are present on station rows
- `filters.regionOptions` is derived from stored station regions
- `summary[*].count` reflects current status snapshots
- filtering by `status`, `region`, and `capacityRange` still works

- [ ] **Step 2: Add failing assertions for alarm and VPP derived data**

Add assertions that cover:
- `/api/pvms/alarms/recent` summary counts come from snapshot rows
- `items[0]` includes station metadata and suggestion text
- `/api/pvms/dashboard/vpp-node-status` calculates `availableCapacityMw` and `onlineStations` from current station status snapshots, not only from the seed row

- [ ] **Step 3: Run the focused dashboard test and verify failure**

Run: `mvn -Dtest=DashboardControllerTest test`

Expected:
- FAIL because the current `DashboardMockService` does not expose `resourceUnitId/resourceUnitName`
- FAIL because VPP derivation is still hardcoded

- [ ] **Step 4: Keep the failing assertions in place**

Do not loosen the new assertions. They define the migration contract.

---

### Task 3: Implement repositories and the dashboard map data service

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardStationGeoRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardAlarmSnapshotRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/repository/DashboardVppNodeSnapshotRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/service/DashboardMapDataService.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/DashboardController.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/dashboard/controller/AlarmController.java`

- [ ] **Step 1: Implement the station repository**

Query the current station map view from:
- `dashboard_station`
- `dashboard_station_status_snapshot`

Return a focused row model containing:
- station identity
- resource unit data
- region
- longitude/latitude
- capacity
- current status
- realtime power
- today energy
- today revenue
- availability
- health grade
- address
- snapshot timestamp if needed for future extension

- [ ] **Step 2: Implement the alarm repository**

Load recent alarms with:
- station join for station name fallback
- optional `level` filter
- optional `stationId` filter
- deterministic sort order by newest row first

- [ ] **Step 3: Implement the VPP node repository**

Load the current seed node snapshot by node id or the single default node row.

- [ ] **Step 4: Implement `DashboardMapDataService`**

Responsibilities:
- derive `filters.statusOptions`
- derive `filters.regionOptions`
- keep the existing `capacityOptions` buckets
- calculate `summary[*].count` from filtered station rows
- map backend status keys to `statusLabel` and `statusColor`
- calculate `availableCapacityMw` from non-offline and non-fault station capacity
- calculate `onlineStations` from non-offline station count
- merge seed VPP adjustable range and heartbeat with live aggregate counts

- [ ] **Step 5: Rewire controllers**

Use `DashboardMapDataService` for:
- `/api/pvms/dashboard/stations-geo`
- `/api/pvms/alarms/recent`
- `/api/pvms/dashboard/vpp-node-status`

Keep `DashboardMockService` in place for:
- `/api/pvms/dashboard/kpi-summary`
- `/api/pvms/dashboard/power-curve`
- `/api/pvms/dashboard/station-ranking`
- `/api/pvms/dashboard/overview`
- `/api/pvms/weather/current`

- [ ] **Step 6: Run the focused dashboard test again**

Run: `mvn -Dtest=DashboardControllerTest test`

Expected:
- PASS
- new assertions validate the H2-backed implementation

---

### Task 4: Verify backend behavior against the running frontend contract

**Files:**
- Modify only if verification reveals a contract mismatch:
  - `frontend/src/modules/dashboard/components/DashboardMapCard.vue`
  - `frontend/src/api/pvms.js`
  - `frontend/src/shared/host/bridge.js`

- [ ] **Step 1: Start the backend with the new H2-backed dashboard data**

Run: `mvn spring-boot:run`

Expected:
- backend starts on `http://127.0.0.1:8091`
- H2 schema and seed data initialize without SQL errors

- [ ] **Step 2: Probe the three map-side endpoints directly**

Run:
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/dashboard/stations-geo`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/alarms/recent`
- `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/dashboard/vpp-node-status`

Expected:
- all return HTTP 200
- payload shape matches the existing frontend expectations

- [ ] **Step 3: Verify the frontend still consumes the contract**

Run the dev frontend and confirm the dashboard page:
- loads station markers
- updates the summary pills
- shows alarm insight content
- shows VPP node status

If a small compatibility issue appears, patch only the contract adapter, not the map rendering architecture.

- [ ] **Step 4: Run regression checks**

Run:
- `mvn test`
- `npm.cmd test -- backend-bridge.spec.js`

Expected:
- backend tests pass
- bridge regression still proves dev mode uses backend data

---

### Task 5: Sync handover and extension documentation

**Files:**
- Modify: `docs/README.md`
- Modify: `docs/handover/README.md`
- Modify: `docs/handover/09_地图静态底图与动态点位下沉方案.md`
- Create or modify: `docs/modules/backend/dashboard.md`
- Create or modify: `docs/modules/frontend/dashboard.md`

- [ ] **Step 1: Document the implemented static/dynamic boundary**

Explain clearly:
- frontend owns AMap SDK and static map rendering
- backend owns station points, alarm feed, and VPP node dynamic facts

- [ ] **Step 2: Document the H2 seed model**

List:
- every introduced table
- the meaning of each table
- which fields are stored facts
- which values are calculated in service code

- [ ] **Step 3: Document extension guidance for real systems**

Explain how to replace H2 seed data with:
- GIS master data
- station asset registry
- SCADA/EMS realtime measurements
- alarm platform events
- forecast/revenue systems in later phases

State explicitly that:
- some metrics should remain backend-calculated in real deployments
- dev seed data is a stand-in for upstream system integration, not the final architecture

- [ ] **Step 4: Document troubleshooting steps**

Cover:
- backend startup SQL failures
- missing H2 seed rows
- dashboard map loads but no markers
- AMap tile requests fail while backend points still work

- [ ] **Step 5: Final verification pass**

Re-read the handover docs as if the maintainer has zero context and ensure the setup sequence, endpoint ownership, and extension path are obvious.

---

## Verification Checklist

- [ ] `mvn -Dtest=DashboardControllerTest test`
- [ ] `mvn test`
- [ ] `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/dashboard/stations-geo`
- [ ] `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/alarms/recent`
- [ ] `Invoke-WebRequest http://127.0.0.1:8091/api/pvms/dashboard/vpp-node-status`
- [ ] `npm.cmd test -- backend-bridge.spec.js`

## Expected Outcome

After this plan is implemented:

- the dashboard map keeps its frontend base map and rendering path
- station points, recent alarms, and VPP node status come from the backend database
- H2 provides a complete embedded dev dataset for the dashboard map chain
- summary counts and capacity aggregates are calculated in backend service logic
- the handover docs explain both the current dev design and the production extension path
