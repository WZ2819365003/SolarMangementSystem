# M07 可调容量模块

## 功能

实时可调容量汇总: 全局摘要、分站明细、96 点时序曲线。

## 路由

### `GET /api/pvms/adjustable-capacity/realtime`
- 功能: 实时可调容量
- 参数: 无
- 返回:
  ```json
  {
    "summary": { "totalCapacityKw", "currentPowerKw", "maxAdjustableUpKw", "maxAdjustableDownKw", "deferrableLoadKw", "utilizationRate" },
    "stations": [{ "id", "name", "capacityKw", "currentPowerKw", "adjustableUpKw", "adjustableDownKw" }],
    "timeline": [{ "time", "totalPowerKw", "adjustableUpKw", "adjustableDownKw" }]
  }
  ```

## 计算规则

- 上调空间 = 装机容量 × 0.88 - 当前功率
- 下调空间 = 当前功率 - 装机容量 × 0.56
- 故障电站系数降至 0.1

## 测试

- 文件: `AdjustableCapacityControllerTest.java`
- 覆盖: 实时可调容量 (摘要+分站+时序)
- 测试数: 1 个
