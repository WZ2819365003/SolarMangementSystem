# 光伏管理系统 Docker 镜像包

## 文件说明

- `pvms-frontend.tar` - 前端Docker镜像包 (约 62.8 MB)
- `pvms-backend.tar` - 后端Docker镜像包 (约 304 MB)

## 使用方法

### 1. 加载Docker镜像

```powershell
# 加载前端镜像
docker load -i pvms-frontend.tar

# 加载后端镜像
docker load -i pvms-backend.tar
```

### 2. 启动容器

```powershell
# 启动前端容器
docker run -d -p 6618:80 --name pvms-frontend pvms-frontend

# 启动后端容器
docker run -d -p 8091:8091 --name pvms-backend pvms-backend
```

### 3. 访问应用

- 前端应用：http://localhost:6618/pvms/
- 后端API：http://localhost:8091

## 镜像信息

### 前端镜像 (pvms-frontend)
- 基础镜像：nginx:1.27-alpine
- 端口：80
- 路径：/usr/share/nginx/html/pvms

### 后端镜像 (pvms-backend)
- 基础镜像：eclipse-temurin:21-jre
- 端口：8091
- 应用：/app/pvms-backend.jar

## 注意事项

1. 确保Docker服务已启动
2. 端口6618和8091未被占用
3. 前端和后端容器需要同时运行才能正常使用
4. 如需停止容器：`docker stop pvms-frontend pvms-backend`
5. 如需删除容器：`docker rm pvms-frontend pvms-backend`

## 镜像导出时间

2026-04-03 15:28