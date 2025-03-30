<#
.SYNOPSIS
导出 FiveBear 数据库结构到指定目录

.DESCRIPTION
该脚本执行以下操作：
1. 创建目标目录（如果不存在）
2. 使用 mysqldump 导出纯表结构
3. 添加时间戳到文件名
#>

# 配置参数
$MYSQL_PATH = "C:\Program Files\MySQL\MySQL Server 8.0\bin\mysqldump.exe"
$DB_NAME = "fivebear_db"
$OUTPUT_DIR = "d:\FiveBear\sql"
$TIMESTAMP = Get-Date -Format "yyyyMMdd_HHmmss"

# 创建目录
if (-not (Test-Path $OUTPUT_DIR)) {
    New-Item -ItemType Directory -Path $OUTPUT_DIR | Out-Null
}

# 执行导出
& "$MYSQL_PATH" -u root -p --no-data --skip-comments --skip-add-drop-table $DB_NAME | Out-File -FilePath "$OUTPUT_DIR\fivebear_structure_$TIMESTAMP.sql" -Encoding UTF8

# 结果检查
if ($LASTEXITCODE -eq 0) {
    Write-Host "✅ 数据库结构已导出到 $OUTPUT_DIR\fivebear_structure_$TIMESTAMP.sql"
} else {
    Write-Host "❌ 导出失败，请检查MySQL配置和路径" -ForegroundColor Red
}

# 执行方式更新为：
Set-ExecutionPolicy RemoteSigned -Scope Process -Force
.\scripts\db_export.ps1