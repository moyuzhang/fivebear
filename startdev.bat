@echo off
chcp 65001
echo.
echo ==========================================
echo    FiveBear 开发环境启动脚本
echo ==========================================
echo.

echo 正在检查 Redis 服务状态...
redis-cli ping >nul 2>&1
if %errorlevel% neq 0 (
    echo Redis 服务未启动，正在启动...
    redis-server --service-start
    timeout /t 3 >nul
) else (
    echo Redis 服务已启动 ✓
)

echo.
echo 正在启动后端服务 (端口: 8080)...
start "Backend Server" cmd /k "cd /d backend && mvn spring-boot:run"

echo.
echo 等待后端服务启动...
timeout /t 10 >nul

echo.
echo 正在启动前端服务 (端口: 3000)...
start "Frontend Server" cmd /k "cd /d frontend && npm run dev"

echo.
echo ==========================================
echo 服务启动完成！
echo 后端地址: http://localhost:8080
echo 前端地址: http://localhost:3000
echo API文档: http://localhost:8080/swagger-ui.html
echo ==========================================
echo.
echo 按任意键退出...
pause >nul 