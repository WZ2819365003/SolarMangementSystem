# Forecast H2 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the backend `forecast` module on H2 so the existing frontend pages load real Spring Boot responses instead of frontend mock data.

**Architecture:** Add a new `forecast` module in Spring Boot using the same repository/service/controller style as `productionmonitor` and `stationarchive`. Forecast metadata and future prediction seeds live in `fc_*` tables, actual series reuse existing `sa_*` and `pm_*` system facts, and backend services calculate presentation metrics before returning the current frontend contract.

**Tech Stack:** Spring Boot 3.2, JdbcClient, H2, JUnit 5, existing Vue 2 frontend contract, Markdown handover docs

---

## File Structure

- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastController.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastDataService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastMetricsCalculator.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastSeriesService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastStationRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastCurveRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastPredictionRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastModelRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastErrorSampleRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastMonthlyAccuracyRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastAdjustableWindowRepository.java`
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`
- Create: `backend/src/test/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastControllerTest.java`
- Modify: `docs/modules/backend/forecast.md`
- Modify: `docs/modules/frontend/forecast.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`
- Create: `docs/handover/12_M03_预测分析_H2数据与计算说明.md`

## Task 1: Write the backend contract test first

**Files:**
- Create: `backend/src/test/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastControllerTest.java`

- [ ] **Step 1: Write failing tests for all six endpoints**

Create one controller test class with test methods for:

- `shouldReturnForecastMeta`
- `shouldReturnForecastOverview`
- `shouldReturnForecastComparison`
- `shouldReturnForecastDeviationHeatmap`
- `shouldReturnForecastAdjustable`
- `shouldReturnForecastAccuracy`

Assert:

- `$.code == 0`
- the expected top-level fields exist
- representative list lengths are non-zero
- specific seed-backed values exist for one known station

- [ ] **Step 2: Run the new forecast test to verify it fails**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- compile or bean wiring failure because the `forecast` backend module does not exist yet

## Task 2: Add H2 schema and seed data for forecast

**Files:**
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`

- [ ] **Step 1: Extend schema with `fc_*` tables**

Add:

- `fc_model`
- `fc_station_model_binding`
- `fc_prediction_series_15m`
- `fc_error_sample`
- `fc_monthly_accuracy_snapshot`
- `fc_adjustable_window`

Keep the schema consistent with existing table naming and FK usage.

- [ ] **Step 2: Seed deterministic forecast metadata and series**

Insert:

- at least 2 models
- bindings for all stations used by the forecast UI
- future prediction series for `2026-03-30`
- seeded error samples sufficient for histogram and heatmap
- monthly snapshots for at least 6 months
- adjustable windows for all displayed stations

Use existing `sa_station` IDs so the backend can join actual and predicted data.

- [ ] **Step 3: Re-run the forecast test**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- still failing because repositories/controller/service do not exist

## Task 3: Implement the repository layer

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastStationRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastCurveRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastPredictionRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastModelRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastErrorSampleRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastMonthlyAccuracyRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/repository/ForecastAdjustableWindowRepository.java`

- [ ] **Step 1: Implement station and actual-curve repositories**

Follow the `JdbcClient` style used by `ProductionMonitorCurveRepository` and `StationArchive...Repository`.

Repository responsibilities:

- station metadata with company/region join
- actual curve reads from `sa_station_curve_15m`
- optional weather/resource-unit context reads where needed

- [ ] **Step 2: Implement forecast-seed repositories**

Add repositories for:

- prediction series
- model metadata
- error samples
- monthly accuracy snapshots
- adjustable windows

- [ ] **Step 3: Compile tests again**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- now failing at missing service/controller layer, not repository compilation

## Task 4: Implement shared series and metrics services

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastSeriesService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastMetricsCalculator.java`

- [ ] **Step 1: Implement `ForecastSeriesService`**

Include helpers for:

- time-axis generation from 15-minute slots
- slot-to-hour aggregation
- converting rows to numeric series
- building chart-ready arrays
- formatting timeline window labels

- [ ] **Step 2: Implement `ForecastMetricsCalculator`**

Include helpers for:

- deviation percentage
- accuracy percentage
- MAE
- RMSE
- qualified rate
- monthly improvement percent
- hourly heatmap aggregation

- [ ] **Step 3: Add focused calculator assertions if needed**

If controller-only tests become too indirect, add a small service test file for `MAE`, `RMSE`, or heatmap aggregation.

## Task 5: Implement the `ForecastDataService`

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/service/ForecastDataService.java`

- [ ] **Step 1: Implement `getMeta()`**

Return:

- `regions`
- `stations`
- optional `forecastTypes`

Use real station rows, not duplicated hardcoded arrays.

- [ ] **Step 2: Implement `getOverview()`**

Return:

- `kpis`
- `stationTable`

Use actual station output from `sa_station_curve_15m` and predicted values from `fc_prediction_series_15m`.

- [ ] **Step 3: Implement `getComparison()` and `getDeviationHeatmap()`**

Return the exact structures consumed by `ForecastOverviewView.vue`:

- `timeLabels`
- `series.dayAhead`
- `series.ultraShort`
- `series.actual`
- `hours`
- `stations`
- `data`

- [ ] **Step 4: Implement `getAdjustable()`**

Return:

- `kpis`
- `capacityCurve`
- `timeline`
- `stationTable`

The adjustable values should derive from actual load/output space plus forecast bounds, not only from static seed fields.

- [ ] **Step 5: Implement `getAccuracy()`**

Return:

- `kpis`
- `trend`
- `distribution`
- `stationRanking`
- `monthlyTable`

- [ ] **Step 6: Re-run the forecast test**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- failing only because the controller is not wired yet, or because payload shapes still need adjustment

## Task 6: Implement the controller

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastController.java`

- [ ] **Step 1: Add six `GET` endpoints**

Implement:

- `/api/pvms/forecast/meta`
- `/api/pvms/forecast/overview`
- `/api/pvms/forecast/adjustable`
- `/api/pvms/forecast/accuracy`
- `/api/pvms/forecast/comparison`
- `/api/pvms/forecast/deviation-heatmap`

Use the same `ApiResponse.success(...)` pattern as the rest of the backend.

- [ ] **Step 2: Run the forecast controller test**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- green, or with a small number of payload-shape failures to fix

## Task 7: Turn the forecast tests green and run full backend verification

**Files:**
- Modify any of the new forecast files as required by failing tests

- [ ] **Step 1: Fix only the failing payload mismatches**

Adjust field names and computed values until the test matches the frontend contract exactly.

- [ ] **Step 2: Run the forecast controller test again**

Run:

```bash
mvn -Dtest=ForecastControllerTest test
```

Expected:

- PASS

- [ ] **Step 3: Run the full backend suite**

Run:

```bash
mvn test
```

Expected:

- all backend tests pass, including dashboard, production-monitor, stationarchive, stations, system, and forecast

## Task 8: Update module and handover docs

**Files:**
- Modify: `docs/modules/backend/forecast.md`
- Modify: `docs/modules/frontend/forecast.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`
- Create: `docs/handover/12_M03_预测分析_H2数据与计算说明.md`

- [ ] **Step 1: Rewrite backend forecast module doc**

Document:

- endpoint list
- source tables
- which fields are reused from other modules
- which metrics are calculated

- [ ] **Step 2: Update frontend forecast doc**

Document the exact backend contract each view consumes.

- [ ] **Step 3: Add handover note for H2 and extensibility**

Explain:

- seeded prediction metadata
- reused actual series
- upgrade path to real upstream forecast systems

- [ ] **Step 4: Update maintenance and troubleshooting docs**

Add:

- forecast verification commands
- common integration risks
- where to change forecast seeds safely

## Task 9: Verify backend runtime and integration endpoints

**Files:**
- No source edits expected unless verification finds a real issue

- [ ] **Step 1: Start the backend from the forecast branch**

Run:

```bash
mvn spring-boot:run
```

Verify:

- `http://127.0.0.1:8091/api/system/health`
- `http://127.0.0.1:8091/api/pvms/forecast/meta`
- `http://127.0.0.1:8091/api/pvms/forecast/overview`
- `http://127.0.0.1:8091/api/pvms/forecast/adjustable`
- `http://127.0.0.1:8091/api/pvms/forecast/accuracy`

- [ ] **Step 2: Verify the frontend proxy path**

With the frontend already running on `6618`, check:

```bash
http://127.0.0.1:6618/api/pvms/forecast/meta
http://127.0.0.1:6618/api/pvms/forecast/overview
```

Expected:

- `200` responses through the dev proxy

## Task 10: Merge back to `dev` and restart the backend service

**Files:**
- No new source files expected beyond merge metadata

- [ ] **Step 1: Commit the forecast branch**

```bash
git add backend/src/main/java/cn/techstar/pvms/backend/module/forecast backend/src/main/resources/schema.sql backend/src/main/resources/data.sql backend/src/test/java/cn/techstar/pvms/backend/module/forecast docs/modules/backend/forecast.md docs/modules/frontend/forecast.md docs/handover/05_后端维护手册.md docs/handover/08_测试_排障_交接清单.md docs/handover/12_M03_预测分析_H2数据与计算说明.md docs/superpowers/specs/2026-03-30-forecast-h2-design.md docs/superpowers/plans/2026-03-30-forecast-h2-migration.md
git commit -m "feat: add H2-backed forecast backend module"
```

- [ ] **Step 2: Merge `codex/forecast-h2` into `dev`**

Use a non-interactive merge from the isolated worktree.

- [ ] **Step 3: Push `dev`**

```bash
git push origin dev
```

- [ ] **Step 4: Replace the running backend**

Stop the old process on `8091`, then start the backend from merged `dev`, and verify health plus one forecast endpoint.

---

## Execution Notes

- Use only the current frontend contract. Do not redesign the Vue payload shape in this batch.
- Prefer small helper methods and repository records over large, anonymous `Map` assembly blocks where possible.
- Keep calculations deterministic so regression tests do not flicker.
- Reuse existing H2 data from `sa_*` and `pm_*` tables instead of duplicating actual station facts into `fc_*`.
- Do not commit `target/` outputs or runtime logs.
