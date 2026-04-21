# M02 Production Monitor Redesign Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将模块 2 从“资源单元列表 + 详情页”重构成“生产监控层单模块 + 横向视图导航”，并落地聚合商视角下的资源总览、出力分析、调度执行、气象研判四个视图。

**Architecture:** 前端改成一个统一的 `production-monitor` 模块壳子，顶部横向导航切换视图，统一共享资源单元和时间筛选状态；后端接口按视图拆分而不是按详情页拆分。资源单元下的电站在地理位置上接近，因此天气趋势与总出力趋势共享一套基础曲线，成员电站仅按容量和状态系数分摊不同的出力比例；若站点距离过远，应视为另一个资源单元，不在同一数据集内建模。

**Tech Stack:** Vue 2, Vue Router, Element UI, hs-dtk-ui, ECharts 5, Spring Boot, MockMvc, Playwright

---

## File Map

### Frontend files to create

- `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- `frontend/src/modules/production-monitor/components/ProductionMonitorHero.vue`
- `frontend/src/modules/production-monitor/components/ProductionMonitorTabNav.vue`
- `frontend/src/modules/production-monitor/components/ProductionMonitorFilterBar.vue`
- `frontend/src/modules/production-monitor/components/views/ProductionOverviewView.vue`
- `frontend/src/modules/production-monitor/components/views/ProductionOutputView.vue`
- `frontend/src/modules/production-monitor/components/views/ProductionDispatchView.vue`
- `frontend/src/modules/production-monitor/components/views/ProductionWeatherView.vue`
- `frontend/src/modules/production-monitor/components/shared/ResourceUnitStatusBar.vue`
- `frontend/src/modules/production-monitor/components/shared/MemberStationSummaryTable.vue`
- `frontend/src/modules/production-monitor/components/shared/WeatherBriefCard.vue`
- `frontend/src/modules/production-monitor/components/shared/AlarmBriefCard.vue`
- `frontend/src/modules/production-monitor/components/shared/PowerCurvePanel.vue`
- `frontend/src/modules/production-monitor/components/shared/WeatherTrendPanel.vue`
- `frontend/src/modules/production-monitor/components/shared/DispatchTrendPanel.vue`

### Frontend files to modify

- `frontend/src/router/index.js`
- `frontend/src/settings.js`
- `frontend/src/api/pvms.js`
- `frontend/src/shared/mock/index.js`
- `frontend/src/shared/mock/station-monitoring.js`
- `frontend/tests/playwright/specs/station-monitoring.spec.js`
- `frontend/tests/playwright/specs/layout-smoke.spec.js`
- `frontend/docs/modules/station-monitoring.md`
- `doc/M02_生产监控层_前端设计.md`

### Frontend files to delete or deprecate from route path

- `frontend/src/modules/station-monitoring/pages/StationListPage.vue`
- `frontend/src/modules/station-monitoring/pages/StationOverviewPage.vue`

说明：
- 若删除风险较高，可先保留文件，但必须从路由和菜单主链路中移除。

### Backend files to create

- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorMockService.java`
- `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`
- `backend/docs/modules/production-monitor.md`

### Backend files to modify

- `backend/src/main/java/cn/techstar/pvms/backend/common/ApiResponse.java` only if response helper missing new needs
- `backend/src/test/java/cn/techstar/pvms/backend/module/stations/controller/StationControllerTest.java`

### Backend files to delete or deprecate

- `backend/src/main/java/cn/techstar/pvms/backend/module/stations/controller/StationController.java`
- `backend/src/main/java/cn/techstar/pvms/backend/module/stations/service/StationMockService.java`

说明：
- 可先保留旧类，待新模块完全通过后再删除；但旧接口不得继续作为模块 2 主链路。

---

### Task 1: Lock the module shell and routing

**Files:**
- Create: `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- Create: `frontend/src/modules/production-monitor/components/ProductionMonitorHero.vue`
- Create: `frontend/src/modules/production-monitor/components/ProductionMonitorTabNav.vue`
- Create: `frontend/src/modules/production-monitor/components/ProductionMonitorFilterBar.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/settings.js`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write the failing Playwright test for the new shell**

Add assertions for:
- route `/production-monitor/overview`
- left nav active state
- horizontal tab bar with 4 tabs
- shared filter bar visible

- [ ] **Step 2: Run the Playwright spec to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because `/production-monitor/*` route and tab shell do not exist.

- [ ] **Step 3: Implement the minimal module shell**

Build a new page shell that:
- renders hero, tab nav, and filter bar
- derives active view from route
- exposes shared query state:
  - `resourceUnitId`
  - `region`
  - `city`
  - `date`
  - `granularity`

- [ ] **Step 4: Wire router and menu**

Add routes:
- `/production-monitor/overview`
- `/production-monitor/output`
- `/production-monitor/dispatch`
- `/production-monitor/weather`

Update sidebar settings so module 2 appears as a single menu item below module 1.

- [ ] **Step 5: Run the Playwright spec again**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for shell presence assertions, later content assertions still failing.

---

### Task 2: Replace the old mock contract with the new shared resource-unit model

**Files:**
- Modify: `frontend/src/shared/mock/station-monitoring.js`
- Modify: `frontend/src/shared/mock/index.js`
- Modify: `frontend/src/api/pvms.js`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write a failing frontend behavior test for meta + overview boot**

Add a test that verifies:
- default resource unit loads
- overview tab displays shared weather summary
- member stations show different output ratios under the same weather context

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because new mock endpoints and payloads do not exist.

- [ ] **Step 3: Rewrite the mock data model**

Introduce a resource-unit dataset where:
- each resource unit has one shared weather profile
- each resource unit has one shared irradiance / weather trend
- member stations inherit the same weather context
- member station output is derived from:
  - station weight
  - station availability/status
  - shared resource-unit trend

Do not model distant stations inside the same unit.

- [ ] **Step 4: Add new frontend API methods**

Implement:
- `fetchProductionMonitorMeta`
- `fetchProductionMonitorOverview`
- `fetchProductionMonitorOutput`
- `fetchProductionMonitorDispatch`
- `fetchProductionMonitorWeather`

- [ ] **Step 5: Re-run the test**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for the new mock contract assertions.

---

### Task 3: Build the resource overview view

**Files:**
- Create: `frontend/src/modules/production-monitor/components/views/ProductionOverviewView.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/ResourceUnitStatusBar.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/MemberStationSummaryTable.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/WeatherBriefCard.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/AlarmBriefCard.vue`
- Modify: `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write the failing Playwright assertions for overview layout**

Assert:
- 6 KPI cards
- member station summary block
- weather brief card
- alarm brief card
- bottom summary table

- [ ] **Step 2: Run the spec to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because overview content area is not implemented.

- [ ] **Step 3: Implement the overview view**

Keep content focused on:
- current health
- dispatchable capacity
- online rate
- forecast accuracy
- same-weather member station summary

The weather card must explicitly mention photovoltaic indicators:
- irradiance
- cloudiness
- temperature
- wind speed
- conclusion

- [ ] **Step 4: Run the spec again**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for overview layout checks.

---

### Task 4: Build the output analysis view

**Files:**
- Create: `frontend/src/modules/production-monitor/components/views/ProductionOutputView.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/PowerCurvePanel.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/WeatherTrendPanel.vue`
- Modify: `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write the failing Playwright assertions for output analysis**

Assert:
- output tab switches content
- main power curve exists
- weather trend panel exists
- ranking / contribution area exists
- time-granularity control affects the view state

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because output analysis view is missing.

- [ ] **Step 3: Implement the output analysis view**

Requirements:
- actual vs forecast vs baseline
- shared weather trend series
- member station contribution ranking based on station weight
- table rows aligned to selected granularity

- [ ] **Step 4: Run the test again**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for output analysis assertions.

---

### Task 5: Build the dispatch execution view

**Files:**
- Create: `frontend/src/modules/production-monitor/components/views/ProductionDispatchView.vue`
- Create: `frontend/src/modules/production-monitor/components/shared/DispatchTrendPanel.vue`
- Modify: `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write the failing Playwright assertions for dispatch view**

Assert:
- dispatch summary cards
- trend chart
- response metrics / risk hints
- dispatch records table

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because dispatch execution content is missing.

- [ ] **Step 3: Implement the dispatch view**

Model:
- instruction counts
- success rate
- response latency
- risk hints tied to weather and output deviation

- [ ] **Step 4: Run the test again**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for dispatch view assertions.

---

### Task 6: Build the weather assessment view

**Files:**
- Create: `frontend/src/modules/production-monitor/components/views/ProductionWeatherView.vue`
- Modify: `frontend/src/modules/production-monitor/pages/ProductionMonitorPage.vue`
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`

- [ ] **Step 1: Write the failing Playwright assertions for weather assessment**

Assert:
- weather tab content loads
- current weather cards render
- 72h weather trend renders
- photovoltaic impact table renders

- [ ] **Step 2: Run the test to verify it fails**

Run: `npm test -- station-monitoring.spec.js`  
Expected: FAIL because weather assessment view is missing.

- [ ] **Step 3: Implement the weather view**

Keep focus on photovoltaic interpretation:
- current weather
- irradiance trend
- cloudiness markers
- dispatch suggestion

- [ ] **Step 4: Run the test again**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS for weather assessment assertions.

---

### Task 7: Replace backend station endpoints with production-monitor endpoints

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorController.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/productionmonitor/service/ProductionMonitorMockService.java`
- Create: `backend/src/test/java/cn/techstar/pvms/backend/module/productionmonitor/controller/ProductionMonitorControllerTest.java`
- Modify: `backend/docs/modules/production-monitor.md`

- [ ] **Step 1: Write failing MockMvc tests for the new API surface**

Cover:
- `GET /api/pvms/production-monitor/meta`
- `GET /api/pvms/production-monitor/overview`
- `GET /api/pvms/production-monitor/output`
- `GET /api/pvms/production-monitor/dispatch`
- `GET /api/pvms/production-monitor/weather`

- [ ] **Step 2: Run Maven tests to verify failure**

Run: `mvn test -Dtest=ProductionMonitorControllerTest`  
Expected: FAIL because controller and service do not exist.

- [ ] **Step 3: Implement the minimal backend service and controller**

Rules to encode:
- same resource unit => shared weather profile and trend
- member station outputs are proportional derivations, not independent weather systems
- distant stations must be different resource units

- [ ] **Step 4: Run the targeted backend test**

Run: `mvn test -Dtest=ProductionMonitorControllerTest`  
Expected: PASS

- [ ] **Step 5: Run the full backend test suite**

Run: `mvn test`  
Expected: PASS

---

### Task 8: Remove old module-2 route semantics and update docs

**Files:**
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/docs/modules/station-monitoring.md`
- Modify: `doc/M02_生产监控层_前端设计.md`
- Create: `doc/finished/M02_模块2_交付总览.md`
- Create: `doc/finished/M02_前端维护说明.md`
- Create: `doc/finished/M02_后端维护说明.md`
- Modify: `backend/docs/modules/production-monitor.md`

- [ ] **Step 1: Write down the deprecation points before deleting old semantics**

Record:
- old `/station` and `/station/:id`
- new `/production-monitor/*`
- old API set vs new API set

- [ ] **Step 2: Update module docs**

Must include:
- route structure
- filter state
- tab semantics
- API params and responses
- weather/output shared-trend rule

- [ ] **Step 3: Optionally remove old files or leave them as dead code only after verifying no import remains**

If removing files:
- ensure router and tests no longer reference them
- ensure build passes

- [ ] **Step 4: Run frontend lint and build**

Run:
- `npm run lint -- --no-fix`
- `npm run build`

Expected: PASS

---

### Task 9: Final UI verification

**Files:**
- Test: `frontend/tests/playwright/specs/station-monitoring.spec.js`
- Test: `frontend/tests/playwright/specs/layout-smoke.spec.js`

- [ ] **Step 1: Run module-specific Playwright tests**

Run: `npm test -- station-monitoring.spec.js`  
Expected: PASS

- [ ] **Step 2: Run layout smoke tests**

Run: `npm test -- layout-smoke.spec.js`  
Expected: PASS

- [ ] **Step 3: Check for layout regressions manually via screenshot review**

Review:
- top tab bar hierarchy
- filter bar spacing
- chart/table balance
- no large blank areas
- no route/header mismatch

- [ ] **Step 4: Summarize residual risks**

Expected residuals:
- front-end bundle size warning may remain
- backend weather still mock-backed until real采集接入

