#!/bin/bash
echo "正在查找占用8080端口的进程..."
PID=$(lsof -t -i:8080)
if [ -z "$PID" ]; then
    echo "没有找到占用8080端口的进程。"
else
    echo "找到进程ID: $PID"
    kill -9 $PID
    echo "进程 $PID 已结束"
fi