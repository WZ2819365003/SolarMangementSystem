# Device Alarm H2 Migration Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Finish the `devices` and `alarms` modules by moving them from frontend mock assumptions to a Spring Boot + H2 backend implementation based on the existing station-archive facts.

**Architecture:** Build a dedicated `devicealarm` backend module over `sa_station`, `sa_inverter`, `sa_inverter_alarm`, and `sa_station_strategy`, then keep the current frontend pages as thin consumers of the backend contract. Prefer derived calculations over new duplicated seed tables.

**Tech Stack:** Vue 2, Element UI, Spring Boot, H2, MockMvc, Maven

---

### Task 1: Lock the Contracts with Failing Tests

**Files:**
- Create or modify: `backend/src/test/java/cn/techstar/pvms/backend/module/devicealarm/controller/DeviceAlarmControllerTest.java`
- Reference: `frontend/src/modules/devices/pages/DeviceMonitorPage.vue`
- Reference: `frontend/src/modules/alarms/pages/AlarmCenterPage.vue`

- [ ] Write failing tests for `GET /api/pvms/devices/monitor`
- [ ] Run the focused controller test and verify the expected failures
- [ ] Write failing tests for `GET /api/pvms/alarms/center`
- [ ] Run the focused controller test again and verify the expected failures

### Task 2: Build the Backend Read Model

**Files:**
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/controller/DeviceAlarmController.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/repository/DeviceAlarmRepository.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/service/DeviceAlarmDataService.java`
- Create: `backend/src/main/java/cn/techstar/pvms/backend/module/devicealarm/service/DeviceAlarmSupport.java`

- [ ] Add repository queries over `sa_inverter`, `sa_station`, `sa_inverter_alarm`, and `sa_station_strategy`
- [ ] Implement device monitor aggregation
- [ ] Implement alarm center aggregation
- [ ] Re-run the focused controller tests until green

### Task 3: Wire the Frontend to the Real Contract

**Files:**
- Modify only if needed: `frontend/src/modules/devices/pages/DeviceMonitorPage.vue`
- Modify only if needed: `frontend/src/modules/alarms/pages/AlarmCenterPage.vue`
- Modify only if needed: `frontend/src/api/pvms.js`

- [ ] Compare the backend payload with the current page expectations
- [ ] Apply the smallest frontend adjustments needed for live data
- [ ] Verify no extra mock-only assumptions remain in these two pages

### Task 4: Update Handover Documentation

**Files:**
- Modify: `docs/modules/backend/device-alarm.md`
- Modify: `docs/modules/frontend/devices.md`
- Modify: `docs/modules/frontend/alarms.md`
- Modify: `docs/handover/README.md`
- Modify: `docs/handover/05_后端维护手册.md`
- Modify: `docs/handover/08_测试_排障_交接清单.md`
- Create: `docs/handover/14_M05_M06_设备监测与告警中心_H2数据与计算说明.md`

- [ ] Document the device/alarm data sources
- [ ] Document which indicators are computed in the backend
- [ ] Document extension points for future real external systems

### Task 5: Verify and Integrate

**Files:**
- No source file target, verification and git integration only

- [ ] Run `mvn test`
- [ ] Run frontend build
- [ ] Verify backend direct endpoints
- [ ] Verify frontend proxy endpoints
- [ ] Commit the feature branch
- [ ] Merge back into `dev`
- [ ] Restart frontend and backend from merged `dev`
