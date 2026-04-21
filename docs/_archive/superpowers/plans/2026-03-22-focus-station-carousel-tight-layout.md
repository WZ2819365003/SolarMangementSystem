# Focus Station Carousel Tight Layout Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 将首页“重点关注电站”收紧为 3 条可见项，并通过轻量轮播继续展示更多电站，避免卡片视觉过高。

**Architecture:** 保持现有洞察区三卡结构不变，仅调整 `DashboardFocusStationList.vue` 的展示策略。数据仍由 `DashboardMapInsightPanel.vue` 聚合，组件内部负责分页窗口、自动轮播与悬停暂停，Playwright 用可见项数量和轮播指示器校验新行为。

**Tech Stack:** Vue 2, Element UI, Less, Playwright

---

### Task 1: Lock the new visible-window behavior with a failing test

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/tests/playwright/specs/dashboard-overview.spec.js`

- [ ] **Step 1: Write the failing test**
  Assert that the focus station card renders exactly 3 visible items and exposes carousel indicators.

- [ ] **Step 2: Run test to verify it fails**
  Run: `npm.cmd test -- dashboard-overview.spec.js`
  Expected: FAIL because the focus station card still renders 4 items and has no carousel indicator.

### Task 2: Implement the compact carousel card

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/src/modules/dashboard/components/DashboardFocusStationList.vue`

- [ ] **Step 3: Write minimal implementation**
  Add a 3-item visible window, simple page indicators, auto-rotation, and hover-to-pause while keeping current badge density.

- [ ] **Step 4: Run focused test to verify it passes**
  Run: `npm.cmd test -- dashboard-overview.spec.js`
  Expected: PASS

### Task 3: Sync docs and verify the whole slice

**Files:**
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/frontend/docs/modules/dashboard.md`
- Modify: `C:/Users/zhuow/cowork_workplace/gitLabCoding/frontend/光伏管理系统/doc/M01_综合监控中心_前端设计.md`

- [ ] **Step 5: Update docs**
  Document that the focus station card now shows 3 visible items with carousel-style pagination.

- [ ] **Step 6: Run full verification**
  Run:
  - `C:/Users/zhuow/tools/node-v14.16.0-win-x64/npm.cmd run lint -- --no-fix`
  - `C:/Users/zhuow/tools/node-v14.16.0-win-x64/npm.cmd run build --scripts-prepend-node-path=true`
  - `npm.cmd test`
  Expected: all pass, with only existing bundle-size warnings during build.
