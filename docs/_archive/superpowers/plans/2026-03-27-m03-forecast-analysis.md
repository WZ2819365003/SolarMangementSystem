# M03 预测与分析 Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build the M03 Forecast & Analysis module with 3 tab views (预测总览, 可调能力分析, 精度评估), mock data, and ECharts visualizations.

**Architecture:** Tab-based page following the M02 ProductionMonitorPage pattern — a single `ForecastPage.vue` routes between 3 views via `viewKey` meta, with shared filter bar and mock data layer. Each view uses KPI cards + ECharts charts + el-table tables in dark theme.

**Tech Stack:** Vue 2, Element UI 2.15, ECharts 5, LESS, `--pvms-*` design tokens

---

## File Structure

```
src/modules/forecast/
├── pages/
│   └── ForecastPage.vue                    # Main page: tabs + filter + dynamic view
└── components/
    ├── ForecastHero.vue                     # Page header with title + summary stats
    ├── ForecastTabNav.vue                   # 3-tab navigation
    ├── ForecastFilterBar.vue                # Region / station / date range / forecast type
    └── views/
        ├── ForecastOverviewView.vue         # Tab 1: KPIs + comparison chart + heatmap + table
        ├── ForecastAdjustableView.vue       # Tab 2: KPIs + capacity curve + VPP timeline + table
        └── ForecastAccuracyView.vue         # Tab 3: KPIs + trend + distribution + ranking

src/shared/mock/
└── forecast.js                              # All mock data + handlers

src/api/pvms.js                              # Add 6 fetch functions
src/shared/mock/data.js                      # Register forecast handlers
src/router/index.js                          # Add 3 routes
src/settings.js                              # Add menu item
```

**Design decisions:**
- Charts are inline in view components (each view is 1 file with ECharts setup) — the M02 pattern puts charts directly in views, and we follow that. If M02's station forecast tab needs reuse, it can import the view or share mock data.
- Mock data in a single `forecast.js` — follows `station-archive.js` pattern
- Tab nav reuses the exact pattern from `ProductionMonitorTabNav.vue`
- Each view receives `viewData` + `query` + `loading` props from parent page
- **Units**: All power values use **MW** (not kW) to match spec and VPP operator perspective
- **Chart min-height**: 520px per spec requirement

---

## Task 1: Mock Data Layer

**Files:**
- Create: `src/shared/mock/forecast.js`
- Modify: `src/shared/mock/data.js` (add import + spread handlers)

- [ ] **Step 1: Create forecast mock data file**

Create `src/shared/mock/forecast.js` with:
- `buildSuccess(data)` helper (same as station-archive)
- `seededValue(seed, min, max)` for deterministic random
- `stations` reference data (16 stations from station-archive)
- `buildForecastMeta()` → returns region options + station list
- `buildForecastOverview(params)` → returns KPIs (日前精度, 超短期精度, 当日偏差, 合格率) + comparison chart data (96 points: 日前/超短期/实际 3 series) + deviation heatmap (stations × 24h) + station prediction table
- `buildForecastAdjustable(params)` → returns KPIs (总可调/可上调/可下调/24h最大) + 24h capacity curve (area: upper/lower bounds + predicted) + station adjustable table
- `buildForecastAccuracy(params)` → returns KPIs (MAE/RMSE/合格率/月均精度) + 30-day accuracy trend + deviation distribution histogram (10 bins) + station ranking + monthly comparison (6 months)
- Export `forecastMockHandlers` object with 6 routes

Chart data should use physically plausible PV curves: bell-shaped daytime output (peak ~12:00), zero at night, prediction slightly offset from actual.

```javascript
// Key mock handler routes:
'/pvms/forecast/meta': function(options) { ... }
'/pvms/forecast/overview': function(options) { ... }
'/pvms/forecast/adjustable': function(options) { ... }
'/pvms/forecast/accuracy': function(options) { ... }
'/pvms/forecast/comparison': function(options) { ... }
'/pvms/forecast/deviation-heatmap': function(options) { ... }
```

- [ ] **Step 2: Register in data.js**

In `src/shared/mock/data.js`, add:
```javascript
import { forecastMockHandlers } from '@/shared/mock/forecast'
// Spread into mockHandlers:
export const mockHandlers = {
  ...existingHandlers,
  ...forecastMockHandlers
}
```

- [ ] **Step 3: Verify mock loads without error**

Run dev server, check console for import errors.

---

## Task 2: API Functions + Routes + Menu

**Files:**
- Modify: `src/api/pvms.js` (add 6 functions)
- Modify: `src/router/index.js` (add 3 routes)
- Modify: `src/settings.js` (add menu item)

- [ ] **Step 1: Add API functions**

Add to `src/api/pvms.js`:
```javascript
// M03 Forecast
export function fetchForecastMeta() {
  return request('/pvms/forecast/meta', { method: 'get' })
}
export function fetchForecastOverview(params) {
  return request('/pvms/forecast/overview', { method: 'get', params })
}
export function fetchForecastAdjustable(params) {
  return request('/pvms/forecast/adjustable', { method: 'get', params })
}
export function fetchForecastAccuracy(params) {
  return request('/pvms/forecast/accuracy', { method: 'get', params })
}
export function fetchForecastComparison(params) {
  return request('/pvms/forecast/comparison', { method: 'get', params })
}
export function fetchForecastDeviationHeatmap(params) {
  return request('/pvms/forecast/deviation-heatmap', { method: 'get', params })
}
```

- [ ] **Step 2: Add routes**

Add 3 routes to `src/router/index.js` (import ForecastPage lazily or eagerly):
```javascript
import ForecastPage from '@/modules/forecast/pages/ForecastPage.vue'

// Inside children array:
{
  path: '/forecast/overview',
  name: 'forecast-overview',
  component: ForecastPage,
  meta: { title: '预测总览', section: '预测与分析', activeMenu: '/forecast/overview', viewKey: 'overview' }
},
{
  path: '/forecast/adjustable',
  name: 'forecast-adjustable',
  component: ForecastPage,
  meta: { title: '可调能力分析', section: '预测与分析', activeMenu: '/forecast/overview', viewKey: 'adjustable' }
},
{
  path: '/forecast/accuracy',
  name: 'forecast-accuracy',
  component: ForecastPage,
  meta: { title: '精度评估', section: '预测与分析', activeMenu: '/forecast/overview', viewKey: 'accuracy' }
}
```

- [ ] **Step 3: Add menu entry**

In `src/settings.js`, add to menus array (after production-monitor):
```javascript
{
  key: 'forecast',
  label: '预测与分析',
  path: '/forecast/overview',
  icon: 'el-icon-s-data',
  description: '光伏功率预测、可调能力分析与精度评估'
}
```

- [ ] **Step 4: Verify routes register**

Navigate to `/forecast/overview` in browser — should show blank page (ForecastPage not yet created), no console errors for route/mock.

---

## Task 3: ForecastPage + TabNav + FilterBar + Hero

**Files:**
- Create: `src/modules/forecast/pages/ForecastPage.vue`
- Create: `src/modules/forecast/components/ForecastHero.vue`
- Create: `src/modules/forecast/components/ForecastTabNav.vue`
- Create: `src/modules/forecast/components/ForecastFilterBar.vue`

- [ ] **Step 1: Create ForecastTabNav**

Clone pattern from `ProductionMonitorTabNav.vue`. Props: `tabs` array, `activeKey` string. Emits `@change(key)`. 3 tab buttons with same dark theme gradient styling.

- [ ] **Step 2: Create ForecastFilterBar**

`el-form` inline with:
- Region select (区域)
- Station select (电站)
- Date range picker (`el-date-picker` type="daterange")
- Forecast type radio group (日前预测 / 超短期预测)
- Search + Refresh buttons

Props: `query`, `regionOptions`, `stationOptions`. Emits `@search(query)`, `@refresh`.

- [ ] **Step 3: Create ForecastHero**

Simple header component using `AppPageHero` (if exists) or custom div. Shows: section title "预测与分析", current view label, summary description. Props: `viewKey`.

- [ ] **Step 4: Create ForecastPage**

Main page component following `ProductionMonitorPage` pattern:
```javascript
data() {
  return {
    tabs: [
      { key: 'overview', label: '预测总览', path: '/forecast/overview' },
      { key: 'adjustable', label: '可调能力分析', path: '/forecast/adjustable' },
      { key: 'accuracy', label: '精度评估', path: '/forecast/accuracy' }
    ],
    meta: { regionOptions: [], stationOptions: [] },
    query: { region: '', stationId: '', dateRange: [], forecastType: 'day-ahead' },
    viewData: {},
    loadingView: false
  }
}
```

Computed: `currentViewKey` from `$route.meta.viewKey`, `currentViewComponent` mapping to view names.

Lifecycle: `created()` → `fetchForecastMeta()` → `loadCurrentView()`.

Template:
```
ForecastHero
ForecastTabNav
ForecastFilterBar
<component :is="currentViewComponent" :view-data="viewData" :query="query" :loading="loadingView" />
```

Use `pv-page` CSS class for consistent page styling.

- [ ] **Step 5: Verify page renders**

Navigate to `/forecast/overview` → should see hero + tabs + filter bar + empty content area. Tabs should switch routes. Filter should be interactive.

---

## Task 4: ForecastOverviewView (预测总览)

**Files:**
- Create: `src/modules/forecast/components/views/ForecastOverviewView.vue`

- [ ] **Step 1: Create view component**

Template structure:
```
<div class="forecast-overview-view">
  <!-- KPI Cards -->
  <section class="forecast-overview-view__kpis">
    <app-metric-card v-for="kpi in kpis" ... />
  </section>

  <!-- Comparison Chart: 日前 vs 超短期 vs 实际 -->
  <app-section-card title="预测对比曲线">
    <div ref="comparisonChart" class="forecast-overview-view__chart" />
  </app-section-card>

  <!-- Bottom grid: heatmap + table -->
  <div class="forecast-overview-view__grid">
    <!-- Deviation Heatmap -->
    <app-section-card title="偏差热力图">
      <div ref="heatmapChart" class="forecast-overview-view__heatmap" />
    </app-section-card>

    <!-- Station Prediction Table -->
    <app-section-card title="电站预测明细">
      <el-table :data="stationRows" size="mini" stripe>
        <el-table-column prop="name" label="电站" min-width="160" />
        <el-table-column prop="predicted" label="预测出力(MW)" width="130" align="right" />
        <el-table-column prop="actual" label="实际出力(MW)" width="130" align="right" />
        <el-table-column prop="deviation" label="偏差(MW)" width="100" align="right" />
        <el-table-column prop="accuracy" label="精度(%)" width="100" align="right" />
      </el-table>
    </app-section-card>
  </div>
</div>
```

KPIs: 日前预测精度(%), 超短期预测精度(%), 当日预测偏差(MW), 预测合格率(%)

**Comparison Chart** (ECharts):
- 3 lines: 日前预测 (#409EFF blue), 超短期预测 (#36CFC9 cyan), 实际出力 (#67C23A green)
- X axis: 96 time points (00:00-23:45, 15min intervals)
- Y axis: MW
- Area fill between actual and predicted for deviation visualization
- Dark theme: transparent bg, light text, subtle grid lines

**Heatmap** (ECharts):
- X axis: 24 hours (0-23)
- Y axis: station names
- Value: deviation percentage
- Visual map: green (low) → yellow → red (high)
- tooltip showing station, hour, deviation value

- [ ] **Step 2: Wire into ForecastPage**

Import and register in ForecastPage's components. Map `currentViewComponent` for 'overview' → 'ForecastOverviewView'.

- [ ] **Step 3: Verify with dev server**

Navigate to `/forecast/overview`. Verify: KPIs render, comparison chart shows 3 curves, heatmap renders color-coded, table shows station data. All in dark theme.

---

## Task 5: ForecastAdjustableView (可调能力分析)

**Files:**
- Create: `src/modules/forecast/components/views/ForecastAdjustableView.vue`

- [ ] **Step 1: Create view component**

Template structure:
```
<div class="forecast-adjustable-view">
  <!-- KPI Cards -->
  <section class="forecast-adjustable-view__kpis">
    <app-metric-card v-for="kpi in kpis" ... />
  </section>

  <!-- 24h Adjustable Capacity Curve -->
  <app-section-card title="24小时可调能力预测">
    <div ref="capacityChart" class="forecast-adjustable-view__chart" />
  </app-section-card>

  <!-- Station Adjustable Detail Table -->
  <app-section-card title="电站可调能力明细">
    <el-table :data="stationRows" size="mini" stripe>
      <el-table-column prop="name" label="电站" min-width="160" />
      <el-table-column prop="currentAdj" label="当前可调(MW)" width="130" align="right" />
      <el-table-column prop="predicted4h" label="4h预测(MW)" width="130" align="right" />
      <el-table-column prop="maxUp" label="可上调(MW)" width="120" align="right" />
      <el-table-column prop="maxDown" label="可下调(MW)" width="120" align="right" />
      <el-table-column prop="status" label="状态" width="100">
        <template slot-scope="{ row }">
          <el-tag :type="row.statusType" size="mini">{{ row.statusLabel }}</el-tag>
        </template>
      </el-table-column>
    </el-table>
  </app-section-card>
</div>
```

KPIs: 总可调容量(MW), 可上调容量(MW), 可下调容量(MW), 24h最大可调(MW)

**Capacity Chart** (ECharts):
- Area band: upper bound (可上调上限, green dashed) → lower bound (可下调下限, orange dashed), fill between with semi-transparent teal
- Line: predicted adjustable capacity (solid teal)
- X axis: 24h (96 points)
- Y axis: MW

**VPP Node Timeline** (ECharts custom/bar chart):
- Gantt-style chart showing per-station dispatch availability over 24h
- Y axis: station names
- X axis: 24h timeline (0:00-24:00)
- Horizontal bars showing available (green), dispatching (teal), unavailable (gray) time ranges
- Placed below the capacity chart, above the station table

Add to template after capacity chart:
```
<app-section-card title="VPP 节点可调时间线">
  <div ref="timelineChart" class="forecast-adjustable-view__chart" />
</app-section-card>
```

Mock data: each station has 2-3 availability windows per day (e.g., 06:00-11:00 available, 13:00-18:00 available).

- [ ] **Step 2: Wire into ForecastPage**

- [ ] **Step 3: Verify with dev server**

Navigate to `/forecast/adjustable`. Verify: KPIs, capacity curve with area band, VPP timeline, and station table all render correctly.

---

## Task 6: ForecastAccuracyView (精度评估)

**Files:**
- Create: `src/modules/forecast/components/views/ForecastAccuracyView.vue`

- [ ] **Step 1: Create view component**

Template structure:
```
<div class="forecast-accuracy-view">
  <!-- KPI Cards -->
  <section class="forecast-accuracy-view__kpis">
    <app-metric-card v-for="kpi in kpis" ... />
  </section>

  <!-- Top row: Trend + Distribution -->
  <div class="forecast-accuracy-view__grid">
    <!-- Accuracy Trend (30 days) -->
    <app-section-card title="预测精度趋势（近30日）">
      <div ref="trendChart" class="forecast-accuracy-view__chart" />
    </app-section-card>

    <!-- Deviation Distribution Histogram -->
    <app-section-card title="偏差分布">
      <div ref="distChart" class="forecast-accuracy-view__chart" />
    </app-section-card>
  </div>

  <!-- Bottom row: Ranking + Monthly -->
  <div class="forecast-accuracy-view__grid">
    <!-- Station Ranking (horizontal bar) -->
    <app-section-card title="电站精度排名">
      <div ref="rankChart" class="forecast-accuracy-view__chart" />
    </app-section-card>

    <!-- Monthly Comparison Table -->
    <app-section-card title="月度精度对比">
      <el-table :data="monthlyRows" size="mini" stripe>
        <el-table-column prop="month" label="月份" width="100" />
        <el-table-column prop="mae" label="MAE(MW)" width="110" align="right" />
        <el-table-column prop="rmse" label="RMSE(MW)" width="110" align="right" />
        <el-table-column prop="accuracy" label="精度(%)" width="100" align="right" />
        <el-table-column prop="improvement" label="环比" width="100" align="right">
          <template slot-scope="{ row }">
            <span :style="{ color: row.improvement > 0 ? '#67c23a' : '#f56c6c' }">
              {{ row.improvement > 0 ? '+' : '' }}{{ row.improvement }}%
            </span>
          </template>
        </el-table-column>
      </el-table>
    </app-section-card>
  </div>
</div>
```

KPIs: MAE(MW), RMSE(MW), 合格率(%), 本月平均精度(%)

Charts:
- **Trend**: Line chart, 30 data points (days), with a horizontal threshold line at 90% accuracy
- **Distribution**: Bar chart (histogram), 10 bins from -5MW to +5MW deviation, with a normal distribution fit curve overlaid (smooth line series on same chart)
- **Ranking**: Horizontal bar chart, stations sorted by accuracy descending, bars colored by value (green=high, orange=medium, red=low)

- [ ] **Step 2: Wire into ForecastPage**

- [ ] **Step 3: Verify with dev server**

Navigate to `/forecast/accuracy`. Verify all 4 chart areas + table render correctly.

---

## Task 7: Polish & Integration Verification

- [ ] **Step 1: Cross-tab navigation test**

Click through all 3 tabs, verify:
- URL changes correctly
- Filter state persists across tabs
- View data reloads for each tab
- No console errors

- [ ] **Step 2: Left menu navigation**

Click "预测与分析" in left sidebar menu. Verify it navigates to `/forecast/overview`.

- [ ] **Step 3: Responsive check**

Resize viewport to 1280px width. Verify layouts don't break — grids should collapse to single column where appropriate.

- [ ] **Step 4: Dark theme verification**

Inspect all components for any white backgrounds or unreadable text. Tables must use the global App.vue overrides. Charts must use transparent backgrounds.

- [ ] **Step 5: Commit**

```bash
git add src/modules/forecast/ src/shared/mock/forecast.js src/api/pvms.js src/router/index.js src/settings.js src/shared/mock/data.js
git commit -m "feat(M03): add forecast & analysis module with 3 views"
```

---

## Task 8: Playwright Verification Scan

- [ ] **Step 1: Create Playwright test file**

Create `tests/playwright/specs/forecast.spec.js` covering:
- Page loads at `/forecast/overview` without errors
- 3 tab buttons exist and are clickable
- KPI cards render with values
- Chart containers have non-zero dimensions
- Tables have data rows
- Tab switching changes URL and content
- Filter bar elements are interactive

- [ ] **Step 2: Run Playwright tests**

```bash
cd tests/playwright && npx playwright test specs/forecast.spec.js --headed
```

- [ ] **Step 3: Fix any issues found**

Address failures from Playwright scan.

---

## Execution Notes

- **Chart init timing**: Use `this.$nextTick()` after data arrives before calling `chart.setOption()`. Watch `viewData` with deep watcher.
- **Chart cleanup**: Always `chart.dispose()` in `beforeDestroy()` and remove resize listener.
- **No white colors**: All table text must be `rgba(255,255,255,0.85)`, backgrounds transparent. The global App.vue overrides handle most of this.
- **Minimize whitespace**: Charts should be tall (min-height: 520px per spec), grids should fill available space.
- **Mock data realism**: PV curves should be bell-shaped (peak at noon), zero at night. Predictions should be close to actual with small gaussian-like offsets.
