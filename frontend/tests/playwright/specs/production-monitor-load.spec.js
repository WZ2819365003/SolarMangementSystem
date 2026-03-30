const { test, expect } = require('@playwright/test')

test('production monitor load view renders backend-backed summary, table, and drawer', async ({
  page
}, testInfo) => {
  await page.goto('/production-monitor/load?resourceUnitId=RU-001')

  await expect(page.locator('[data-testid="production-monitor-page"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-hero"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-tab-nav"]')).toBeVisible()
  await expect(page.locator('[data-testid="production-monitor-filter-bar"]')).toBeVisible()

  await expect(page.locator('.production-load-view')).toBeVisible()
  await expect(page.getByText('总负荷', { exact: true })).toBeVisible()
  await expect(page.getByText('总光伏出力', { exact: true })).toBeVisible()
  await expect(page.getByText('总可调空间', { exact: true })).toBeVisible()
  await expect(page.getByText('平均爬坡速率', { exact: true })).toBeVisible()

  const loadMetrics = await page.evaluate(() => {
    const kpiItems = Array.from(
      document.querySelectorAll('.production-load-view__kpi-item')
    )
    const tableRows = Array.from(
      document.querySelectorAll('.production-load-view .el-table__body-wrapper tbody tr')
    )

    return {
      kpiCount: kpiItems.length,
      kpiTexts: kpiItems.map(item => item.textContent.trim()),
      rowCount: tableRows.length
    }
  })

  expect(loadMetrics.kpiCount).toBe(6)
  expect(loadMetrics.rowCount).toBeGreaterThanOrEqual(10)
  expect(loadMetrics.kpiTexts.some(text => text.includes('16.3'))).toBe(true)
  expect(loadMetrics.kpiTexts.some(text => text.includes('12.5'))).toBe(true)

  const firstViewButton = page.locator('.production-load-view .el-table__body-wrapper tbody tr .el-button--text').first()
  await expect(firstViewButton).toBeVisible()
  await firstViewButton.click()

  const drawer = page.locator('.production-load-view__drawer')
  await expect(drawer).toBeVisible()
  await expect(drawer.getByText('电网交互曲线')).toBeVisible()
  await expect(drawer.getByText('当前负荷')).toBeVisible()
  await expect(drawer.getByText('光伏出力')).toBeVisible()
  await expect(drawer.getByText('可调功率')).toBeVisible()

  const chartMetrics = await page.evaluate(() => {
    const chart = document.querySelector('.production-load-view__dialog-chart')
    return {
      height: chart ? Math.round(chart.getBoundingClientRect().height) : 0,
      canvasCount: chart ? chart.querySelectorAll('canvas').length : 0
    }
  })

  expect(chartMetrics.height).toBeGreaterThan(240)
  expect(chartMetrics.canvasCount).toBeGreaterThan(0)

  await page.screenshot({
    path: testInfo.outputPath('production-monitor-load.png'),
    fullPage: true
  })
})
