# 文档导航

项目文档统一维护在 `docs/` 目录下。现在的文档体系按“新人交接可用、维护者能快速定位、设计和实现能互相对照”来组织。

## 新人优先阅读

1. `handover/README.md`
2. `handover/01_系统与阶段概览.md`
3. `handover/02_环境准备与本地启动.md`
4. `handover/03_仓库结构与核心代码入口.md`
5. `handover/04_前端维护手册.md`
6. `handover/05_后端维护手册.md`
7. `handover/07_Mock现状_联调策略_发布差距.md`
8. `handover/09_地图静态底图与动态点位下沉方案.md`

## 目录说明

- `handover/`
  面向接手同学的交接手册和专题说明。
- `audits/`
  代码审计、联调问题、阶段性质量评估。
- `modules/frontend/`
  前端模块结构、页面职责、接口依赖说明。
- `modules/backend/`
  后端接口、数据来源、计算逻辑、维护边界说明。
- `product/design/`
  设计方案、需求分析、模块规划。
- `product/delivery/`
  阶段性交付说明、推进记录。
- `testing/`
  测试策略、脚本、验证记录。
- `superpowers/specs/`
  设计 spec，记录目标边界、约束和方案拆分。
- `superpowers/plans/`
  已落地的实施计划，适合继续开发时对照执行。

## 当前重点入口

- 交接总入口：`docs/handover/README.md`
- 最新审计报告：`docs/audits/2026-03-29_前后端代码审计报告.md`
- 地图专题交接：`docs/handover/09_地图静态底图与动态点位下沉方案.md`
- 地图专题设计：`docs/superpowers/specs/2026-03-30-dashboard-map-static-dynamic-split-design.md`
- 地图专题实施计划：`docs/superpowers/plans/2026-03-30-dashboard-map-static-dynamic-split.md`
- 前端 dashboard 模块：`docs/modules/frontend/dashboard.md`
- 后端 dashboard 模块：`docs/modules/backend/dashboard.md`

## 维护约定

- 新增交接文档优先放在 `docs/handover/`。
- 审计与问题排查结论统一放在 `docs/audits/`。
- 模块说明按前后端分别维护，避免单篇文档过于混杂。
- 文档内容以当前代码为准，不保留已经失效的历史说明。
- 默认使用 UTF-8 编码。
