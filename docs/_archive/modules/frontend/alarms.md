# Alarms Module

## Route

- Path: `/alarms/center`
- Purpose: display alarm summary, process suggestions, and the alarm list

## API

- `GET /api/pvms/alarms/center`

## Query Params

- `keyword`: matches alarm id, station name, device name, category, or description
- `level`: `严重` / `重要` / `一般` / `提示`
- `status`: `未处理` / `处理中` / `待确认` / `已关闭`

## Response Contract

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [
      { "label": "今日新增", "value": 0, "unit": "条" }
    ],
    "processBoard": [
      "严重告警需 15 分钟内派单。"
    ],
    "rows": [
      {
        "alarmId": "SA-ALM-001",
        "stationName": "深圳湾科创园 A 站",
        "deviceName": "INV-01 (600kW)",
        "category": "通讯",
        "level": "提示",
        "status": "待确认",
        "owner": "华南运维组",
        "happenedAt": "2026-03-30 13:40:00"
      }
    ],
    "total": 1
  }
}
```

## Notes

- This page now consumes backend H2 alarm facts plus backend aggregation.
- The backend normalizes raw H2 level/status fields into the Chinese labels already used by the page.
- The page should keep using the backend response directly instead of rebuilding summary cards in the frontend.
