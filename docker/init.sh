#!/bin/bash

# 构建后端
cd ../backend
mvn clean package -DskipTests

# 构建前端
cd ../frontend
npm install
npm run build

# 构建并运行 Docker 容器
cd ../docker
docker-compose up -d --build