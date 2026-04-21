# Production Monitor Module

## Scope

- 模块编号: `M02`
- 当前模块名称: `生产监控层`
- 当前主入口:
  - `/production-monitor/overview`
  - `/production-monitor/output`
  - `/production-monitor/dispatch`
  - `/production-monitor/weather`
- 当前产品定位:
  - 面向虚拟电厂聚合商的光伏生产监控模块
  - 模块 1 负责看全局，模块 2 负责看某个聚合资源单元的生产运行

## Core Model

- 一级业务对象是 `聚合资源单元`
- 成员对象是 `电站`
- 同一个聚合资源单元内:
  - 成员电站在地理位置上相对接近
  - 共用同一套天气剖面
  - 共用同一条总出力趋势
  - 各电站只按照容量权重和状态系数分摊出力比例
- 如果成员电站距离过远:
  - 不能继续放在同一个资源单元中建模
  - 应单独配置为新的资源单元

## Routes

### `/production-monitor/overview`

- 作用:
  - 查看资源单元健康状态、可调能力、在线率、天气快报和成员电站摘要
- 页面结构:
  - 资源单元状态条
  - 6 张 KPI 卡
  - 成员电站摘要表
  - 天气快报卡
  - 告警快报卡
  - 运行摘要表

### `/production-monitor/output`

- 作用:
  - 查看资源单元出力预测、实际出力、共享天气趋势和成员电站贡献
- 页面结构:
  - 指标摘要卡
  - 主出力曲线
  - 天气趋势曲线
  - 成员电站贡献排行
  - 分时数据表

### `/production-monitor/dispatch`

- 作用:
  - 查看调度指令执行、响应时长、风险提示和执行记录
- 页面结构:
  - 执行摘要卡
  - 指令执行趋势
  - 风险提示列表
  - 执行记录表

### `/production-monitor/weather`

- 作用:
  - 从光伏生产视角解读天气变化，并为后续调度判断提供依据
- 页面结构:
  - 天气摘要卡
  - 72 小时趋势图
  - 光伏天气影响表

## Shared Query State

- 页面顶部四个视图共享同一套查询状态
- 当前共享字段:
  - `resourceUnitId`
  - `region`
  - `city`
  - `date`
  - `granularity`
- 当前行为:
  - 切换横向导航时保留当前资源单元和筛选条件
  - 切换资源单元后只刷新当前视图的数据
  - `date` 当前主要作为前端保留参数，后端 mock 还没有真正消费它

## Frontend Request Chain

- API 封装入口:
  - `src/api/pvms.js`
- 宿主桥接:
  - `src/shared/host/bridge.js`
- 本地 mock:
  - `src/shared/mock/station-monitoring.js`

开发态默认行为:

- 优先走前端 mock
- 不直接请求本地 Spring Boot
- 后续如果要切到本地后端联调，需要调整桥接层

## APIs

### `GET /pvms/production-monitor/meta`

- 用途:
  - 初始化区域、城市、资源单元和默认资源单元
- 当前请求参数:
  - 无必填参数
- 当前响应核心字段:
  - `defaultResourceUnitId`
  - `regionOptions`
  - `resourceUnits`

### `GET /pvms/production-monitor/overview`

- 用途:
  - 获取资源总览视图数据
- 当前请求参数:
  - `resourceUnitId`
- 当前响应核心字段:
  - `info`
  - `kpis`
  - `memberStations`
  - `weatherBrief`
  - `alarmBrief`
  - `summaryTable`

### `GET /pvms/production-monitor/output`

- 用途:
  - 获取出力分析视图数据
- 当前请求参数:
  - `resourceUnitId`
  - `granularity`
- 当前响应核心字段:
  - `summary`
  - `curve`
  - `weatherTrend`
  - `contributionRanking`
  - `table`

### `GET /pvms/production-monitor/dispatch`

- 用途:
  - 获取调度执行视图数据
- 当前请求参数:
  - `resourceUnitId`
- 当前响应核心字段:
  - `summary`
  - `executionTrend`
  - `riskHints`
  - `records`

### `GET /pvms/production-monitor/weather`

- 用途:
  - 获取天气研判视图数据
- 当前请求参数:
  - `resourceUnitId`
- 当前响应核心字段:
  - `summary`
  - `trend`
  - `impactTable`

## Deprecated Semantics

- 旧模块 2 主链路:
  - `/station`
  - `/station/:id`
- 当前处理方式:
  - 路由保留重定向，统一跳到 `/production-monitor/overview`
- 旧接口集合:
  - `/pvms/resource-units/list`
  - `/pvms/resource-units/{id}/overview`
  - `/pvms/resource-units/{id}/power-curve`
- 当前模块主链路不再使用这套接口

## Visual Notes

- 保持模块 1 的深蓝色底、宿主主题变量和卡片语义
- 左侧菜单中模块 2 放在模块 1 下方
- 页面顶部使用横向视图导航，而不是二级左菜单
- 天气信息必须是光伏相关天气，不是普通天气描述
- 页面结构强调“模块壳子 + 共享筛选 + 不同视图”，而不是“列表页 + 详情页”

## Tests

- Playwright:
  - `tests/playwright/specs/station-monitoring.spec.js`
  - `tests/playwright/specs/layout-smoke.spec.js`
- 当前回归重点:
  - 模块页壳子和横向导航存在
  - 四个视图能切换
  - 资源单元切换后当前视图刷新
  - 成员电站共享天气上下文
  - 页面没有大块留白或布局塌陷
