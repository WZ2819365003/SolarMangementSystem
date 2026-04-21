# Stations Module

## Route

- Path: `/stations/archive`
- Purpose: 展示站点档案、并网状态、健康等级和责任归属。

## Params

- Query params:
  - `keyword`: 站点 / 区域 / 责任人关键字
  - `gridStatus`: 并网状态

## Data Contract

- API: `GET /pvms/stations/archive`
- Response:

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "statistics": [],
    "rows": [],
    "total": 0
  }
}
```
