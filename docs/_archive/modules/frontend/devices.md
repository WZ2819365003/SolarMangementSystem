# Devices Module

## Route

- Path: `/devices/monitor`
- Purpose: display device summary, device grouping, maintenance suggestions, and the device status table

## API

- `GET /api/pvms/devices/monitor`

## Query Params

- `keyword`: matches device id, device name, station name, or device type
- `status`: `在线` / `离线` / `告警`

## Response Contract

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "summaryCards": [
      { "label": "设备总数", "value": 0, "unit": "台" }
    ],
    "deviceGroups": [
      { "label": "SUN2000-600KTL", "ratio": 25 }
    ],
    "maintenanceTips": [
      "站点 / 设备 温度过高，建议优先巡检。"
    ],
    "rows": [
      {
        "deviceId": "SZ-001-INV-01",
        "deviceName": "INV-01 (600kW)",
        "stationName": "深圳湾科创园 A 站",
        "type": "SUN2000-600KTL",
        "status": "在线",
        "loadRate": "78.6%",
        "temperature": "42.1℃",
        "lastReportAt": "2026-03-30 14:27:00"
      }
    ]
  }
}
```

## Notes

- This page now consumes backend H2 data, not frontend mock data.
- The backend already returns display-friendly strings for `loadRate`, `temperature`, and `lastReportAt`.
- The page itself remains a thin consumer and should not re-implement these calculations.
