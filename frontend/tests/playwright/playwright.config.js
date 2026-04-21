const path = require('path')
const { defineConfig } = require('@playwright/test')

const frontendRoot = path.resolve(__dirname, '..', '..')
const npmPath = 'C:\\Users\\zhuow\\tools\\node-v14.16.0-win-x64\\npm.cmd'

module.exports = defineConfig({
  testDir: './specs',
  timeout: 30000,
  reporter: [
    ['list'],
    ['html', { outputFolder: 'output/report', open: 'never' }]
  ],
  use: {
    baseURL: 'http://127.0.0.1:6618',
    viewport: { width: 1440, height: 900 },
    trace: 'retain-on-failure',
    screenshot: 'only-on-failure',
    headless: true
  },
  webServer: {
    command: `"${npmPath}" run serve --scripts-prepend-node-path=true`,
    cwd: frontendRoot,
    url: 'http://127.0.0.1:6618/dashboard/overview',
    timeout: 120000,
    reuseExistingServer: !process.env.CI
  }
})
