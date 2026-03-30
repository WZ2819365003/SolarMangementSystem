const { test, expect } = require('@playwright/test')

test('development bridge requests backend data through the dev proxy', async ({
  page
}) => {
  const backendRequests = []

  page.on('request', request => {
    if (request.url().includes('/api/pvms/dashboard/stations-geo')) {
      backendRequests.push({
        method: request.method(),
        url: request.url()
      })
    }
  })

  await page.goto('/dashboard/overview')
  await expect(page.locator('[data-testid="dashboard-overview"]')).toBeVisible()

  await expect
    .poll(() => backendRequests.length, {
      timeout: 5000
    })
    .toBeGreaterThan(0)
})
