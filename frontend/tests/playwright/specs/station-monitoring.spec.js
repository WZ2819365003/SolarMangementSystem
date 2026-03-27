const { test, expect } = require('@playwright/test')

test('production monitor module renders structured shell and tab views', async ({
  page
}, testInfo) => {
  await page.setViewportSize({ width: 1920, height: 1080 })
  await page.goto('/production-monitor/overview')

  await expect(page.locator('[data-testid="production-monitor-page"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-hero"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-tab-nav"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-filter-bar"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-tab-overview"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-tab-output"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-tab-dispatch"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-tab-weather"]')).toBeVisible()
  await expect(page.locator('.el-menu-item.is-active', { hasText: '生产监控层' })).toBeVisible()

  await expect(page.locator('[data-testid="production-overview-view"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-overview-kpis"]')).toBeVisible()
  await expect(page.locator('[data-testid="member-station-summary-table"]')).toBeVisible()
  await expect(page.locator('[data-testid="weather-brief-card"]')).toBeVisible()
  await expect(page.locator('[data-testid="alarm-brief-card"]')).toBeVisible()

  const resourceUnitFilter = page.locator(
    '[data-testid="production-resource-unit-select"]'
  )
  await resourceUnitFilter.click()

  const resourceUnitOptions = page.locator(
    '.el-select-dropdown:visible .el-select-dropdown__item'
  )
  await expect(resourceUnitOptions.first()).toBeVisible()
  const resourceOptionCount = await resourceUnitOptions.count()
  expect(resourceOptionCount).toBeGreaterThanOrEqual(6)

  const initialUnitName = await page
    .locator('.production-monitor-hero__unit-name')
    .textContent()

  await resourceUnitOptions.nth(resourceOptionCount - 1).click()
  await page.locator('[data-testid="production-monitor-search"]').click()

  await expect(page.locator('.production-monitor-hero__unit-name')).not.toHaveText(
    initialUnitName || ''
  )

  const overviewKpiRows = await page.evaluate(() => {
    const cards = Array.from(
      document.querySelectorAll('[data-testid="production-overview-kpis"] > *')
    )
    return cards.map(card => Math.round(card.getBoundingClientRect().top))
  })

  expect(new Set(overviewKpiRows).size).toBe(1)

  const overviewMatrix = await page.evaluate(() => {
    const cards = Array.from(
      document.querySelectorAll(
        '[data-testid="production-overview-view"] .app-section-card'
      )
    )
    const [member, weather, alarm, running] = cards.map(card => {
      const rect = card.getBoundingClientRect()
      return {
        top: Math.round(rect.top),
        left: Math.round(rect.left),
        width: Math.round(rect.width)
      }
    })

    return { member, weather, alarm, running }
  })

  expect(Math.abs(overviewMatrix.member.top - overviewMatrix.weather.top)).toBeLessThan(20)
  expect(Math.abs(overviewMatrix.running.top - overviewMatrix.alarm.top)).toBeLessThan(20)
  expect(Math.abs(overviewMatrix.member.left - overviewMatrix.running.left)).toBeLessThan(20)
  expect(Math.abs(overviewMatrix.weather.left - overviewMatrix.alarm.left)).toBeLessThan(20)
  expect(overviewMatrix.running.width).toBeLessThanOrEqual(
    overviewMatrix.member.width + 80
  )

  const sharedWeatherMetrics = await page.evaluate(() => {
    const cells = Array.from(
      document.querySelectorAll('[data-testid="member-station-weather-cell"]')
    )
    return {
      count: cells.length,
      values: cells.map(item => item.textContent.trim())
    }
  })

  expect(sharedWeatherMetrics.count).toBeGreaterThanOrEqual(3)
  expect(new Set(sharedWeatherMetrics.values).size).toBeLessThanOrEqual(2)

  await page.locator('[data-testid="production-tab-output"]').click()
  await expect(page.locator('[data-testid="production-output-view"]')).toBeVisible()
  await expect(page.locator('[data-testid="power-curve-panel"]')).toBeVisible()
  await expect(page.locator('[data-testid="weather-trend-panel"]')).toBeVisible()
  await expect(page.locator('[data-testid="member-station-ranking"]')).toBeVisible()

  const outputLayout = await page.evaluate(() => {
    const splitGrid = document.querySelector('[data-testid="production-output-view"] .pv-split-grid')
    if (!splitGrid || splitGrid.children.length < 2) {
      return { sameRow: false }
    }

    const firstRect = splitGrid.children[0].getBoundingClientRect()
    const secondRect = splitGrid.children[1].getBoundingClientRect()

    return {
      sameRow: Math.abs(firstRect.top - secondRect.top) < 16
    }
  })

  expect(outputLayout.sameRow).toBe(true)

  const timeSliceWidthRatio = await page.evaluate(() => {
    const cards = Array.from(
      document.querySelectorAll('[data-testid="production-output-view"] .app-section-card')
    )
    const targetCard = cards.find(card => card.textContent.includes('分时数据'))
    if (!targetCard) {
      return 0
    }

    const shell = targetCard.querySelector('.pv-table-shell')
    const table = targetCard.querySelector('.el-table')
    if (!shell || !table) {
      return 0
    }

    return Number(
      (
        table.getBoundingClientRect().width / shell.getBoundingClientRect().width
      ).toFixed(2)
    )
  })

  expect(timeSliceWidthRatio).toBeGreaterThan(0.95)

  await page.locator('[data-testid="production-tab-dispatch"]').click()
  await expect(page.locator('[data-testid="production-dispatch-view"]')).toBeVisible()
  await expect(page.locator('[data-testid="dispatch-trend-panel"]')).toBeVisible()
  await expect(page.locator('[data-testid="dispatch-record-table"]')).toBeVisible()

  await page.locator('[data-testid="production-tab-weather"]').click()
  await expect(page.locator('[data-testid="production-weather-view"]')).toBeVisible()
  await expect(page.locator('[data-testid="weather-trend-72h"]')).toBeVisible()
  await expect(page.locator('[data-testid="weather-impact-table"]')).toBeVisible()

  const weatherTableTheme = await page.evaluate(() => {
    const cell = document.querySelector('[data-testid="weather-impact-table"] .el-table__body td')
    const header = document.querySelector('[data-testid="weather-impact-table"] .el-table th')

    if (!cell || !header) {
      return { bodyBg: '', headerBg: '' }
    }

    return {
      bodyBg: window.getComputedStyle(cell).backgroundColor,
      headerBg: window.getComputedStyle(header).backgroundColor
    }
  })

  expect(weatherTableTheme.bodyBg).not.toBe('rgba(0, 0, 0, 0)')
  expect(weatherTableTheme.headerBg).not.toBe('rgba(0, 0, 0, 0)')

  await page.screenshot({
    path: testInfo.outputPath('production-monitor-redesign.png'),
    fullPage: true
  })
})
