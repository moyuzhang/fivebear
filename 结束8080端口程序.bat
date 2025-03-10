@echo off
echo 正在查找占用8080端口的进程...
for /f "tokens=5" %%p in ('netstat -aon ^| findstr :8080') do (
    echo 找到进程ID: %%p
    taskkill /PID %%p /F
    echo 进程 %%p 已结束
)