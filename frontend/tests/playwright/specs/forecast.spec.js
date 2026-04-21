const { test, expect } = require('@playwright/test')

test.describe('M03 Forecast & Analysis Module', () => {
  test('forecast overview page loads with KPIs and charts', async ({ page }) => {
    await page.goto('/forecast/overview')

    // Page structure
    await expect(page.locator('[data-testid="forecast-page"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-hero"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-tab-nav"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-filter-bar"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-overview-view"]')).toBeVisible()

    // 3 tab buttons exist
    await expect(page.locator('[data-testid="forecast-tab-overview"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-tab-adjustable"]')).toBeVisible()
    await expect(page.locator('[data-testid="forecast-tab-accuracy"]')).toBeVisible()

    // KPI cards render with values
    const kpiCards = page.locator('[data-testid="forecast-overview-view"] .app-metric-card')
    await expect(kpiCards).toHaveCount(4)

    // Check KPI values are not empty
    const kpiValues = page.locator('[data-testid="forecast-overview-view"] .app-metric-card__value')
    for (let i = 0; i < 4; i++) {
      const text = await kpiValues.nth(i).textContent()
      expect(text.trim().length).toBeGreaterThan(0)
    }

    // Chart containers exist and have non-zero dimensions
    const charts = await page.evaluate(() => {
      var canvases = document.querySelectorAll('[data-testid="forecast-overview-view"] canvas')
      var results = []
      canvases.forEach(function (c) {
        results.push({ width: c.width, height: c.height })
      })
      return results
    })
    expect(charts.length).toBeGreaterThanOrEqual(2)
    charts.forEach(function (c) {
      expect(c.width).toBeGreaterThan(0)
      expect(c.height).toBeGreaterThan(0)
    })

    // Station prediction table has data rows
    const tableRows = page.locator('[data-testid="forecast-overview-view"] .el-table__body tr')
    const rowCount = await tableRows.count()
    expect(rowCount).toBeGreaterThanOrEqual(10)
  })

  test('tab switching changes URL and view content', async ({ page }) => {
    await page.goto('/forecast/overview')
    await expect(page.locator('[data-testid="forecast-overview-view"]')).toBeVisible()

    // Click adjustable tab
    await page.click('[data-testid="forecast-tab-adjustable"]')
    await expect(page).toHaveURL(/\/forecast\/adjustable/)
    await expect(page.locator('[data-testid="forecast-adjustable-view"]')).toBeVisible()

    // KPI cards render in adjustable view
    const adjKpis = page.locator('[data-testid="forecast-adjustable-view"] .app-metric-card')
    await expect(adjKpis).toHaveCount(4)

    // Click accuracy tab
    await page.click('[data-testid="forecast-tab-accuracy"]')
    await expect(page).toHaveURL(/\/forecast\/accuracy/)
    await expect(page.locator('[data-testid="forecast-accuracy-view"]')).toBeVisible()

    // KPI cards render in accuracy view
    const accKpis = page.locator('[data-testid="forecast-accuracy-view"] .app-metric-card')
    await expect(accKpis).toHaveCount(4)

    // Click back to overview tab
    await page.click('[data-testid="forecast-tab-overview"]')
    await expect(page).toHaveURL(/\/forecast\/overview/)
    await expect(page.locator('[data-testid="forecast-overview-view"]')).toBeVisible()
  })

  test('filter bar elements are interactive', async ({ page }) => {
    await page.goto('/forecast/overview')
    await expect(page.locator('[data-testid="forecast-filter-bar"]')).toBeVisible()

    // Region select is clickable
    const regionSelect = page.locator('[data-testid="forecast-filter-bar"] .el-select').first()
    await regionSelect.click()
    // Dropdown should appear
    const dropdown = page.locator('.el-select-dropdown:visible')
    await expect(dropdown).toBeVisible()

    // Close dropdown by clicking elsewhere
    await page.click('[data-testid="forecast-hero"]')

    // Search button is clickable
    const searchBtn = page.locator('[data-testid="forecast-search"]')
    await expect(searchBtn).toBeVisible()
    await searchBtn.click()
  })

  test('adjustable view has VPP timeline and station table', async ({ page }) => {
    await page.goto('/forecast/adjustable')
    await expect(page.locator('[data-testid="forecast-adjustable-view"]')).toBeVisible()

    // Check charts exist (capacity curve + timeline)
    const charts = await page.evaluate(() => {
      var canvases = document.querySelectorAll('[data-testid="forecast-adjustable-view"] canvas')
      return canvases.length
    })
    expect(charts).toBeGreaterThanOrEqual(2)

    // Station table has data
    const tableRows = page.locator('[data-testid="forecast-adjustable-view"] .el-table__body tr')
    const rowCount = await tableRows.count()
    expect(rowCount).toBeGreaterThanOrEqual(10)
  })

  test('accuracy view has trend, distribution, ranking, and monthly table', async ({ page }) => {
    await page.goto('/forecast/accuracy')
    await expect(page.locator('[data-testid="forecast-accuracy-view"]')).toBeVisible()

    // Check charts exist (trend + distribution + ranking = 3)
    const charts = await page.evaluate(() => {
      var canvases = document.querySelectorAll('[data-testid="forecast-accuracy-view"] canvas')
      return canvases.length
    })
    expect(charts).toBeGreaterThanOrEqual(3)

    // Monthly table has data
    const tableRows = page.locator('[data-testid="forecast-accuracy-view"] .el-table__body tr')
    const rowCount = await tableRows.count()
    expect(rowCount).toBeGreaterThanOrEqual(5)
  })

  test('left sidebar menu navigates to forecast', async ({ page }) => {
    await page.goto('/dashboard/overview')

    // Find and click the forecast menu item
    const forecastMenu = page.locator('text=预测与分析').first()
    await expect(forecastMenu).toBeVisible()
    await forecastMenu.click()

    await expect(page).toHaveURL(/\/forecast\/overview/)
    await expect(page.locator('[data-testid="forecast-page"]')).toBeVisible()
  })

  test('no console errors on any forecast tab', async ({ page }) => {
    var errors = []
    page.on('pageerror', function (err) { errors.push(err.message) })

    await page.goto('/forecast/overview')
    await page.waitForSelector('[data-testid="forecast-overview-view"]')

    await page.click('[data-testid="forecast-tab-adjustable"]')
    await page.waitForSelector('[data-testid="forecast-adjustable-view"]')

    await page.click('[data-testid="forecast-tab-accuracy"]')
    await page.waitForSelector('[data-testid="forecast-accuracy-view"]')

    expect(errors).toEqual([])
  })
})
