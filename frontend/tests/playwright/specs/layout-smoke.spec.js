const { test, expect } = require('@playwright/test')

const pages = [
  {
    name: 'production-monitor-overview',
    path: '/production-monitor/overview',
    title: '生产监控层',
    selector: '.app-page-hero__title'
  },
  {
    name: 'production-monitor-output',
    path: '/production-monitor/output',
    title: '生产监控层',
    selector: '.app-page-hero__title'
  },
  {
    name: 'dashboard-overview',
    path: '/dashboard/overview',
    title: '综合监控中心',
    selector: '.app-page-hero__title'
  },
  {
    name: 'stations-archive',
    path: '/stations/archive',
    title: '电站档案',
    selector: '.app-page-hero__title'
  },
  {
    name: 'devices-monitor',
    path: '/devices/monitor',
    title: '设备监控',
    selector: '.app-page-hero__title'
  },
  {
    name: 'alarms-center',
    path: '/alarms/center',
    title: '告警中心',
    selector: '.app-page-hero__title'
  }
]

pages.forEach(config => {
  test(`${config.name} layout is stable`, async ({ page }, testInfo) => {
    await page.goto(config.path)
    await expect(page.locator(config.selector, { hasText: config.title })).toBeVisible()

    const metrics = await page.evaluate(() => {
      const shell = document.querySelector('.pv-shell')
      const aside = document.querySelector('.pv-shell__aside')
      const main = document.querySelector('.pv-shell__content')
      const body = document.body
      const docEl = document.documentElement

      return {
        bodyScrollWidth: body.scrollWidth,
        viewportWidth: window.innerWidth,
        shellHeight: shell ? shell.getBoundingClientRect().height : 0,
        asideHeight: aside ? aside.getBoundingClientRect().height : 0,
        mainWidth: main ? main.getBoundingClientRect().width : 0,
        documentHeight: docEl.scrollHeight
      }
    })

    expect(metrics.bodyScrollWidth).toBeLessThanOrEqual(metrics.viewportWidth + 1)
    expect(metrics.shellHeight).toBeGreaterThan(760)
    expect(metrics.asideHeight).toBeGreaterThan(760)
    expect(metrics.mainWidth).toBeGreaterThan(900)
    expect(metrics.documentHeight).toBeGreaterThan(700)

    await page.screenshot({
      path: testInfo.outputPath(`${config.name}.png`),
      fullPage: true
    })
  })
})
