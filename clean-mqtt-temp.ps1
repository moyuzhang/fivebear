# 清理 MQTT 临时目录的 PowerShell 脚本

# 确保 temp 目录存在
if (-not (Test-Path "temp")) {
    New-Item -ItemType Directory -Path "temp"
}

# 获取当前目录下所有匹配的目录
$tempDirs = Get-ChildItem -Path . -Directory | Where-Object { $_.Name -match '^fivebear-mqtt-client-.*-tcplocalhost1883$' }

# 如果没有找到匹配的目录，显示提示信息
if ($tempDirs.Count -eq 0) {
    Write-Host "没有找到需要清理的 MQTT 临时目录。"
    exit
}

# 显示找到的目录
Write-Host "找到以下 MQTT 临时目录："
$tempDirs | ForEach-Object { Write-Host $_.Name }

# 询问用户是否要移动这些目录
$confirmation = Read-Host "是否要移动这些目录到 temp 文件夹？(Y/N)"
if ($confirmation -eq 'Y') {
    # 移动目录
    $tempDirs | ForEach-Object {
        $targetPath = Join-Path "temp" $_.Name
        Move-Item -Path $_.FullName -Destination $targetPath -Force
        Write-Host "已移动: $($_.Name) -> temp/$($_.Name)"
    }
    Write-Host "移动完成！"
} else {
    Write-Host "操作已取消。"
} 