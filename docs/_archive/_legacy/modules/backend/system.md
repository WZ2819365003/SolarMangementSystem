# System 模块

## 功能

后端初始化与健康检查模块，用于确认服务是否启动正常。

## 路由

### `GET /api/system/health`

## 参数

无

## 响应

```json
{
  "code": 0,
  "message": "success",
  "data": {
    "service": "pvms-backend",
    "status": "UP",
    "version": "0.0.1-SNAPSHOT"
  }
}
```
