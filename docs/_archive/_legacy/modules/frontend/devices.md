# Devices Module

## Route

- Path: `/devices/monitor`
- Purpose: 展示设备在线率、设备分布、运维提示和实时状态。

## Params

- Query params:
  - `keyword`: 设备 / 站点 / 类型关键字
  - `status`: `在线` / `离线` / `告警`

## Data Contract

- API: `GET /pvms/devices/monitor`
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [],
    "deviceGroups": [],
    "maintenanceTips": [],
    "rows": [],
    "total": 0
  }
}
```
