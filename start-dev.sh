#!/bin/bash

echo ""
echo "=========================================="
echo "    FiveBear 开发环境启动脚本"
echo "=========================================="
echo ""

# 检查Redis服务状态
echo "正在检查 Redis 服务状态..."
if redis-cli ping > /dev/null 2>&1; then
    echo "Redis 服务已启动 ✓"
else
    echo "Redis 服务未启动，请手动启动 Redis"
    echo "macOS: brew services start redis"
    echo "Linux: sudo systemctl start redis"
    exit 1
fi

echo ""
echo "正在启动后端服务 (端口: 8080)..."
cd backend
gnome-terminal --tab --title="Backend Server" -- bash -c "mvn spring-boot:run; exec bash" 2>/dev/null || \
osascript -e 'tell application "Terminal" to do script "cd '$(pwd)' && mvn spring-boot:run"' 2>/dev/null || \
(echo "请手动在新终端中运行: cd backend && mvn spring-boot:run")

echo ""
echo "等待后端服务启动..."
sleep 10

echo ""
echo "正在启动前端服务 (端口: 3000)..."
cd ../frontend
gnome-terminal --tab --title="Frontend Server" -- bash -c "npm run dev; exec bash" 2>/dev/null || \
osascript -e 'tell application "Terminal" to do script "cd '$(pwd)' && npm run dev"' 2>/dev/null || \
(echo "请手动在新终端中运行: cd frontend && npm run dev")

echo ""
echo "=========================================="
echo "服务启动完成！"
echo "后端地址: http://localhost:8080"
echo "前端地址: http://localhost:3000"
echo "API文档: http://localhost:8080/swagger-ui.html"
echo "==========================================" 