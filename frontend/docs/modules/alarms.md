# Alarms Module

## Route

- Path: `/alarms/center`
- Purpose: 展示告警级别、处理看板和告警列表。

## Params

- Query params:
  - `keyword`: 告警 / 站点 / 设备关键字
  - `level`: 告警级别
  - `status`: 处置状态

## Data Contract

- API: `GET /pvms/alarms/center`
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [],
    "processBoard": [],
    "rows": [],
    "total": 0
  }
}
```
