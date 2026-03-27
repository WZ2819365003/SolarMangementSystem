# Infrastructure Module

## Purpose

- 记录前端基础依赖、宿主兼容层和三方能力接入方式。

## UI Libraries

- `element-ui`: 基础表单、表格、弹窗组件
- `hs-dtk-ui`: 对齐现有宿主和 `vpp-main` 风格的扩展组件
- `echarts` + `echarts-liquidfill`: 图表和增强图形能力

## Dependency Sources

- `hs-dtk-ui`: 目标来源是 `http://223.71.1.243/repository/npm-hosted/hs-dtk-ui/-/hs-dtk-ui-0.1.29.tgz`，但该地址当前返回统一认证页，因此临时改为复用本机已安装的 `0.1.29` tgz：`../../vpp-main/node_modules/hs-dtk-ui/hs-dtk-ui-0.1.29.tgz`
- `@amap/amap-jsapi-loader`: 官方 npm 包

## Map Config

- Loader: `@amap/amap-jsapi-loader`
- Config file: `frontend/src/shared/plugins/amap.js`
- Env keys:
  - `VUE_APP_AMAP_KEY`
  - `VUE_APP_AMAP_SECURITY_CODE`
  - `VUE_APP_AMAP_VERSION`

## Response

- 本模块不提供业务路由
- 主要职责是为后续页面模块提供依赖和运行时配置
