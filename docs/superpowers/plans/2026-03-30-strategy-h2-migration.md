# Strategy Management H2 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the missing M04 strategy management module with real frontend routes, H2-backed backend APIs, seeded strategy data, backend simulation/revenue calculations, and handover-grade documentation.

**Architecture:** Create a new standalone `strategy` frontend module and a new backend `strategy` module. Reuse existing station/company/forecast facts from `sa_*` and `fc_*`, store strategy metadata in new `sg_*` tables, and compute KPI/simulation/revenue outputs in backend services.

**Tech Stack:** Vue 2, Vue Router, Vuex, Spring Boot 3, JdbcClient, H2, JUnit + MockMvc, PowerShell, Maven

---

## File Structure

### Frontend

- Create: `frontend/src/modules/strategy/pages/StrategyPage.vue`
- Create: `frontend/src/modules/strategy/components/StrategyHero.vue`
- Create: `frontend/src/modules/strategy/components/StrategyTabNav.vue`
- Create: `frontend/src/modules/strategy/components/StrategyFilterBar.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyListView.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyConfigView.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyRevenueView.vue`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/settings.js`
- Modify: `frontend/src/api/pvms.js`

### Backend

- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyDataService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategySimulationService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyRevenueService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyMetaRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyTreeRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRecordRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyPriceRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRevenueRepository.java`
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`
- Create: `backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java`

### Docs

- Modify: `docs/modules/backend/strategy.md`
- Modify: `docs/modules/frontend/strategy.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`
- Modify: `docs/handover/README.md`
- Modify: `docs/README.md`
- Create: `docs/handover/13_M04_策略管理_H2数据与计算说明.md`

## Task 1: Write M04 Controller Contract Tests

**Files:**
- Create: `backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java`

- [ ] **Step 1: Write the failing test**

Create MockMvc tests for:

- `GET /api/pvms/strategy/meta`
- `GET /api/pvms/strategy/tree`
- `GET /api/pvms/strategy/kpi`
- `GET /api/pvms/strategy/list`
- `GET /api/pvms/strategy/detail?id=...`
- `GET /api/pvms/strategy/electricity-price?stationId=...`
- `POST /api/pvms/strategy/create`
- `POST /api/pvms/strategy/batch-create`
- `POST /api/pvms/strategy/submit?id=...`
- `POST /api/pvms/strategy/terminate?id=...`
- `POST /api/pvms/strategy/batch-submit`
- `POST /api/pvms/strategy/batch-delete`
- `POST /api/pvms/strategy/simulate`
- `POST /api/pvms/strategy/batch-simulate`
- `GET /api/pvms/strategy/revenue/summary`
- `GET /api/pvms/strategy/revenue/detail`
- `GET /api/pvms/strategy/compare?ids=...`

Assert the key contract fields:

- `tree`
- `confidenceRange`
- `results`
- `totalRevenue`
- request body name `strategies`

- [ ] **Step 2: Run test to verify it fails**

Run: `mvn -Dtest=StrategyControllerTest test`

Expected: fail with missing route / controller behavior

- [ ] **Step 3: Commit**

```bash
git add backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java
git commit -m "test: add failing strategy controller contract tests"
```

## Task 2: Add Strategy H2 Schema and Seed Data

**Files:**
- Modify: `backend/src/main/resources/schema.sql`
- Modify: `backend/src/main/resources/data.sql`

- [ ] **Step 1: Add failing schema-driven assumptions to test if needed**

If Task 1 lacks enough coverage for seed counts, add assertions for:

- strategy count
- tree node count
- region/station coverage
- price periods

- [ ] **Step 2: Add schema tables**

Append `sg_*` tables:

- `sg_strategy`
- `sg_strategy_period`
- `sg_execution_log`
- `sg_price_period`
- `sg_revenue_daily`
- `sg_strategy_snapshot`

Use foreign keys into `sa_company` and `sa_station`.

- [ ] **Step 3: Add seed data**

Populate:

- 20-30 seeded strategies across regions
- per-strategy periods
- execution logs
- station price periods
- daily revenue history
- simulation snapshots

Reuse existing `sa_*` / `fc_*` station ids.

- [ ] **Step 4: Run targeted test**

Run: `mvn -Dtest=StrategyControllerTest test`

Expected: still fail, but no schema/init failure

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/resources/schema.sql backend/src/main/resources/data.sql
git commit -m "feat: add strategy H2 schema and seed data"
```

## Task 3: Implement Strategy Read APIs

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyMetaRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyTreeRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRecordRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyPriceRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRevenueRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyDataService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyRevenueService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java`

- [ ] **Step 1: Implement repository reads**

Add focused query classes for:

- meta
- hierarchy tree inputs
- strategy list/detail
- electricity price
- revenue history

- [ ] **Step 2: Implement read service methods**

Add methods for:

- `getMeta`
- `getTree`
- `getKpi`
- `getList`
- `getDetail`
- `getElectricityPrice`
- `getRevenueSummary`
- `getRevenueDetail`
- `compare`

- [ ] **Step 3: Wire controller GET endpoints**

Return `ApiResponse.success(...)` and align field names exactly with the approved contract.

- [ ] **Step 4: Run targeted test**

Run: `mvn -Dtest=StrategyControllerTest test`

Expected: read endpoints pass, write/simulate endpoints still fail

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/techstar/pvms/backend/module/strategy
git commit -m "feat: implement strategy read APIs"
```

## Task 4: Implement Strategy Write and State Transition APIs

**Files:**
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategyDataService.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRecordRepository.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java`

- [ ] **Step 1: Implement create and batch-create**

Support request bodies:

- single strategy object
- `{ strategies: [...] }`

Persist into `sg_strategy` and `sg_strategy_period`.

- [ ] **Step 2: Implement submit / terminate / batch-submit / batch-delete**

Update status and append execution log rows.

- [ ] **Step 3: Re-run targeted tests**

Run: `mvn -Dtest=StrategyControllerTest test`

Expected: write endpoint tests pass except simulate paths

- [ ] **Step 4: Commit**

```bash
git add backend/src/main/java/cn/techstar/pvms/backend/module/strategy
git commit -m "feat: add strategy write and state transition APIs"
```

## Task 5: Implement Strategy Simulation APIs

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/service/StrategySimulationService.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyController.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyPriceRepository.java`
- Modify: `backend/src/main/java/cn/techstar/pvms/backend/module/strategy/repository/StrategyRecordRepository.java`

- [ ] **Step 1: Implement single simulation**

Use:

- station capacity
- seeded price periods
- output/load context
- forecast context

Return:

- `estimatedRevenue`
- `confidenceRange`
- `breakdown`

- [ ] **Step 2: Implement batch simulation**

Accept `{ strategies: [...] }` and return:

- `results`
- `totalRevenue`

- [ ] **Step 3: Run targeted test**

Run: `mvn -Dtest=StrategyControllerTest test`

Expected: all controller tests pass

- [ ] **Step 4: Run full backend test suite**

Run: `mvn test`

Expected: all backend tests pass

- [ ] **Step 5: Commit**

```bash
git add backend/src/main/java/cn/techstar/pvms/backend/module/strategy backend/src/test/java/cn/techstar/pvms/backend/module/strategy/controller/StrategyControllerTest.java
git commit -m "feat: add strategy simulation and revenue logic"
```

## Task 6: Add Frontend Strategy API Bindings and Routes

**Files:**
- Modify: `frontend/src/api/pvms.js`
- Modify: `frontend/src/router/index.js`
- Modify: `frontend/src/settings.js`

- [ ] **Step 1: Add strategy API functions**

Add:

- `fetchStrategyMeta`
- `fetchStrategyTree`
- `fetchStrategyKpi`
- `fetchStrategyList`
- `fetchStrategyDetail`
- `fetchStrategyElectricityPrice`
- `createStrategy`
- `batchCreateStrategy`
- `submitStrategy`
- `terminateStrategy`
- `batchSubmitStrategy`
- `batchDeleteStrategy`
- `simulateStrategy`
- `batchSimulateStrategy`
- `fetchStrategyRevenueSummary`
- `fetchStrategyRevenueDetail`
- `fetchStrategyCompare`

- [ ] **Step 2: Add router entries**

Add:

- `/strategy/list`
- `/strategy/config`
- `/strategy/revenue`

- [ ] **Step 3: Add menu entry**

Expose strategy menu item in `settings.js`.

- [ ] **Step 4: Commit**

```bash
git add frontend/src/api/pvms.js frontend/src/router/index.js frontend/src/settings.js
git commit -m "feat: wire strategy routes and API bindings"
```

## Task 7: Build Frontend Strategy Module

**Files:**
- Create: `frontend/src/modules/strategy/pages/StrategyPage.vue`
- Create: `frontend/src/modules/strategy/components/StrategyHero.vue`
- Create: `frontend/src/modules/strategy/components/StrategyTabNav.vue`
- Create: `frontend/src/modules/strategy/components/StrategyFilterBar.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyListView.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyConfigView.vue`
- Create: `frontend/src/modules/strategy/components/views/StrategyRevenueView.vue`

- [ ] **Step 1: Build page shell**

Mirror the forecast-style orchestration:

- meta load
- tab route switching
- per-view data loading

- [ ] **Step 2: Implement list view**

Display:

- KPI cards
- strategy list
- detail drawer/dialog
- batch actions

- [ ] **Step 3: Implement config view**

Display:

- tree
- form
- single simulate result
- batch simulate result
- create and submit actions

- [ ] **Step 4: Implement revenue view**

Display:

- KPI cards
- trend chart
- detail table
- compare panel

- [ ] **Step 5: Manually verify via running frontend**

At minimum verify:

- route loads
- menu navigation works
- requests hit `/api/pvms/strategy/*`

- [ ] **Step 6: Commit**

```bash
git add frontend/src/modules/strategy
git commit -m "feat: add strategy frontend module"
```

## Task 8: Update Handover and Module Docs

**Files:**
- Modify: `docs/modules/backend/strategy.md`
- Modify: `docs/modules/frontend/strategy.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`
- Modify: `docs/handover/README.md`
- Modify: `docs/README.md`
- Create: `docs/handover/13_M04_策略管理_H2数据与计算说明.md`

- [ ] **Step 1: Rewrite backend strategy doc**

Explain:

- actual code locations
- `sg_*` tables
- reused `sa_*` / `fc_*` facts
- backend computation boundaries

- [ ] **Step 2: Rewrite frontend strategy doc**

Explain:

- route structure
- component roles
- data flow per view
- contract assumptions

- [ ] **Step 3: Add M04 handover doc**

Explain:

- what is metadata
- what is backend computed
- how to replace H2 with real systems later

- [ ] **Step 4: Update indexes**

Add M04 references into handover and docs entrypoints.

- [ ] **Step 5: Commit**

```bash
git add docs
git commit -m "docs: add strategy module handover documentation"
```

## Task 9: Final Verification, Merge, and Restart

**Files:**
- Verify only; no planned source changes unless verification finds issues

- [ ] **Step 1: Run backend full verification**

Run: `mvn test`

Expected: pass

- [ ] **Step 2: Run frontend verification in the main frontend workspace**

Run the existing frontend build command from the workspace that has `node_modules`.

Expected: build success or clearly documented limitation

- [ ] **Step 3: Verify live endpoints**

Check:

- `http://127.0.0.1:8091/api/system/health`
- `http://127.0.0.1:8091/api/pvms/strategy/meta`
- `http://127.0.0.1:8091/api/pvms/strategy/tree`
- `http://127.0.0.1:8091/api/pvms/strategy/simulate`
- `http://127.0.0.1:6618/api/pvms/strategy/meta`

- [ ] **Step 4: Merge into `dev`**

Merge the isolated branch into the active `dev` worktree.

- [ ] **Step 5: Push `dev`**

Run:

```bash
git push origin dev
```

- [ ] **Step 6: Restart backend**

Stop the current `8091` backend and start the merged `dev` backend.

- [ ] **Step 7: Capture runtime log paths**

Record:

- backend stdout log
- backend stderr log

- [ ] **Step 8: Final summary**

Report:

- what changed
- what passed
- what was not verified
