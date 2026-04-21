# Dashboard 前端模块

## 1. 模块职责

dashboard 是系统首页，负责把多个摘要型能力汇集到一个入口中展示：

- GIS 站点地图
- 核心 KPI
- 功率曲线
- 排名信息
- 最近告警
- 天气信息
- VPP 节点摘要

这里最重要的维护认识是：前端负责地图渲染，不再负责地图业务真相。

## 2. 代码入口

- 页面组件：`frontend/src/modules/dashboard/pages/DashboardPage.vue`
- 地图主卡片：`frontend/src/modules/dashboard/components/DashboardMapCard.vue`
- 地图筛选：`frontend/src/modules/dashboard/components/DashboardMapFilterPanel.vue`
- 地图洞察：`frontend/src/modules/dashboard/components/DashboardMapInsightPanel.vue`
- 地图详情：`frontend/src/modules/dashboard/components/DashboardMapDetail.vue`
- AMap 插件：`frontend/src/shared/plugins/amap.js`
- API 入口：`frontend/src/api/pvms.js`
- 请求桥接：`frontend/src/shared/host/bridge.js`

## 3. 当前前后端边界

### 前端负责

- AMap SDK 加载
- 地图底图样式
- 地图实例初始化
- Marker 渲染
- 选中态和聚焦
- SDK 异常时 fallback 视图

### 后端负责

- 站点点位
- 站点状态
- 资源单元归属
- 地图筛选项
- 状态摘要统计
- 地图洞察区依赖的最近告警
- VPP 节点摘要

这条边界已经在当前代码里落地，前端不应再引入新的地图业务 mock 来覆盖后端返回。

## 4. 页面数据流

### 地图链路

地图主卡片依赖：

- `GET /pvms/dashboard/stations-geo`
- `GET /pvms/alarms/recent`
- `GET /pvms/dashboard/vpp-node-status`

前端收到数据后：

- 用 `stations` 渲染地图点位
- 用 `summary` 渲染地图状态摘要
- 用 `filters` 渲染筛选器
- 用 `items` 渲染洞察告警区
- 用 `vppNodePayload` 渲染节点摘要

### 其他首页链路

目前仍有部分首页区域消费 mock 风格接口：

- `GET /pvms/dashboard/kpi-summary`
- `GET /pvms/dashboard/power-curve`
- `GET /pvms/dashboard/station-ranking`
- `GET /pvms/weather/current`

所以 dashboard 前端虽然已经能打真实后端，但不代表首页所有区域都已经接到数据库化后端。

## 5. `stations-geo` 当前契约

地图点位项现在至少包含：

- `id`
- `name`
- `resourceUnitId`
- `resourceUnitName`
- `region`
- `longitude`
- `latitude`
- `status`
- `statusLabel`
- `statusColor`
- `capacityKwp`
- `realtimePowerKw`
- `todayEnergyKwh`
- `todayRevenueCny`
- `healthGrade`
- `availability`
- `address`

如果以后前端新增弹层、跳转或联动，优先复用这些字段，不要再从本地 mock 重新拼站点信息。

## 6. 开发环境注意事项

### 请求链路

开发环境现在默认走后端代理：

- 前端请求基地址默认是 `/api`
- `vue.config.js` 会把 `/api` 代理到 `http://127.0.0.1:8091`

只有显式设置 `VUE_APP_REQUEST_MODE=mock` 时，才会退回前端 mock。

### 地图资源

出现下面这种外部请求属于正常现象：

- `https://jsapi-data1.amap.com/...`

这是 AMap 的底图资源请求，不是你后端接口。

## 7. 排障建议

### 地图没有底图

优先检查：

- AMap key 和安全配置
- 网络是否能访问高德资源
- `amap.js` 是否加载失败

### 地图有底图但没有点位

优先检查：

- `/api/pvms/dashboard/stations-geo` 是否返回数据
- 开发环境是否误切回 `mock`
- 后端是否已经启动到最新代码

### 地图有点位但洞察区为空

优先检查：

- `/api/pvms/alarms/recent`
- `/api/pvms/dashboard/vpp-node-status`

## 8. 维护原则

- 地图底图样式问题，优先在前端处理。
- 点位、状态、统计口径问题，优先在后端处理。
- 资源单元、告警、VPP 节点这类业务字段，不要回退到前端硬编码。
- 如果未来要扩展 production-monitor、forecast、strategy 的地图或地理分布能力，优先复用这次“前端静态渲染 + 后端动态事实”的边界。
