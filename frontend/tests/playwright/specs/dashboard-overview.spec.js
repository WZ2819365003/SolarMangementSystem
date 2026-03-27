const { test, expect } = require('@playwright/test')

test('dashboard overview renders core monitoring sections', async ({
  page
}, testInfo) => {
  await page.goto('/dashboard/overview')

  await expect(page.locator('[data-testid="dashboard-overview"]')).toBeVisible()
  await expect(page.locator('[data-testid="dashboard-map-card"]')).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-map-insight-panel"]')
  ).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-focus-stations"]')
  ).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-focus-station-indicators"]')
  ).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-dispatch-digest"]')
  ).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-dispatch-actions"]')
  ).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-region-summary"]')
  ).toBeVisible()
  await expect(page.locator('[data-testid="dashboard-kpi-board"]')).toBeVisible()
  await expect(page.locator('[data-testid="dashboard-power-curve"]')).toBeVisible()
  await expect(
    page.locator('[data-testid="dashboard-station-ranking"]')
  ).toBeVisible()
  await expect(page.locator('[data-testid="dashboard-alarm-feed"]')).toBeVisible()
  await expect(page.locator('[data-testid="dashboard-weather-card"]')).toBeVisible()

  const metrics = await page.evaluate(() => {
    const mapCard = document.querySelector('[data-testid="dashboard-map-card"]')
    const mapStage = document.querySelector('.dashboard-map-card__stage')
    const mapInsightPanel = document.querySelector(
      '[data-testid="dashboard-map-insight-panel"]'
    )
    const focusItems = document.querySelectorAll(
      '[data-testid="dashboard-focus-station-item"]'
    )
    const focusBadges = document.querySelectorAll(
      '[data-testid="dashboard-focus-station-badge"]'
    )
    const focusIndicators = document.querySelectorAll(
      '[data-testid="dashboard-focus-station-indicator"]'
    )
    const dispatchBars = document.querySelectorAll(
      '[data-testid="dashboard-dispatch-progress"]'
    )
    const dispatchActions = document.querySelectorAll(
      '[data-testid="dashboard-dispatch-action-item"]'
    )
    const kpiBoard = document.querySelector(
      '[data-testid="dashboard-kpi-board"]'
    )
    const powerCurve = document.querySelector(
      '[data-testid="dashboard-power-curve"]'
    )

    return {
      mapWidth: mapCard ? mapCard.getBoundingClientRect().width : 0,
      mapStageHeight: mapStage ? mapStage.getBoundingClientRect().height : 0,
      mapInsightHeight: mapInsightPanel
        ? mapInsightPanel.getBoundingClientRect().height
        : 0,
      focusItemCount: focusItems.length,
      focusBadgeCount: focusBadges.length,
      focusIndicatorCount: focusIndicators.length,
      dispatchBarCount: dispatchBars.length,
      dispatchActionCount: dispatchActions.length,
      kpiHeight: kpiBoard ? kpiBoard.getBoundingClientRect().height : 0,
      powerCurveHeight: powerCurve ? powerCurve.getBoundingClientRect().height : 0
    }
  })

  expect(metrics.mapWidth).toBeGreaterThan(480)
  expect(metrics.mapStageHeight).toBeGreaterThan(500)
  expect(metrics.mapInsightHeight).toBeGreaterThan(180)
  expect(metrics.focusItemCount).toBe(3)
  expect(metrics.focusBadgeCount).toBeGreaterThanOrEqual(9)
  expect(metrics.focusIndicatorCount).toBeGreaterThanOrEqual(2)
  expect(metrics.dispatchBarCount).toBe(4)
  expect(metrics.dispatchActionCount).toBe(2)
  expect(metrics.kpiHeight).toBeGreaterThan(320)
  expect(metrics.powerCurveHeight).toBeGreaterThan(260)

  const mapViewBeforeFocus = await page.evaluate(() => {
    const card = document.querySelector('[data-testid="dashboard-map-card"]')
    const mapVm = card && card.__vue__ && card.__vue__.$parent
    if (!mapVm || !mapVm.mapInstance) {
      return {
        found: false
      }
    }

    return {
      found: true,
      zoom: mapVm.mapInstance.getZoom(),
      hasUserSelectionFocus: mapVm.hasUserSelectionFocus,
      selectedStationId: mapVm.selectedStationId
    }
  })

  expect(mapViewBeforeFocus.found).toBe(true)
  expect(mapViewBeforeFocus.hasUserSelectionFocus).toBe(false)
  expect(mapViewBeforeFocus.zoom).toBeLessThan(7)

  await page.locator('[data-testid="dashboard-focus-station-item"]').nth(1).click()

  await expect(
    page.locator('[data-testid="dashboard-map-reset-overview"]')
  ).toBeVisible()

  await expect
    .poll(
      async () =>
        page.evaluate(() => {
          const card = document.querySelector('[data-testid="dashboard-map-card"]')
          const mapVm = card && card.__vue__ && card.__vue__.$parent
          return mapVm && mapVm.mapInstance ? mapVm.mapInstance.getZoom() : 0
        }),
      {
        timeout: 5000
      }
    )
    .toBeGreaterThan(13.5)

  const mapFocusMetrics = await page.evaluate(() => {
    const card = document.querySelector('[data-testid="dashboard-map-card"]')
    const mapVm = card && card.__vue__ && card.__vue__.$parent
    if (!mapVm || !mapVm.mapInstance) {
      return {
        found: false
      }
    }

    return {
      found: true,
      zoom: mapVm.mapInstance.getZoom(),
      hasUserSelectionFocus: mapVm.hasUserSelectionFocus,
      selectedStationId: mapVm.selectedStationId
    }
  })

  expect(mapFocusMetrics.found).toBe(true)
  expect(mapFocusMetrics.hasUserSelectionFocus).toBe(true)
  expect(mapFocusMetrics.selectedStationId).not.toBe('PV-001')
  expect(mapFocusMetrics.zoom).toBeGreaterThan(13.5)

  await page.locator('[data-testid="dashboard-map-reset-overview"]').click()

  await expect
    .poll(
      async () =>
        page.evaluate(() => {
          const card = document.querySelector('[data-testid="dashboard-map-card"]')
          const mapVm = card && card.__vue__ && card.__vue__.$parent
          return mapVm && mapVm.mapInstance ? mapVm.mapInstance.getZoom() : 99
        }),
      {
        timeout: 5000
      }
    )
    .toBeLessThan(7)

  const mapViewAfterReset = await page.evaluate(() => {
    const card = document.querySelector('[data-testid="dashboard-map-card"]')
    const mapVm = card && card.__vue__ && card.__vue__.$parent
    if (!mapVm || !mapVm.mapInstance) {
      return {
        found: false
      }
    }

    return {
      found: true,
      zoom: mapVm.mapInstance.getZoom(),
      hasUserSelectionFocus: mapVm.hasUserSelectionFocus
    }
  })

  expect(mapViewAfterReset.found).toBe(true)
  expect(mapViewAfterReset.hasUserSelectionFocus).toBe(false)
  expect(mapViewAfterReset.zoom).toBeLessThan(7)

  await page.screenshot({
    path: testInfo.outputPath('dashboard-overview.png'),
    fullPage: true
  })
})
