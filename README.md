# 光伏管理系统

## 目录

- `frontend/`: Vue2 微前端子应用，已按宿主挂载协议初始化
- `backend/`: Spring Boot 后端服务，按模块推进并强制测试先行

## 核心约束

1. 前端保持宿主适配能力，支持独立运行和后续接入现有宿主系统。
2. 前端视觉语言向当前宿主和 `vpp-main` 靠拢，重点复用深色面板、青蓝主色、表格和表单风格。
3. 前端使用 Playwright 截图测试校验关键页面布局，避免横向溢出、主体过窄和大面积留白。
4. 后端每完成一个功能模块，必须先补测试并通过，再进入下一个模块。
5. 前后端均以模块为单位维护 `docs/modules/*.md`，记录路由、参数和响应结构。

## 已初始化内容

### Frontend

- Vue2 + Vue Router + Vuex + Element UI 基础工程
- 可独立运行的侧边导航壳子
- 4 个核心页面骨架：
  - 总览驾驶舱
  - 电站档案
  - 设备监控
  - 告警中心
- 宿主桥接层：`public-path`、`mount/unmount`、`window._tsx` 请求兼容
- 模块文档：`frontend/docs/modules/*.md`
- Playwright 布局 smoke test：`frontend/tests/playwright`

### Backend

- Spring Boot 3.2.5 基础工程
- 通用响应体 `ApiResponse`
- 健康检查接口 `/api/system/health`
- MockMvc 测试用例
- 模块文档：`backend/docs/modules/system.md`

## 运行命令

### Frontend App

```powershell
cd C:\Users\zhuow\cowork_workplace\gitLabCoding\frontend\光伏管理系统\frontend
C:\Users\zhuow\tools\node-v14.16.0-win-x64\npm.cmd install --scripts-prepend-node-path=true
C:\Users\zhuow\tools\node-v14.16.0-win-x64\npm.cmd run serve --scripts-prepend-node-path=true
```

### Frontend Build

```powershell
cd C:\Users\zhuow\cowork_workplace\gitLabCoding\frontend\光伏管理系统\frontend
C:\Users\zhuow\tools\node-v14.16.0-win-x64\npm.cmd run build --scripts-prepend-node-path=true
```

### Frontend Playwright

```powershell
cd C:\Users\zhuow\cowork_workplace\gitLabCoding\frontend\光伏管理系统\frontend\tests\playwright
npm install
npm run install:browsers
npm test
```

### Backend

```powershell
cd C:\Users\zhuow\cowork_workplace\gitLabCoding\frontend\光伏管理系统\backend
mvn test
mvn spring-boot:run
```

## 模块推进顺序

1. 壳子与总览
2. 电站管理
3. 设备监控
4. 告警中心
5. 发电分析
