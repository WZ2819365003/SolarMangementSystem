# Forecast H2 Migration Design

## 1. Background

The frontend already ships a complete `forecast` module with three views:

- overview
- adjustable-capacity analysis
- accuracy evaluation

The frontend calls six backend endpoints through `frontend/src/api/pvms.js`:

- `GET /api/pvms/forecast/meta`
- `GET /api/pvms/forecast/overview`
- `GET /api/pvms/forecast/adjustable`
- `GET /api/pvms/forecast/accuracy`
- `GET /api/pvms/forecast/comparison`
- `GET /api/pvms/forecast/deviation-heatmap`

At the current `dev` branch state, the backend has no `forecast` module implementation at all. The UI therefore depends on the frontend mock layer and cannot verify the real backend contract.

The user requirement for this batch is explicit:

- build the `forecast` backend using embedded H2 in development
- keep the frontend static pages unchanged where practical
- prediction inputs and model metadata can be seeded
- actual values should come from existing system data, not be duplicated blindly
- summary metrics, error metrics, and forecast-related indicators must be calculated in backend code
- documentation must explain present behavior and future extensibility to real external forecast systems

## 2. Goal

Build a complete H2-backed `forecast` backend module that satisfies the current frontend contract and separates:

- system facts already owned by other modules
- forecast metadata and seeded prediction results
- derived forecast indicators calculated by backend services

After implementation:

- development mode should load `forecast` from Spring Boot instead of frontend mock data
- the backend should return the exact shape that the current Vue views consume
- the docs should make it clear which data is seed data and which values are calculated

## 3. Approved Data Boundary

This batch uses the following boundary:

### 3.1 System facts pulled from existing modules

The `forecast` backend should reuse existing H2-backed facts already established by the system:

- station master data from `sa_station`
- company and region affiliation from `sa_company`
- actual PV output and station-level forecast history from `sa_station_curve_15m`
- resource-unit level weather and operating context from `pm_resource_unit`, `pm_weather_snapshot`, and `pm_station_curve_15m` where needed
- current station status and data quality from `sa_station`

### 3.2 Forecast-owned metadata seeded in H2

The `forecast` module itself should seed:

- forecast model catalog and model version metadata
- station-to-model assignment
- forecast scenario metadata
- confidence-band metadata
- station-level day-ahead and ultra-short future prediction series
- historical error-sample distributions
- monthly forecast evaluation baselines
- adjustable-capacity forecast windows

### 3.3 Backend-calculated outputs

The backend service layer should calculate:

- `dayAheadAccuracy`
- `ultraShortAccuracy`
- `dailyDeviation`
- `qualifiedRate`
- `MAE`
- `RMSE`
- heatmap matrices
- station ranking
- monthly improvement percentages
- adjustable-capacity ranges
- timeline availability windows

The backend must not store these as final presentation fields when they can be derived from the seed facts.

## 4. API Contract Strategy

The frontend contract stays unchanged in this batch.

### 4.1 `GET /api/pvms/forecast/meta`

Return the shape the page already expects:

- `regions`
- `stations`
- optional supporting fields that do not break the frontend, such as `forecastTypes` and `defaultDateRange`

### 4.2 `GET /api/pvms/forecast/overview`

Return:

- `kpis`
- `stationTable`

The data must directly satisfy `ForecastOverviewView.vue`.

### 4.3 `GET /api/pvms/forecast/comparison`

Return:

- `timeLabels`
- `series.dayAhead`
- `series.ultraShort`
- `series.actual`

### 4.4 `GET /api/pvms/forecast/deviation-heatmap`

Return:

- `hours`
- `stations`
- `data`

Where `data` is the two-dimensional matrix that the current heatmap view flattens locally.

### 4.5 `GET /api/pvms/forecast/adjustable`

Return:

- `kpis`
- `capacityCurve`
- `timeline`
- `stationTable`

### 4.6 `GET /api/pvms/forecast/accuracy`

Return:

- `kpis`
- `trend`
- `distribution`
- `stationRanking`
- `monthlyTable`

## 5. Data Model

This batch should add a dedicated `forecast` H2 model using `fc_*` tables.

### 5.1 `fc_model`

Purpose:

- model metadata and version registry

Core fields:

- `id`
- `name`
- `type`
- `version`
- `provider`
- `description`
- `status`

### 5.2 `fc_station_model_binding`

Purpose:

- station-level default model selection

Core fields:

- `station_id`
- `day_ahead_model_id`
- `ultra_short_model_id`
- `confidence_level`

### 5.3 `fc_prediction_series_15m`

Purpose:

- future prediction seed data at 15-minute granularity

Core fields:

- `station_id`
- `biz_date`
- `time_slot`
- `forecast_type`
- `predicted_power_kw`
- `upper_bound_kw`
- `lower_bound_kw`
- `scenario_tag`

### 5.4 `fc_error_sample`

Purpose:

- error-sample seeds used for histogram distribution and heatmap support

Core fields:

- `station_id`
- `biz_date`
- `hour_slot`
- `forecast_type`
- `error_kw`
- `qualified`

### 5.5 `fc_monthly_accuracy_snapshot`

Purpose:

- monthly summary baselines used for the accuracy table

Core fields:

- `station_id`
- `month_key`
- `mae_kw`
- `rmse_kw`
- `accuracy_pct`

### 5.6 `fc_adjustable_window`

Purpose:

- station-level availability windows for the adjustable timeline

Core fields:

- `station_id`
- `biz_date`
- `window_order`
- `start_slot`
- `end_slot`
- `window_status`

## 6. Calculation Rules

The backend should follow a strict separation:

### 6.1 Stored facts

Store directly:

- seeded model metadata
- seeded future prediction series
- seeded confidence bounds
- seeded error samples
- seeded monthly baselines
- seeded adjustable windows

### 6.2 Reused system facts

Read from existing module tables:

- actual station output from `sa_station_curve_15m.pv_output_kw`
- historical station forecast comparison inputs from `sa_station_curve_15m.forecast_day_ahead_kw` and `forecast_ultra_short_kw`
- station region/company/status metadata from `sa_station` and `sa_company`

### 6.3 Derived values

Calculate in code:

- overview KPI cards from actual vs prediction deviations
- station table deviations and accuracy
- comparison payload actual series + seeded prediction series
- heatmap matrix from seeded error samples or recalculated hourly deviations
- adjustable KPI totals from station-level prediction windows plus actual operating space
- accuracy KPIs from actual and predicted series
- distribution bins from error samples
- ranking and monthly improvement deltas

## 7. Backend Structure

The new module should follow the established repository/service/controller pattern already used by `productionmonitor` and `stationarchive`.

### 7.1 Controller

Create:

- `backend/src/main/java/cn/techstar/pvms/backend/module/forecast/controller/ForecastController.java`

### 7.2 Repository layer

Create focused JDBC repositories:

- `ForecastStationRepository`
- `ForecastCurveRepository`
- `ForecastPredictionRepository`
- `ForecastModelRepository`
- `ForecastErrorSampleRepository`
- `ForecastMonthlyAccuracyRepository`
- `ForecastAdjustableWindowRepository`

### 7.3 Service layer

Preferred service split:

- `ForecastDataService` as orchestrator for endpoint assembly
- `ForecastMetricsCalculator` for shared metrics
- `ForecastSeriesService` for 15-minute series conversion, axis building, hourly aggregation, and bound calculations

This mirrors the current codebase style without over-fragmenting it.

## 8. Testing Strategy

### 8.1 Controller contract tests

Add `ForecastControllerTest` and cover all six endpoints:

- status `200`
- response code `0`
- expected field presence and collection lengths
- representative value assertions proving H2-backed behavior

### 8.2 Behavioral assertions

At minimum, tests should prove:

- the module exists on the backend and no longer relies on frontend mock data
- overview returns `kpis` and `stationTable`
- comparison returns `series.dayAhead`, `series.ultraShort`, and `series.actual`
- heatmap returns a station-by-hour matrix
- adjustable returns timeline windows and station rows
- accuracy returns trend, distribution, ranking, and monthly table

### 8.3 Regression safety

Run the existing backend suite after adding the module to ensure no impact on:

- dashboard
- production-monitor
- stationarchive
- system health

## 9. Documentation Requirements

Update the following docs after implementation:

- `docs/modules/backend/forecast.md`
- `docs/modules/frontend/forecast.md`
- `docs/handover/05_后端维护手册.md`
- `docs/handover/08_测试_排障_交接清单.md`

Add a dedicated handover note:

- `docs/handover/12_M03_预测分析_H2数据与计算说明.md`

The docs must clearly state:

- which forecast data is seeded in H2
- which actual values come from existing modules
- which metrics are calculated in backend code
- how to replace H2 seeds with upstream forecast-system synchronization later

## 10. Future Extension Path

This design is intentionally compatible with a future real forecast platform.

When a real upstream system becomes available, the replacement path is:

1. keep frontend contracts unchanged
2. keep backend-calculated KPI assembly unchanged
3. replace `fc_prediction_series_15m` and related seed ingestion with synchronized external prediction data
4. optionally replace `fc_model` and binding metadata with a model registry integration

The rule remains:

- raw prediction results can come from upstream systems
- business-facing metrics and page payloads should still be assembled and calculated by backend services

## 11. Success Criteria

This batch is complete when:

- all six `forecast` endpoints exist in Spring Boot
- the responses satisfy the current Vue components without frontend contract changes
- H2 stores forecast metadata and prediction seeds
- actual series are read from existing system tables
- forecast metrics are calculated in backend code
- the backend tests pass
- docs explain the current implementation and future extensibility
