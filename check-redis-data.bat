@echo off
echo ========================================
echo Redis多地登录数据检查工具
echo ========================================
echo.

echo [1] 检查Redis连接状态:
redis-cli ping
echo.

echo [2] 查看所有用户会话键:
redis-cli KEYS "login:session:*"
echo.

echo [3] 查看admin用户的活跃会话:
redis-cli GET "login:session:admin"
echo.

echo [4] 查看所有登录相关的键:
redis-cli KEYS "login:*"
echo.

echo [5] 查看强制下线Token (如果存在):
redis-cli KEYS "*forced*"
echo.

echo ========================================
echo 数据检查完成
echo ========================================
pause 