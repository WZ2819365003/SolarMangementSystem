# M04 Strategy Management H2 Migration Design

## 1. Background

As of `2026-03-30`, the repository has already migrated three major data chains away from frontend-only mock data:

- `dashboard`
- `production-monitor`
- `forecast`

`strategy` is the next major gap. The current repository state is weaker than the existing documentation suggests:

- there is no standalone frontend `strategy` module under `frontend/src/modules/strategy/`
- there is no standalone backend `strategy` module under `backend/src/main/java/.../module/strategy/`
- the router has no `/strategy/*` routes
- the top-level menu has no strategy entry
- the only existing strategy-related UI is the station-monitoring child component `StationStrategyView.vue`
- the current strategy docs describe a module that does not actually exist in `dev`

So this work is not a simple migration of existing code. It is the first real implementation of M04 on top of the newer repository architecture.

## 2. Goal

Implement a full M04 strategy management module that matches the project’s newer delivery pattern:

- frontend routes and pages are real, not placeholder docs
- backend routes are real and locally testable
- strategy data has an H2 development data foundation
- revenue simulation, KPI aggregation, batch actions, and comparisons are computed in backend services
- docs are detailed enough for handover and long-term maintenance

## 3. Scope

### In scope

- frontend `strategy` module with three views:
  - list
  - config
  - revenue
- router and menu integration
- strategy API bindings in `frontend/src/api/pvms.js`
- backend strategy controller/service/repository implementation
- H2 strategy schema and seed data
- backend tests for strategy controller
- strategy module docs and handover docs

### Out of scope

- production-grade auth, approval workflow, and audit trail
- real external electricity market integration
- real optimizer / dispatch engine
- cross-service eventing
- backend DTO refactor across the whole system

## 4. Design Principles

### 4.1 Reuse system facts, do not duplicate them blindly

M04 should not create a separate isolated world of station and production data.

It should reuse existing system facts where possible:

- `sa_station` for station and region hierarchy
- `sa_company` for company structure
- `sa_station_curve_15m` for output/load context
- `fc_prediction_series_15m` for prediction-aware strategy simulation context

### 4.2 Keep strategy metadata in H2 first

Development-mode strategy definitions, pricing periods, execution records, and seeded revenue history should be stored in dedicated H2 tables.

This makes local联调 predictable and allows future replacement with:

- real dispatch platform data
- external price feeds
- strategy execution systems
- optimizer outputs

### 4.3 Keep business aggregation in backend services

The frontend should not calculate:

- KPI rollups
- revenue totals
- confidence ranges
- batch simulation summaries
- comparison curves / revenue deltas

Those should remain in backend services so the computation boundary is stable and reusable.

### 4.4 Use frontend-friendly unified contracts

The earlier documented drift should not be recreated.

The backend will align to the more ergonomic strategy frontend contract:

- tree response uses `tree`
- single simulation returns `confidenceRange`
- batch simulation returns `results` and `totalRevenue`
- batch create request body uses `strategies`

This avoids shipping a fresh set of mismatched field names.

## 5. High-Level Architecture

## 5.1 Frontend structure

New module:

- `frontend/src/modules/strategy/pages/StrategyPage.vue`
- `frontend/src/modules/strategy/components/StrategyHero.vue`
- `frontend/src/modules/strategy/components/StrategyTabNav.vue`
- `frontend/src/modules/strategy/components/StrategyFilterBar.vue`
- `frontend/src/modules/strategy/components/views/StrategyListView.vue`
- `frontend/src/modules/strategy/components/views/StrategyConfigView.vue`
- `frontend/src/modules/strategy/components/views/StrategyRevenueView.vue`

Responsibilities:

- `StrategyPage.vue`
  owns tab routing, top-level meta loading, and per-view data orchestration
- `ListView`
  owns strategy querying and bulk actions display
- `ConfigView`
  owns tree selection, create form, single simulate, batch simulate, create/submit flow
- `RevenueView`
  owns strategy收益 summary/detail/compare display

## 5.2 Backend structure

New backend module:

- `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyDataService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategySimulationService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyRevenueService.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/...`

Service split:

- `StrategyDataService`
  metadata, tree, list, detail, state transitions
- `StrategySimulationService`
  single and batch simulation
- `StrategyRevenueService`
  KPI summary, revenue trend/detail, compare

This prevents one giant “do everything” service like the earlier mock style.

## 6. Data Model

Dedicated strategy tables will use `sg_*` prefix.

### 6.1 `sg_strategy`

Main strategy record.

Fields:

- `id`
- `station_id`
- `company_id`
- `name`
- `type`
- `status`
- `mode`
- `target_power_kw`
- `start_time`
- `end_time`
- `version_no`
- `remark`
- `created_at`
- `updated_at`

### 6.2 `sg_strategy_period`

Per-strategy time windows.

Fields:

- `strategy_id`
- `period_order`
- `start_slot`
- `end_slot`
- `action_type`
- `target_ratio`

### 6.3 `sg_execution_log`

Execution and lifecycle records.

Fields:

- `id`
- `strategy_id`
- `event_time`
- `action`
- `result`
- `deviation_rate_pct`
- `operator_name`

### 6.4 `sg_price_period`

Time-of-use electricity pricing.

Fields:

- `station_id`
- `period_order`
- `start_slot`
- `end_slot`
- `price_type`
- `price_cny_per_kwh`

### 6.5 `sg_revenue_daily`

Seeded daily revenue history for trend/detail.

Fields:

- `strategy_id`
- `biz_date`
- `estimated_revenue_cny`
- `actual_revenue_cny`
- `peak_saving_cny`
- `response_reward_cny`
- `penalty_cny`

### 6.6 `sg_strategy_snapshot`

Optional per-strategy seeded derived snapshot for list/kpi speed.

Fields:

- `strategy_id`
- `last_simulated_revenue_cny`
- `confidence_low_cny`
- `confidence_high_cny`
- `success_probability_pct`

## 7. Data Boundary

### 7.1 System fact data reused by M04

- company/station hierarchy from `sa_company` + `sa_station`
- load/output context from `sa_station_curve_15m`
- forecast-aware context from `fc_prediction_series_15m`

### 7.2 Strategy metadata maintained in H2

- strategy records
- execution records
- pricing periods
- seeded revenue history
- simulation snapshots

### 7.3 Backend-computed outputs

- KPI cards
- list summary fields
- single simulation result
- batch simulation aggregate
- revenue trend summary
- compare output

## 8. API Contract

### 8.1 Read APIs

- `GET /api/pvms/strategy/meta`
- `GET /api/pvms/strategy/tree`
- `GET /api/pvms/strategy/kpi`
- `GET /api/pvms/strategy/list`
- `GET /api/pvms/strategy/detail`
- `GET /api/pvms/strategy/electricity-price`
- `GET /api/pvms/strategy/revenue/summary`
- `GET /api/pvms/strategy/revenue/detail`
- `GET /api/pvms/strategy/compare`

### 8.2 Write APIs

- `POST /api/pvms/strategy/create`
- `POST /api/pvms/strategy/batch-create`
- `POST /api/pvms/strategy/submit`
- `POST /api/pvms/strategy/terminate`
- `POST /api/pvms/strategy/batch-submit`
- `POST /api/pvms/strategy/batch-delete`
- `POST /api/pvms/strategy/simulate`
- `POST /api/pvms/strategy/batch-simulate`

### 8.3 Key contract decisions

- `tree` is the root response field for hierarchy
- batch payload uses `{ strategies: [...] }`
- single simulate returns:
  - `estimatedRevenue`
  - `confidenceRange`
  - `breakdown`
- batch simulate returns:
  - `results`
  - `totalRevenue`

## 9. Backend Computation Logic

### 9.1 Strategy KPI

Computed from `sg_strategy`:

- active count
- pending count
- executing count
- simulated revenue today
- success rate

### 9.2 Single simulation

Inputs:

- selected strategy form values
- station context
- station load/output baseline
- time-of-use price periods
- forecast curve context

Outputs:

- `estimatedRevenue`
- `confidenceRange`
- `breakdown`

Revenue logic is seeded-business logic, not market-grade optimization:

- peak/flat/valley actions derive savings/reward weights
- station capacity and target power clamp strategy effect
- forecast uncertainty expands confidence interval

### 9.3 Batch simulation

Inputs:

- multiple strategy definitions

Outputs:

- one per-strategy result list
- total revenue rollup

### 9.4 Revenue summary/detail

Summary:

- KPI cards
- trend
- type breakdown

Detail:

- tabular list with date/type/station/revenue fields

Compare:

- per-strategy summary and delta information

## 10. Frontend View Behavior

### 10.1 List view

Loads:

- `kpi`
- `list`
- `detail` when user inspects one strategy

Supports:

- filter by status/type/station
- submit
- terminate
- batch submit
- batch delete

### 10.2 Config view

Loads:

- `tree`
- `electricity-price`

Supports:

- single station create
- batch create
- single simulation
- batch simulation
- create then submit

### 10.3 Revenue view

Loads:

- `revenue/summary`
- `revenue/detail`
- `compare`

Supports:

- trend chart
- detail table
- strategy comparison

## 11. Testing Strategy

### 11.1 Backend

Add `StrategyControllerTest.java` covering:

- meta
- tree
- kpi
- list
- detail
- electricity-price
- create
- batch-create
- submit
- terminate
- batch-submit
- batch-delete
- simulate
- batch-simulate
- revenue summary
- revenue detail
- compare

TDD order:

1. write failing test
2. run to observe expected failure
3. implement minimal code
4. rerun targeted test
5. rerun full backend test suite

### 11.2 Frontend

Minimum:

- route renders
- API requests wired in `pvms.js`
- build passes in the existing frontend workspace
- manual proxy verification from `6618`

## 12. Documentation Deliverables

Update:

- `docs/modules/backend/strategy.md`
- `docs/modules/frontend/strategy.md`
- `docs/handover/05_后端维护手册.md`
- `docs/handover/08_测试_排障_交接清单.md`
- `docs/handover/README.md`
- `docs/README.md`

Add:

- `docs/handover/13_M04_策略管理_H2数据与计算说明.md`
- `docs/superpowers/plans/2026-03-30-strategy-h2-migration.md`

## 13. Risks

### Risk 1

M04 does not currently exist in code, only in docs.  
This means the migration includes first-time module creation, which is larger than M03.

### Risk 2

If frontend and backend are built independently without a single contract target, the old field drift will reappear immediately.

### Risk 3

If simulation logic grows unchecked inside one service or one Vue file, maintainability will collapse quickly.

## 14. Recommendation

Implement M04 as a full module in one isolated branch, but build it in four stages:

1. H2 schema + repository + controller tests
2. backend read/write APIs
3. frontend module and route integration
4. docs, merge, restart, and verification

This is the smallest approach that still produces a real maintainable module rather than another documented-but-missing feature.
